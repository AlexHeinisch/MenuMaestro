package dev.heinisch.menumaestro.integration_test.organization;

import dev.heinisch.menumaestro.integration_test.BaseWebIntegrationTest;
import dev.heinisch.menumaestro.integration_test.utils.ErrorResponseAssert;
import io.restassured.RestAssured;
import io.restassured.http.Headers;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openapitools.model.ErrorResponse;
import org.openapitools.model.OrganizationCreateDto;
import org.openapitools.model.OrganizationSummaryDto;
import org.springframework.http.HttpStatus;

import java.util.List;

import static dev.heinisch.menumaestro.integration_test.utils.test_constants.DefaultAccountTestData.DEFAULT_USERNAME;
import static dev.heinisch.menumaestro.integration_test.utils.test_constants.DefaultAccountTestData.defaultAccount;
import static dev.heinisch.menumaestro.integration_test.utils.test_constants.DefaultOrganizationTestData.defaultCreateOrganizationDto;

public class DeleteOrganizationWebIntegrationTest extends BaseWebIntegrationTest {

    @Override
    protected String getBasePath() {
        return "/organizations";
    }

    @BeforeEach
    void setup() {
        accountRepository.save(defaultAccount());
    }


    @Test
    void whenDeleteOrg_withValidId_thenDeleted() {
        // Arrange
        var createDto = defaultCreateOrganizationDto();
        var summaryDto = createOrganizationSuccessful(createDto);

        // Act: Send DELETE request for the existing organization
        RestAssured.given()
                .contentType("application/json")
                .headers(new Headers(this.generateValidAuthorizationHeader(DEFAULT_USERNAME, List.of("ROLE_USER"))))
                .delete(URI + "/" + summaryDto.getId())
                .then()
                .statusCode(HttpStatus.NO_CONTENT.value());

        // Assert: Check the organization has been deleted
        RestAssured.given()
                .contentType("application/json")
                .headers(new Headers(this.generateValidAuthorizationHeader(DEFAULT_USERNAME, List.of("ROLE_USER"))))
                .get(URI + "/" + summaryDto.getId())
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value());
    }

    @Test
    void whenDeleteOrg_withNonExistingId_thenNotFound() {
        // Act: Send DELETE request for a non-existing organization ID
        String nonExistingId = "999999";
        var errorResponse = RestAssured.given()
                .contentType("application/json")
                .headers(new Headers(this.generateValidAuthorizationHeader(DEFAULT_USERNAME, List.of("ROLE_USER"))))
                .delete(URI + "/" + nonExistingId)
                .then()
                .extract()
                .as(ErrorResponse.class);

        // Assert: Validate that the response is a 404 Not Found
        ErrorResponseAssert.assertThat(errorResponse)
                .hasStatus(HttpStatus.NOT_FOUND)
                .messageContains("Organization with id");
    }

    private OrganizationSummaryDto createOrganizationSuccessful(OrganizationCreateDto dto) {
        return createOrganization(dto)
                .then()
                .statusCode(HttpStatus.CREATED.value())
                .extract()
                .as(OrganizationSummaryDto.class);
    }

    private Response createOrganization(OrganizationCreateDto dto) {
        return RestAssured.given()
                .contentType("application/json")
                .headers(new Headers(List.of(
                        this.generateValidAuthorizationHeader(DEFAULT_USERNAME, List.of("ROLE_USER")))))
                .body(dto)
                .post(URI);
    }
}
