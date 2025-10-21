package dev.heinisch.menumaestro.account;

import dev.heinisch.menumaestro.BaseWebIntegrationTest;
import dev.heinisch.menumaestro.utils.ErrorResponseAssert;
import dev.heinisch.menumaestro.utils.RestHelper;
import io.restassured.RestAssured;
import io.restassured.http.Method;
import jakarta.annotation.PostConstruct;
import org.junit.jupiter.api.Test;
import org.openapitools.model.AccountCreateRequestDto;
import org.openapitools.model.AccountInfoDto;
import org.openapitools.model.ErrorResponse;
import org.springframework.http.HttpStatus;

import java.util.List;

import static dev.heinisch.menumaestro.utils.test_constants.DefaultAccountTestData.DEFAULT_USERNAME;
import static dev.heinisch.menumaestro.utils.test_constants.DefaultAccountTestData.defaultAccountCreateRequestDto;
import static org.assertj.core.api.Assertions.assertThat;

public class CreateAccountIT extends BaseWebIntegrationTest {

    private RestHelper.BodyRestHelper<AccountInfoDto, AccountCreateRequestDto> rest;

    @PostConstruct
    private void initRestHelper() {
        rest = new RestHelper.BodyRestHelper<>(
                AccountInfoDto.class,
                this.generateValidAuthorizationHeader(DEFAULT_USERNAME, List.of("ROLE_USER")),
                Method.POST,
                URI,
                HttpStatus.ACCEPTED
        );
    }

    @Override
    protected String getBasePath() {
        return "/accounts";
    }

    @Test
    void whenCreateAccount_withValidData_thenAccepted() {
        var createDto = defaultAccountCreateRequestDto();
        // Account creation now returns ACCEPTED (202) with no body since it creates a pending registration
        // that requires email verification before the account is fully created
        RestAssured.given()
                .contentType("application/json")
                .body(createDto)
                .when()
                .post(URI)
                .then()
                .statusCode(HttpStatus.ACCEPTED.value());
    }

    @Test
    void whenCreateAccount_withBlankUsername_thenUnprocessableEntity() {
        var createDto = defaultAccountCreateRequestDto()
                .username("");
        ErrorResponseAssert.assertThat(rest.requestFails(createDto, HttpStatus.UNPROCESSABLE_ENTITY))
                .hasStatus(HttpStatus.UNPROCESSABLE_ENTITY)
                .messageEquals("Validation error occurred!")
                .detailsContainSubstring("username cannot be blank");
    }

    @Test
    void whenCreateAccount_withBlankEmail_thenUnprocessableEntity() {
        var createDto = defaultAccountCreateRequestDto()
                .email("");
        ErrorResponseAssert.assertThat(rest.requestFails(createDto, HttpStatus.UNPROCESSABLE_ENTITY))
                .hasStatus(HttpStatus.UNPROCESSABLE_ENTITY)
                .messageEquals("Validation error occurred!")
                .detailsContainSubstring("email cannot be blank");
    }

    @Test
    void whenCreateAccount_withBlankFirstName_thenUnprocessableEntity() {
        var createDto = defaultAccountCreateRequestDto()
                .firstName("");
        ErrorResponseAssert.assertThat(rest.requestFails(createDto, HttpStatus.UNPROCESSABLE_ENTITY))
                .hasStatus(HttpStatus.UNPROCESSABLE_ENTITY)
                .messageEquals("Validation error occurred!")
                .detailsContainSubstring("first name cannot be blank");
    }

    @Test
    void whenCreateAccount_withBlankLastName_thenUnprocessableEntity() {
        var createDto = defaultAccountCreateRequestDto()
                .lastName("");
        ErrorResponseAssert.assertThat(rest.requestFails(createDto, HttpStatus.UNPROCESSABLE_ENTITY))
                .hasStatus(HttpStatus.UNPROCESSABLE_ENTITY)
                .messageEquals("Validation error occurred!")
                .detailsContainSubstring("last name cannot be blank");
    }

    @Test
    void whenCreateAccount_withBlankPassword_thenUnprocessableEntity() {
        var createDto = defaultAccountCreateRequestDto()
                .password("");
        ErrorResponseAssert.assertThat(rest.requestFails(createDto, HttpStatus.UNPROCESSABLE_ENTITY))
                .hasStatus(HttpStatus.UNPROCESSABLE_ENTITY)
                .messageEquals("Validation error occurred!")
                .detailsContainSubstring("password cannot be blank");
    }

    @Test
    void whenCreateAccount_withUsernameTooLong_thenUnprocessableEntity() {
        var createDto = defaultAccountCreateRequestDto()
                .username("TEST".repeat(200));
        ErrorResponseAssert.assertThat(rest.requestFails(createDto, HttpStatus.UNPROCESSABLE_ENTITY))
                .hasStatus(HttpStatus.UNPROCESSABLE_ENTITY)
                .messageEquals("Validation error occurred!")
                .detailsContainSubstring("username is too long");
    }

