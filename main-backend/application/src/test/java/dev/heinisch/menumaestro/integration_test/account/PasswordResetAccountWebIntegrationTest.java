package dev.heinisch.menumaestro.integration_test.account;

import dev.heinisch.menumaestro.domain.account.Account;
import dev.heinisch.menumaestro.integration_test.BaseWebIntegrationTest;
import dev.heinisch.menumaestro.integration_test.utils.ErrorResponseAssert;
import dev.heinisch.menumaestro.service.EmailService;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.openapitools.model.ErrorResponse;
import org.openapitools.model.ResetPasswordCommitRequestDto;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;

import java.time.Instant;

import static dev.heinisch.menumaestro.integration_test.utils.test_constants.DefaultAccountTestData.DEFAULT_EMAIL;
import static dev.heinisch.menumaestro.integration_test.utils.test_constants.DefaultAccountTestData.DEFAULT_FIRST_NAME;
import static dev.heinisch.menumaestro.integration_test.utils.test_constants.DefaultAccountTestData.DEFAULT_LAST_NAME;
import static dev.heinisch.menumaestro.integration_test.utils.test_constants.DefaultAccountTestData.DEFAULT_NEW_PASSWORD;
import static dev.heinisch.menumaestro.integration_test.utils.test_constants.DefaultAccountTestData.DEFAULT_PASSWORD;
import static dev.heinisch.menumaestro.integration_test.utils.test_constants.DefaultAccountTestData.DEFAULT_PASSWORD_RESET_TOKEN;
import static dev.heinisch.menumaestro.integration_test.utils.test_constants.DefaultAccountTestData.DEFAULT_PASSWORD_RESET_VALID_UNTIL_DATE;
import static dev.heinisch.menumaestro.integration_test.utils.test_constants.DefaultAccountTestData.DEFAULT_USERNAME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

public class PasswordResetAccountWebIntegrationTest extends BaseWebIntegrationTest {

    @MockBean
    private EmailService emailService;

    @Override
    protected String getBasePath() {
        return "/accounts";
    }

    @BeforeEach
    void setup() {
        accountRepository.saveAndFlush(Account
                .builder()
                .username(DEFAULT_USERNAME)
                .email(DEFAULT_EMAIL)
                .firstName(DEFAULT_FIRST_NAME)
                .lastName(DEFAULT_LAST_NAME)
                .passwordHash(passwordEncoder.encode(DEFAULT_PASSWORD))
                .isGlobalAdmin(true)
                .passwordResetToken(DEFAULT_PASSWORD_RESET_TOKEN)
                .passwordResetPermittedUntil(DEFAULT_PASSWORD_RESET_VALID_UNTIL_DATE)
                .build()
        );
    }

    @Test
    void whenInitiateResetPassword_withExistingUser_thenEmailSent() {
        initiateResetPassword(DEFAULT_USERNAME)
                .then()
                .statusCode(HttpStatus.NO_CONTENT.value());
        verify(emailService, times(1))
                .sendPasswordResetEmail(eq(DEFAULT_EMAIL), Mockito.anyString());
    }

    @Test
    void whenInitiateResetPassword_withNonExistingUser_thenNoEmailSent() {
        initiateResetPassword(DEFAULT_USERNAME + "Blub")
                .then()
                .statusCode(HttpStatus.NO_CONTENT.value());
        verifyNoInteractions(emailService);
    }

    @Test
    void whenInitiateResetPassword_withBlankUsername_thenUnprocessableEntity() {
        var errorResponse = initiateResetPassword(" ")
                .then()
                .statusCode(HttpStatus.UNPROCESSABLE_ENTITY.value())
                .extract()
                .as(ErrorResponse.class);
        ErrorResponseAssert.assertThat(errorResponse)
                .hasStatus(HttpStatus.UNPROCESSABLE_ENTITY)
                .messageContains("Validation error")
                .detailsContainSubstring("username cannot be blank");
    }

    @Test
    void whenInitiateResetPassword_withTooLongUsername_thenUnprocessableEntity() {
        var errorResponse = initiateResetPassword("Blub".repeat(20))
                .then()
                .statusCode(HttpStatus.UNPROCESSABLE_ENTITY.value())
                .extract()
                .as(ErrorResponse.class);
        ErrorResponseAssert.assertThat(errorResponse)
                .hasStatus(HttpStatus.UNPROCESSABLE_ENTITY)
                .messageContains("Validation error")
                .detailsContainSubstring("username is too long");
    }

    @Test
    void whenCommitPasswordReset_withNonExistingUser_thenNoContent() {
        commitResetPassword(DEFAULT_USERNAME + "Blub", DEFAULT_PASSWORD_RESET_TOKEN, new ResetPasswordCommitRequestDto(DEFAULT_NEW_PASSWORD))
                .then()
                .statusCode(HttpStatus.NO_CONTENT.value());
    }

    @Test
    void whenCommitPasswordReset_withCorrectToken_thenNoContent() {
        commitResetPassword(DEFAULT_USERNAME, DEFAULT_PASSWORD_RESET_TOKEN, new ResetPasswordCommitRequestDto(DEFAULT_NEW_PASSWORD))
                .then()
                .statusCode(HttpStatus.NO_CONTENT.value());
        var account = accountRepository.findById(DEFAULT_USERNAME).orElseThrow();
        assertThat(passwordEncoder.matches(DEFAULT_NEW_PASSWORD, account.getPasswordHash())).isTrue();
    }

