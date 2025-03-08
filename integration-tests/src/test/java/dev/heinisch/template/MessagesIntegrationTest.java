package dev.heinisch.template;

import org.junit.jupiter.api.Test;
import org.openapitools.model.MessageCreateRequest;
import org.openapitools.model.MessageResponse;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

public class MessagesIntegrationTest extends BaseTest {

    @Test
    public void testCreateMessage() {
        MessageCreateRequest request = new MessageCreateRequest().text("Hello");
        var response = given().contentType("application/json")
                .header("Content-Type", "application/json")
                .body(request)
                .when().post("/messages").then().statusCode(201).extract().as(MessageResponse.class);
        assertThat(response.getText()).isEqualTo("Hello");
    }



}
