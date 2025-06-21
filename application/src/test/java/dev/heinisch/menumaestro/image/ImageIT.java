package dev.heinisch.menumaestro.image;

import dev.heinisch.menumaestro.domain.image.ImageRecord;
import dev.heinisch.menumaestro.BaseWebIntegrationTest;
import dev.heinisch.menumaestro.persistence.ImageRepository;
import io.restassured.RestAssured;
import io.restassured.builder.MultiPartSpecBuilder;
import io.restassured.http.Header;
import io.restassured.http.Headers;
import io.restassured.specification.MultiPartSpecification;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openapitools.model.ErrorResponse;
import org.openapitools.model.ImageUploadResponseDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

@ActiveProfiles({"datagen-off", "test"})
public class ImageIT extends BaseWebIntegrationTest {
    private Header authHeader;

    @LocalServerPort
    int serverPort;

    @Autowired
    ImageRepository imageRepository;

    @Override
    protected String getBasePath() {
        return "/images";
    }

    @BeforeEach
    void setup() {
        authHeader = generateValidAuthorizationHeader("someone", List.of());
    }

    @Test
    void uploadJustBelowMaxFileSize_success() {
        RestAssured.given()
                .multiPart(multiPartWith("image/png", 1024 * 1024 - 16))
                .headers(Headers.headers(authHeader))
                .when()
                .post(URI)
                .then()
                .log().ifError()
                .statusCode(200);
        ImageRecord image = imageRepository.findAll().getFirst();
        Assertions.assertEquals("someone", image.getUploadedBy());
    }

    @Test
    void upload_unauthenticated_fails() {
        RestAssured.given()
                .multiPart(multiPartWith("image/png", 128))
                .log().all()
                .when()
                .post(URI)
                .then()
                .log().ifError()
                .statusCode(403);
    }

    @Test
    void upload_filenameHasNoMatchingExtension_fails() {
        var errorResponse = RestAssured.given()
                .multiPart(multiPartWith("image/exe", 128))
                .headers(Headers.headers(authHeader))
                .when()
                .post(URI)
                .then()
                .log().ifError()
                .statusCode(422)
                .extract().as(ErrorResponse.class);
        Assertions.assertTrue(errorResponse.getMessage().contains("Unsupported image mime type: image/exe"));
    }

    @Test
    void upload_fileSizeLimitExceeds_fails() {
        var file2 = multiPartWith("image/png", 1024 * 1024 * 5 + 1024);
        ErrorResponse response = RestAssured.given()
                .multiPart(file2)
                .headers(Headers.headers(authHeader))
                .when()
                .post(URI)
                .then()
                .log().ifError()
                .statusCode(413)
                .extract().as(ErrorResponse.class);
        Assertions.assertEquals(413, response.getStatus());
        Assertions.assertTrue(response.getMessage().contains("exceeded"));
    }

    @Test
    void download_wrongId_fails() {
        ErrorResponse response = RestAssured.given()
                .when()
                .get(URI + "/123123123")
                .then()
                .log().ifError()
                .statusCode(404)
                .extract().as(ErrorResponse.class);
        Assertions.assertAll(
                () -> Assertions.assertTrue(response.getMessage().contains("not found")),
                () -> Assertions.assertTrue(response.getMessage().contains("Image"))
        );
    }

    @Test
    void upload_thenDownload_success() {
        byte[] imageData = new byte[256];
        for (int i = 0; i < 256; i++) {
            imageData[i] = (byte) i;
        }
        var response = RestAssured.given()
                .multiPart(multiPartWith("image/png", imageData))
                .headers(Headers.headers(authHeader))
                .when()
                .post(URI)
                .then()
                .log().ifError()
                .statusCode(200)
                .extract().as(ImageUploadResponseDto.class);
        Assertions.assertNotNull(response);
        // download link does not know about our custom test port
        // need to replace with custom test port
        Assertions.assertAll(
                () -> Assertions.assertNotNull(response.getIdentifier()),
                () -> Assertions.assertNotNull(response.getDownloadLink()),
                () -> Assertions.assertTrue(response.getDownloadLink().contains(":8080/")),
                () -> Assertions.assertTrue(response.getDownloadLink().contains(response.getIdentifier()))
        );

        String downloadLink = response.getDownloadLink();
        downloadLink = downloadLink.replace(":8080/", ":" + serverPort + "/");
        byte[] downloadedImage = RestAssured.given()
                .get(downloadLink)
                .then()
                .statusCode(200)
                .extract().body().asByteArray();
        Assertions.assertArrayEquals(imageData, downloadedImage);
    }

    private MultiPartSpecification multiPartWith(String mimeType, int length) {
        return multiPartWith(mimeType, new byte[length]);
    }

    private MultiPartSpecification multiPartWith(String mimeType, byte[] content) {
        return new MultiPartSpecBuilder(content)
                .fileName("file")
                .controlName("file")
                .mimeType(mimeType)
                .build();
    }
}