    @Test
    void whenCommitPasswordReset_withAccountNotInPasswordResetState_thenForbidden() {
        var account = accountRepository.findById(DEFAULT_USERNAME).orElseThrow();
        account.setPasswordResetPermittedUntil(null);
        account.setPasswordResetToken(null);
        accountRepository.save(account);

        var errorResponse = commitResetPasswordFails(DEFAULT_USERNAME, DEFAULT_PASSWORD_RESET_TOKEN, new ResetPasswordCommitRequestDto(DEFAULT_NEW_PASSWORD), HttpStatus.FORBIDDEN);
        ErrorResponseAssert.assertThat(errorResponse)
                .hasStatus(HttpStatus.FORBIDDEN)
                .messageContains("Password reset failed");
    }

    @Test
    void whenCommitPasswordReset_withInvalidToken_thenForbidden() {
        var errorResponse = commitResetPasswordFails(DEFAULT_USERNAME, "sometoken", new ResetPasswordCommitRequestDto(DEFAULT_NEW_PASSWORD), HttpStatus.FORBIDDEN);
        ErrorResponseAssert.assertThat(errorResponse)
                .hasStatus(HttpStatus.FORBIDDEN)
                .messageContains("Password reset failed");
    }

    @Test
    void whenCommitPasswordReset_withExpiredReset_thenForbidden() {
        var account = accountRepository.findById(DEFAULT_USERNAME).orElseThrow();
        account.setPasswordResetPermittedUntil(Instant.now().minusSeconds(100));
        accountRepository.save(account);

        var errorResponse = commitResetPasswordFails(DEFAULT_USERNAME, DEFAULT_PASSWORD_RESET_TOKEN, new ResetPasswordCommitRequestDto(DEFAULT_NEW_PASSWORD), HttpStatus.FORBIDDEN);
        ErrorResponseAssert.assertThat(errorResponse)
                .hasStatus(HttpStatus.FORBIDDEN)
                .messageContains("Password reset failed");
    }

    @Test
    void whenCommitPasswordReset_withBlankUser_thenUnprocessableEntity() {
        var errorResponse = commitResetPasswordFails(" ", DEFAULT_PASSWORD_RESET_TOKEN, new ResetPasswordCommitRequestDto(DEFAULT_NEW_PASSWORD), HttpStatus.UNPROCESSABLE_ENTITY);
        ErrorResponseAssert.assertThat(errorResponse)
                .hasStatus(HttpStatus.UNPROCESSABLE_ENTITY)
                .messageContains("Validation error")
                .detailsContainSubstring("username cannot be blank");
    }

    @Test
    void whenCommitPasswordReset_withTooLongUsername_thenUnprocessableEntity() {
        var errorResponse = commitResetPasswordFails("Blub".repeat(30), DEFAULT_PASSWORD_RESET_TOKEN, new ResetPasswordCommitRequestDto(DEFAULT_NEW_PASSWORD), HttpStatus.UNPROCESSABLE_ENTITY);
        ErrorResponseAssert.assertThat(errorResponse)
                .hasStatus(HttpStatus.UNPROCESSABLE_ENTITY)
                .messageContains("Validation error")
                .detailsContainSubstring("username is too long");
    }

    @Test
    void whenCommitPasswordReset_withBlankNewPassword_thenUnprocessableEntity() {
        var errorResponse = commitResetPasswordFails(DEFAULT_USERNAME, DEFAULT_PASSWORD_RESET_TOKEN, new ResetPasswordCommitRequestDto(" "), HttpStatus.UNPROCESSABLE_ENTITY);
        ErrorResponseAssert.assertThat(errorResponse)
                .hasStatus(HttpStatus.UNPROCESSABLE_ENTITY)
                .messageContains("Validation error")
                .detailsContainSubstring("password cannot be blank");
    }

    @Test
    void whenCommitPasswordReset_withTooShortNewPassword_thenUnprocessableEntity() {
        var errorResponse = commitResetPasswordFails(DEFAULT_USERNAME, DEFAULT_PASSWORD_RESET_TOKEN, new ResetPasswordCommitRequestDto("xx"), HttpStatus.UNPROCESSABLE_ENTITY);
        ErrorResponseAssert.assertThat(errorResponse)
                .hasStatus(HttpStatus.UNPROCESSABLE_ENTITY)
                .messageContains("Validation error")
                .detailsContainSubstring("password is too short");
    }

    @Test
    void whenCommitPasswordReset_withTooLongNewPassword_thenUnprocessableEntity() {
        var errorResponse = commitResetPasswordFails(DEFAULT_USERNAME, DEFAULT_PASSWORD_RESET_TOKEN, new ResetPasswordCommitRequestDto("xx".repeat(50)), HttpStatus.UNPROCESSABLE_ENTITY);
        ErrorResponseAssert.assertThat(errorResponse)
                .hasStatus(HttpStatus.UNPROCESSABLE_ENTITY)
                .messageContains("Validation error")
                .detailsContainSubstring("password is too long");
    }


    private ErrorResponse commitResetPasswordFails(String username, String token, ResetPasswordCommitRequestDto dto, HttpStatus status) {
        return commitResetPassword(username, token, dto)
                .then()
                .statusCode(status.value())
                .extract()
                .as(ErrorResponse.class);
    }

    private Response commitResetPassword(String username, String token, ResetPasswordCommitRequestDto dto) {
        return RestAssured
                .given()
                .contentType("application/json")
                .body(dto)
                .put(URI + "/" + username + "/reset-password/" + token);
    }

    private Response initiateResetPassword(String username) {
        return RestAssured
                .given()
                .post(URI + "/" + username + "/reset-password");
    }


}
