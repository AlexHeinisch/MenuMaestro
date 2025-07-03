package dev.heinisch.menumaestro.auth;

import dev.heinisch.menumaestro.BaseWebIntegrationTest;
import dev.heinisch.menumaestro.domain.account.Account;
import dev.heinisch.menumaestro.utils.ErrorResponseAssert;
import io.restassured.RestAssured;
import io.restassured.http.Header;
import io.restassured.response.Response;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openapitools.model.ErrorResponse;
import org.openapitools.model.LoginRequestDto;
import org.openapitools.model.TokenResponseDto;
import org.springframework.http.HttpStatus;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import static dev.heinisch.menumaestro.utils.test_constants.DefaultAccountTestData.DEFAULT_EMAIL;
import static dev.heinisch.menumaestro.utils.test_constants.DefaultAccountTestData.DEFAULT_FIRST_NAME;
import static dev.heinisch.menumaestro.utils.test_constants.DefaultAccountTestData.DEFAULT_LAST_NAME;
import static dev.heinisch.menumaestro.utils.test_constants.DefaultAccountTestData.DEFAULT_PASSWORD;
import static dev.heinisch.menumaestro.utils.test_constants.DefaultAccountTestData.DEFAULT_USERNAME;
import static dev.heinisch.menumaestro.utils.test_constants.DefaultAccountTestData.defaultLoginRequestDto;
import static org.assertj.core.api.Assertions.assertThat;

public class AuthenticationIT extends BaseWebIntegrationTest {

    @Override
    protected String getBasePath() {
        return "/auth";
    }

    @BeforeEach
    void setup() {
        accountRepository.save(Account
                .builder()
                .username(DEFAULT_USERNAME)
                .email(DEFAULT_EMAIL)
                .firstName(DEFAULT_FIRST_NAME)
                .lastName(DEFAULT_LAST_NAME)
                .passwordHash(passwordEncoder.encode(DEFAULT_PASSWORD))
                .isGlobalAdmin(true)
                .passwordResetToken(null)
                .passwordResetPermittedUntil(null)
                .build()
        );
    }

    @Test
    void whenLogin_withCorrectCredentials_thenOK() {
        var responseDto = login(new LoginRequestDto().username(DEFAULT_USERNAME).password(DEFAULT_PASSWORD))
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .as(TokenResponseDto.class);
        assertThat(responseDto.getAccessToken()).isNotNull();
        assertThat(responseDto.getAccessToken().getToken()).isNotNull();
        assertThat(responseDto.getAccessToken().getExpiryDate())
                .isNotNull()
                .isAfter(OffsetDateTime.now());
    }

    @Test
    void whenLogin_withIncorrectUsername_thenUnauthorized() {
        var errorResponse = loginFailed(
                defaultLoginRequestDto().username("XXX"),
                HttpStatus.UNAUTHORIZED
        );
        ErrorResponseAssert.assertThat(errorResponse)
                .hasStatus(HttpStatus.UNAUTHORIZED)
                .messageContains("Username or password is incorrect.");
    }

    @Test
    void whenLogin_withIncorrectPassword_thenUnauthorized() {
        var errorResponse = loginFailed(
                defaultLoginRequestDto().password("XXXXXXX"),
                HttpStatus.UNAUTHORIZED
        );
        ErrorResponseAssert.assertThat(errorResponse)
                .hasStatus(HttpStatus.UNAUTHORIZED)
                .messageContains("Username or password is incorrect.");
    }

    @Test
    void whenLogin_withBlankUsername_thenValidationError() {
        var errorResponse = loginFailed(
                defaultLoginRequestDto().username(""),
                HttpStatus.UNPROCESSABLE_ENTITY
        );
        ErrorResponseAssert.assertThat(errorResponse)
                .hasStatus(HttpStatus.UNPROCESSABLE_ENTITY)
                .messageContains("Validation error")
                .detailsContainSubstring("username cannot be blank");
    }

    @Test
    void whenLogin_withTooLongUsername_thenValidationError() {
        var errorResponse = loginFailed(
                defaultLoginRequestDto().username("LOL".repeat(250)),
                HttpStatus.UNPROCESSABLE_ENTITY
        );
        ErrorResponseAssert.assertThat(errorResponse)
                .hasStatus(HttpStatus.UNPROCESSABLE_ENTITY)
                .messageContains("Validation error")
                .detailsContainSubstring("username is too long");
    }

    @Test
    void whenLogin_withBlankPassword_thenValidationError() {
        var errorResponse = loginFailed(
                defaultLoginRequestDto().password(""),
                HttpStatus.UNPROCESSABLE_ENTITY
        );
        ErrorResponseAssert.assertThat(errorResponse)
                .hasStatus(HttpStatus.UNPROCESSABLE_ENTITY)
                .messageContains("Validation error")
                .detailsContainSubstring("password cannot be blank");
    }

    @Test
    void whenLogin_withTooShortPassword_thenValidationError() {
        var errorResponse = loginFailed(
                defaultLoginRequestDto().password("X"),
                HttpStatus.UNPROCESSABLE_ENTITY
        );
        ErrorResponseAssert.assertThat(errorResponse)
                .hasStatus(HttpStatus.UNPROCESSABLE_ENTITY)
                .messageContains("Validation error")
                .detailsContainSubstring("password is too short");
    }

    @Test
    void refreshWithJwt_refreshesJwtButKeepsExpiry() {
        Date expires = new Date(System.currentTimeMillis() + 1000*6);
        Header header = generateValidAuthorizationHeaderExpires(DEFAULT_USERNAME, Collections.emptyList(), expires);
        TokenResponseDto token = RestAssured
                .given()
                .header(header)
                .when()
                .post(URI+"/refresh-roles")
                .then()
                .statusCode(200)
                .log().headers()
                .extract().body().as(TokenResponseDto.class);
        Date expiryDate = Date.from(token.getAccessToken().getExpiryDate().toInstant());
        Assertions.assertTrue(Math.abs(Duration.between(expires.toInstant(), expiryDate.toInstant()).getSeconds()) < 2);
    }

    private ErrorResponse loginFailed(LoginRequestDto dto, HttpStatus status) {
        return login(dto)
                .then()
                .statusCode(status.value())
                .extract()
                .as(ErrorResponse.class);
    }

    private Response login(LoginRequestDto dto) {
        return RestAssured
                .given()
                .contentType("application/json")
                .body(dto)
                .post(URI + "/login");
    }

    protected Header generateValidAuthorizationHeaderExpires(String username, List<String> authorityStrings, Date expires) {
        HashMap<String, Object> authorities = new HashMap<>();
        authorities.put(jwtProperties.getAccountAccessToken().getRoleClaimName(), authorityStrings);
        return new Header("Authorization", "Bearer " + jwtService.generateAccountAccessToken(authorities, username, expires));
    }
}
