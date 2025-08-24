package dev.heinisch.menumaestro.account;

import dev.heinisch.menumaestro.BaseWebIntegrationTest;
import io.restassured.RestAssured;
import org.junit.jupiter.api.Test;
import org.openapitools.model.AccountInfoResponse;
import org.openapitools.model.ConfirmEmailRequest;
import org.openapitools.model.LoginRequest;
import org.springframework.http.HttpStatus;

import static dev.heinisch.menumaestro.utils.test_constants.DefaultAccountTestData.defaultAccountCreateRequestDto;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class FullCreateAccountIT extends BaseWebIntegrationTest {

    @Override
    protected String getBasePath() {
        return "";
    }

    @Test
    void fullAccountCreationTest_success() {
        var dto = defaultAccountCreateRequestDto();
        doNothing().when(emailService).sendEmailConfirmationEmail(any(), any(), any());

        var createRequest = RestAssured
                .given()
                .contentType("application/json")
                .body(dto)
                .post(URI + "/accounts")
                .then()
                .assertThat()
                .statusCode(HttpStatus.CREATED.value())
                .extract()
                .as(AccountInfoResponse.class);
        assertThat(createRequest).isNotNull();
        assertThat(createRequest.getUsername()).isNotNull().isEqualTo(dto.getUsername());
        assertThat(createRequest.getEmail()).isNotNull().isEqualTo(dto.getEmail());
        assertThat(createRequest.getFirstName()).isNotNull().isEqualTo(dto.getFirstName());
        assertThat(createRequest.getLastName()).isNotNull().isEqualTo(dto.getLastName());
        verify(emailService, times(1)).sendEmailConfirmationEmail(any(), any(), any());

        RestAssured
                .given()
                .contentType("application/json")
                .body(new LoginRequest().username(dto.getUsername()).password(dto.getPassword()))
                .post(URI + "/auth/login")
                .then()
                .assertThat()
                .statusCode(HttpStatus.UNAUTHORIZED.value());

        String token = pendingAccountRepository.findByEmail(dto.getEmail()).orElseThrow().getConfirmationToken();
        RestAssured
                .given()
                .contentType("application/json")
                .body(new ConfirmEmailRequest().token(token))
                .post(URI + "/accounts/" + dto.getUsername() + "/confirm-email")
                .then()
                .assertThat()
                .statusCode(HttpStatus.NO_CONTENT.value());

        RestAssured
                .given()
                .contentType("application/json")
                .body(new LoginRequest().username(dto.getUsername()).password(dto.getPassword()))
                .post(URI + "/auth/login")
                .then()
                .assertThat()
                .statusCode(HttpStatus.OK.value());
    }

}
