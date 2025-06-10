package at.codemaestro.integration_test.account;

import at.codemaestro.domain.account.Account;
import at.codemaestro.integration_test.BaseWebIntegrationTest;
import at.codemaestro.integration_test.utils.ErrorResponseAssert;
import at.codemaestro.integration_test.utils.RestHelper;
import io.restassured.http.Headers;
import io.restassured.http.Method;
import jakarta.annotation.PostConstruct;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openapitools.model.AccountEditRequestDto;
import org.openapitools.model.AccountInfoDto;
import org.springframework.http.HttpStatus;

import java.util.List;

import static at.codemaestro.integration_test.utils.test_constants.DefaultAccountTestData.DEFAULT_EMAIL;
import static at.codemaestro.integration_test.utils.test_constants.DefaultAccountTestData.DEFAULT_FIRST_NAME;
import static at.codemaestro.integration_test.utils.test_constants.DefaultAccountTestData.DEFAULT_LAST_NAME;
import static at.codemaestro.integration_test.utils.test_constants.DefaultAccountTestData.DEFAULT_PASSWORD;
import static at.codemaestro.integration_test.utils.test_constants.DefaultAccountTestData.DEFAULT_USERNAME;
import static at.codemaestro.integration_test.utils.test_constants.DefaultAccountTestData.defaultEditRequestDto;
import static org.assertj.core.api.Assertions.assertThat;

public class EditAccountWebIntegrationTest extends BaseWebIntegrationTest {

    private RestHelper.PathAndBodyRestHelper<AccountInfoDto, String, AccountEditRequestDto> rest;