    @Test
    void whenCreateAccount_withFirstNameTooLong_thenUnprocessableEntity() {
        var createDto = defaultAccountCreateRequestDto()
                .firstName("TEST".repeat(200));
        ErrorResponseAssert.assertThat(rest.requestFails(createDto, HttpStatus.UNPROCESSABLE_ENTITY))
                .hasStatus(HttpStatus.UNPROCESSABLE_ENTITY)
                .messageEquals("Validation error occurred!")
                .detailsContainSubstring("first name is too long");
    }

    @Test
    void whenCreateAccount_withLastNameTooLong_thenUnprocessableEntity() {
        var createDto = defaultAccountCreateRequestDto()
                .lastName("TEST".repeat(200));
        ErrorResponseAssert.assertThat(rest.requestFails(createDto, HttpStatus.UNPROCESSABLE_ENTITY))
                .hasStatus(HttpStatus.UNPROCESSABLE_ENTITY)
                .messageEquals("Validation error occurred!")
                .detailsContainSubstring("last name is too long");
    }

    @Test
    void whenCreateAccount_withPasswordTooLong_thenUnprocessableEntity() {
        var createDto = defaultAccountCreateRequestDto()
                .password("TEST".repeat(200));
        ErrorResponseAssert.assertThat(rest.requestFails(createDto, HttpStatus.UNPROCESSABLE_ENTITY))
                .hasStatus(HttpStatus.UNPROCESSABLE_ENTITY)
                .messageEquals("Validation error occurred!")
                .detailsContainSubstring("password is too long");
    }

    @Test
    void whenCreateAccount_withPasswordTooShort_thenUnprocessableEntity() {
        var createDto = defaultAccountCreateRequestDto()
                .password("T");
        ErrorResponseAssert.assertThat(rest.requestFails(createDto, HttpStatus.UNPROCESSABLE_ENTITY))
                .hasStatus(HttpStatus.UNPROCESSABLE_ENTITY)
                .messageEquals("Validation error occurred!")
                .detailsContainSubstring("password is too short");
    }

    @Test
    void whenCreateAccount_withInvalidEmailFormat_thenUnprocessableEntity() {
        var createDto = defaultAccountCreateRequestDto()
                .email("ThisIsNotAnEmail");
        ErrorResponseAssert.assertThat(rest.requestFails(createDto, HttpStatus.UNPROCESSABLE_ENTITY))
                .hasStatus(HttpStatus.UNPROCESSABLE_ENTITY)
                .messageEquals("Validation error occurred!")
                .detailsContainSubstring("email is not an email");
    }

    @Test
    void whenCreateAccount_withAlreadyExistingUsername_thenConflict() {
        var createDto = defaultAccountCreateRequestDto();
        RestAssured.given()
                .contentType("application/json")
                .body(createDto)
                .when()
                .post(URI)
                .then()
                .statusCode(HttpStatus.ACCEPTED.value());

        // Now returns "already pending verification" message since first registration is pending
        ErrorResponseAssert.assertThat(
                RestAssured.given()
                        .contentType("application/json")
                        .body(createDto.email("new@example.com"))
                        .when()
                        .post(URI)
                        .then()
                        .statusCode(HttpStatus.CONFLICT.value())
                        .extract()
                        .as(ErrorResponse.class)
        )
                .hasStatus(HttpStatus.CONFLICT)
                .messageContains("already pending verification");
    }

    @Test
    void whenCreateAccount_withAlreadyExistingEmail_thenConflict() {
        var createDto = defaultAccountCreateRequestDto();
        RestAssured.given()
                .contentType("application/json")
                .body(createDto)
                .when()
                .post(URI)
                .then()
                .statusCode(HttpStatus.ACCEPTED.value());

        // Now returns "already pending verification" message since first registration is pending
        ErrorResponseAssert.assertThat(
                RestAssured.given()
                        .contentType("application/json")
                        .body(createDto.username("newusername"))
                        .when()
                        .post(URI)
                        .then()
                        .statusCode(HttpStatus.CONFLICT.value())
                        .extract()
                        .as(ErrorResponse.class)
        )
                .hasStatus(HttpStatus.CONFLICT)
                .messageContains("already pending verification");
    }

    private void assertInfoDtoEqualsCreateDto(AccountInfoDto accountInfoDto, AccountCreateRequestDto accountCreateRequestDto) {
        assertThat(accountInfoDto.getLastName()).isEqualTo(accountCreateRequestDto.getLastName());
        assertThat(accountInfoDto.getFirstName()).isEqualTo(accountCreateRequestDto.getFirstName());
        assertThat(accountInfoDto.getUsername()).isEqualTo(accountCreateRequestDto.getUsername());
        assertThat(accountInfoDto.getEmail()).isEqualTo(accountCreateRequestDto.getEmail());
        assertThat(accountInfoDto.getIsGlobalAdmin()).isNotNull();
    }

}