    @PostConstruct
    private void initRestHelper() {
        rest = new RestHelper.PathAndBodyRestHelper<>(
                AccountInfoDto.class,
                this.generateValidAuthorizationHeader(DEFAULT_USERNAME, List.of("ROLE_USER")),
                Method.PUT,
                URI + "/{username}",
                HttpStatus.OK
        );
    }

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
                .passwordResetToken(null)
                .passwordResetPermittedUntil(null)
                .build()
        );
    }

    @Test
    void basicAuthTests() {
        rest.basicAuthTests(DEFAULT_USERNAME, defaultEditRequestDto());
    }

    @Test
    void whenEditAccount_withoutCorrectJWT_forUser_thenForbidden() {
        var editDto = defaultEditRequestDto();
        var errorResponse = rest.requestFails(DEFAULT_USERNAME, editDto,
                HttpStatus.FORBIDDEN,
                new Headers(List.of(generateValidAuthorizationHeader(DEFAULT_USERNAME + "1", List.of("ROLE_USER"))))
        );
        ErrorResponseAssert.assertThat(errorResponse)
                .hasStatus(HttpStatus.FORBIDDEN)
                .messageContains("Access Denied");
    }

    @Test
    void whenEditAccount_withValidData_thenOK() {
        var editDto = defaultEditRequestDto();
        var responseBody = rest.requestSuccessful(
                DEFAULT_USERNAME,
                editDto
        );
        assertInfoDtoEqualsEditDto(responseBody, editDto);
    }

    @Test
    void whenEditAccount_withValidData_adminEditsForOtherUser_thenOK() {
        var editDto = defaultEditRequestDto();
        var responseBody = rest.requestSuccessful(
                DEFAULT_USERNAME,
                editDto,
                new Headers(List.of(generateValidAuthorizationHeader("someAdminUser", List.of("ROLE_ADMIN"))))
        );
        assertInfoDtoEqualsEditDto(responseBody, editDto);
    }

    @Test
    void whenEditAccount_withValidData_noFirstname_thenOK() {
        var editDto = defaultEditRequestDto().firstName(null);
        var responseBody = rest.requestSuccessful(
                DEFAULT_USERNAME,
                editDto
        );
        editDto.setFirstName(DEFAULT_FIRST_NAME);
        assertInfoDtoEqualsEditDto(responseBody, editDto);
    }

    @Test
    void whenEditAccount_withNonExistingAccount_thenNotFound() {
        var editDto = defaultEditRequestDto();
        var errorResponse = rest.requestFails(
                DEFAULT_USERNAME + "1",
                editDto,
                HttpStatus.NOT_FOUND,
                new Headers(generateValidAuthorizationHeader(DEFAULT_USERNAME + "1", List.of("ROLE_USER")))
        );
        ErrorResponseAssert.assertThat(errorResponse)
                .hasStatus(HttpStatus.NOT_FOUND)
                .messageContains("not found");
    }

    @Test
    void whenEditAccount_withAlreadyExistingEmail_thenConflict() {
        accountRepository.save(Account
                .builder()
                .username(DEFAULT_USERNAME + "Tmp")
                .email("existingEmail@example.com")
                .firstName(DEFAULT_FIRST_NAME)
                .lastName(DEFAULT_LAST_NAME)
                .passwordHash(passwordEncoder.encode(DEFAULT_PASSWORD))
                .isGlobalAdmin(true)
                .passwordResetToken(null)
                .passwordResetPermittedUntil(null)
                .build()
        );

        var editDto = defaultEditRequestDto()
                .email("existingEmail@example.com");
        var errorResponse = rest.requestFails(
                DEFAULT_USERNAME,
                editDto,
                HttpStatus.CONFLICT
        );
        ErrorResponseAssert.assertThat(errorResponse)
                .hasStatus(HttpStatus.CONFLICT)
                .messageContains("already exists");
    }

    @Test
    void whenEditAccount_withEditEmail_noMatchingConfirmPassword_thenForbidden() {
        var editDto = defaultEditRequestDto()
                .email("existingEmail@example.com")
                .oldPassword("wrongPass");
        var errorResponse = rest.requestFails(
                DEFAULT_USERNAME,
                editDto,
                HttpStatus.FORBIDDEN
        );
        ErrorResponseAssert.assertThat(errorResponse)
                .hasStatus(HttpStatus.FORBIDDEN)
                .messageContainsIgnoreCase("does not match");
    }

    @Test
    void whenEditAccount_withInvalidEmail_thenUnprocessableEntity() {
        var editDto = defaultEditRequestDto().email("B");
        var errorResponse = rest.requestFails(DEFAULT_USERNAME, editDto, HttpStatus.UNPROCESSABLE_ENTITY);
        ErrorResponseAssert.assertThat(errorResponse)
                .hasStatus(HttpStatus.UNPROCESSABLE_ENTITY)
                .messageContains("Validation error")
                .detailsContainSubstring("email is not an email");
    }

    @Test
    void whenEditAccount_withBlankEmail_thenUnprocessableEntity() {
        var editDto = defaultEditRequestDto().email("");
        var errorResponse = rest.requestFails(DEFAULT_USERNAME, editDto, HttpStatus.UNPROCESSABLE_ENTITY);
        ErrorResponseAssert.assertThat(errorResponse)
                .hasStatus(HttpStatus.UNPROCESSABLE_ENTITY)
                .messageContains("Validation error")
                .detailsContainSubstring("email cannot be blank");
    }

    @Test
    void whenEditAccount_withTooLongEmail_thenUnprocessableEntity() {
        var editDto = defaultEditRequestDto().email("LOL".repeat(20) + "@example.com");
        var errorResponse = rest.requestFails(DEFAULT_USERNAME, editDto, HttpStatus.UNPROCESSABLE_ENTITY);
        ErrorResponseAssert.assertThat(errorResponse)
                .hasStatus(HttpStatus.UNPROCESSABLE_ENTITY)
                .messageContains("Validation error")
                .detailsContainSubstring("email is too long");
    }

    @Test
    void whenEditAccount_withBlankFirstName_thenUnprocessableEntity() {
        var editDto = defaultEditRequestDto().firstName("");
        var errorResponse = rest.requestFails(DEFAULT_USERNAME, editDto, HttpStatus.UNPROCESSABLE_ENTITY);
        ErrorResponseAssert.assertThat(errorResponse)
                .hasStatus(HttpStatus.UNPROCESSABLE_ENTITY)
                .messageContains("Validation error")
                .detailsContainSubstring("first name cannot be blank");
    }

    @Test
    void whenEditAccount_withTooLongFirstName_thenUnprocessableEntity() {
        var editDto = defaultEditRequestDto().firstName("LOL".repeat(200));
        var errorResponse = rest.requestFails(DEFAULT_USERNAME, editDto, HttpStatus.UNPROCESSABLE_ENTITY);
        ErrorResponseAssert.assertThat(errorResponse)
                .hasStatus(HttpStatus.UNPROCESSABLE_ENTITY)
                .messageContains("Validation error")
                .detailsContainSubstring("first name is too long");
    }

    @Test
    void whenEditAccount_withBlankLastName_thenUnprocessableEntity() {
        var editDto = defaultEditRequestDto().lastName("");
        var errorResponse = rest.requestFails(DEFAULT_USERNAME, editDto, HttpStatus.UNPROCESSABLE_ENTITY);
        ErrorResponseAssert.assertThat(errorResponse)
                .hasStatus(HttpStatus.UNPROCESSABLE_ENTITY)
                .messageContains("Validation error")
                .detailsContainSubstring("last name cannot be blank");
    }

    @Test
    void whenEditAccount_withTooLongLastName_thenUnprocessableEntity() {
        var editDto = defaultEditRequestDto().lastName("LOL".repeat(200));
        var errorResponse = rest.requestFails(DEFAULT_USERNAME, editDto, HttpStatus.UNPROCESSABLE_ENTITY);
        ErrorResponseAssert.assertThat(errorResponse)
                .hasStatus(HttpStatus.UNPROCESSABLE_ENTITY)
                .messageContains("Validation error")
                .detailsContainSubstring("last name is too long");
    }

    @Test
    void whenEditAccount_withTooLongNewPassword_thenUnprocessableEntity() {
        var editDto = defaultEditRequestDto().newPassword("LOL".repeat(200));
        var errorResponse = rest.requestFails(DEFAULT_USERNAME, editDto, HttpStatus.UNPROCESSABLE_ENTITY);
        ErrorResponseAssert.assertThat(errorResponse)
                .hasStatus(HttpStatus.UNPROCESSABLE_ENTITY)
                .messageContains("Validation error")
                .detailsContainSubstring("password is too long");
    }

    @Test
    void whenEditAccount_withTooShortNewPassword_thenUnprocessableEntity() {
        var editDto = defaultEditRequestDto().newPassword("LOL");
        var errorResponse = rest.requestFails(DEFAULT_USERNAME, editDto, HttpStatus.UNPROCESSABLE_ENTITY);
        ErrorResponseAssert.assertThat(errorResponse)
                .hasStatus(HttpStatus.UNPROCESSABLE_ENTITY)
                .messageContains("Validation error")
                .detailsContainSubstring("password is too short");
    }

    @Test
    void whenEditAccount_withBlankNewPassword_thenUnprocessableEntity() {
        var editDto = defaultEditRequestDto().newPassword("");
        var errorResponse = rest.requestFails(DEFAULT_USERNAME, editDto, HttpStatus.UNPROCESSABLE_ENTITY);
        ErrorResponseAssert.assertThat(errorResponse)
                .hasStatus(HttpStatus.UNPROCESSABLE_ENTITY)
                .messageContains("Validation error")
                .detailsContainSubstring("password cannot be blank");
    }

    @Test
    void whenEditAccount_withTooLongOldPassword_thenUnprocessableEntity() {
        var editDto = defaultEditRequestDto().oldPassword("LOL".repeat(200));
        var errorResponse = rest.requestFails(DEFAULT_USERNAME, editDto, HttpStatus.UNPROCESSABLE_ENTITY);
        ErrorResponseAssert.assertThat(errorResponse)
                .hasStatus(HttpStatus.UNPROCESSABLE_ENTITY)
                .messageContains("Validation error")
                .detailsContainSubstring("password is too long");
    }

    @Test
    void whenEditAccount_withTooShortOldPassword_thenUnprocessableEntity() {
        var editDto = defaultEditRequestDto().oldPassword("LOL");
        var errorResponse = rest.requestFails(DEFAULT_USERNAME, editDto, HttpStatus.UNPROCESSABLE_ENTITY);
        ErrorResponseAssert.assertThat(errorResponse)
                .hasStatus(HttpStatus.UNPROCESSABLE_ENTITY)
                .messageContains("Validation error")
                .detailsContainSubstring("password is too short");
    }

    @Test
    void whenEditAccount_withBlankConfirmPassword_thenUnprocessableEntity() {
        var editDto = defaultEditRequestDto().oldPassword("");
        var errorResponse = rest.requestFails(DEFAULT_USERNAME, editDto, HttpStatus.UNPROCESSABLE_ENTITY);
        ErrorResponseAssert.assertThat(errorResponse)
                .hasStatus(HttpStatus.UNPROCESSABLE_ENTITY)
                .messageContains("Validation error")
                .detailsContainSubstring("password cannot be blank");
    }

    private void assertInfoDtoEqualsEditDto(AccountInfoDto accountInfoDto, AccountEditRequestDto accountEditRequestDto) {
        assertThat(accountInfoDto.getLastName()).isEqualTo(accountEditRequestDto.getLastName());
        assertThat(accountInfoDto.getFirstName()).isEqualTo(accountEditRequestDto.getFirstName());
        assertThat(accountInfoDto.getEmail()).isEqualTo(accountEditRequestDto.getEmail());
        assertThat(accountInfoDto.getIsGlobalAdmin()).isNotNull();
    }
}
