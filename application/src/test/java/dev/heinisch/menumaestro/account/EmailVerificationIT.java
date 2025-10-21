package dev.heinisch.menumaestro.account;

import dev.heinisch.menumaestro.BaseWebIntegrationTest;
import dev.heinisch.menumaestro.domain.account.PendingRegistration;
import dev.heinisch.menumaestro.persistence.PendingRegistrationRepository;
import dev.heinisch.menumaestro.service.PendingRegistrationService;
import dev.heinisch.menumaestro.utils.ErrorResponseAssert;
import io.restassured.RestAssured;
import org.junit.jupiter.api.Test;
import org.openapitools.model.AccountCreateRequestDto;
import org.openapitools.model.AccountInfoDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static dev.heinisch.menumaestro.utils.test_constants.DefaultAccountTestData.DEFAULT_EMAIL;
import static dev.heinisch.menumaestro.utils.test_constants.DefaultAccountTestData.DEFAULT_FIRST_NAME;
import static dev.heinisch.menumaestro.utils.test_constants.DefaultAccountTestData.DEFAULT_LAST_NAME;
import static dev.heinisch.menumaestro.utils.test_constants.DefaultAccountTestData.DEFAULT_PASSWORD;
import static dev.heinisch.menumaestro.utils.test_constants.DefaultAccountTestData.DEFAULT_USERNAME;
import static dev.heinisch.menumaestro.utils.test_constants.DefaultAccountTestData.defaultAccountCreateRequestDto;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;

public class EmailVerificationIT extends BaseWebIntegrationTest {

    @Autowired
    private PendingRegistrationRepository pendingRegistrationRepository;

    @Autowired
    private PendingRegistrationService pendingRegistrationService;

    @Override
    protected String getBasePath() {
        return "/accounts";
    }

    @Test
    void whenCreateAccount_withValidData_thenPendingRegistrationCreated() {
        AccountCreateRequestDto createDto = defaultAccountCreateRequestDto();

        RestAssured.given()
                .contentType("application/json")
                .body(createDto)
                .when()
                .post(URI)
                .then()
                .statusCode(HttpStatus.ACCEPTED.value());

        // Verify pending registration was created
        PendingRegistration pending = pendingRegistrationRepository.findByUsername(DEFAULT_USERNAME).orElseThrow();
        assertThat(pending.getUsername()).isEqualTo(DEFAULT_USERNAME);
        assertThat(pending.getEmail()).isEqualTo(DEFAULT_EMAIL);
        assertThat(pending.getFirstName()).isEqualTo(DEFAULT_FIRST_NAME);
        assertThat(pending.getLastName()).isEqualTo(DEFAULT_LAST_NAME);
        assertThat(pending.getVerificationToken()).isNotNull();
        assertThat(pending.getExpiresAt()).isAfter(Instant.now());

        // Verify email was sent
        verify(emailService).sendEmailVerification(anyString(), anyString());

        // Verify account was NOT created yet
        assertThat(accountRepository.findById(DEFAULT_USERNAME)).isEmpty();
    }

    @Test
    void whenVerifyEmail_withValidToken_thenAccountCreated() {
        // Create pending registration
        AccountCreateRequestDto createDto = defaultAccountCreateRequestDto();
        RestAssured.given()
                .contentType("application/json")
                .body(createDto)
                .when()
                .post(URI)
                .then()
                .statusCode(HttpStatus.ACCEPTED.value());

        PendingRegistration pending = pendingRegistrationRepository.findByUsername(DEFAULT_USERNAME).orElseThrow();
        String token = pending.getVerificationToken();

        // Verify email
        AccountInfoDto response = RestAssured.given()
                .queryParam("token", token)
                .when()
                .get(URI.replace("/accounts", "/verify-email"))
                .then()
                .statusCode(HttpStatus.CREATED.value())
                .extract()
                .as(AccountInfoDto.class);

        // Assert account was created
        assertThat(response.getUsername()).isEqualTo(DEFAULT_USERNAME);
        assertThat(response.getEmail()).isEqualTo(DEFAULT_EMAIL);
        assertThat(accountRepository.findById(DEFAULT_USERNAME)).isPresent();

        // Assert pending registration was deleted
        assertThat(pendingRegistrationRepository.findByUsername(DEFAULT_USERNAME)).isEmpty();
    }

    @Test
    void whenVerifyEmail_withInvalidToken_thenNotFound() {
        String invalidToken = UUID.randomUUID().toString();

        ErrorResponseAssert.assertThat(
                RestAssured.given()
                        .queryParam("token", invalidToken)
                        .when()
                        .get(URI.replace("/accounts", "/verify-email"))
                        .then()
                        .statusCode(HttpStatus.NOT_FOUND.value())
                        .extract()
                        .as(Object.class)
        )
                .hasStatus(HttpStatus.NOT_FOUND)
                .messageContains("Invalid or expired verification token");
    }

    @Test
    void whenVerifyEmail_withExpiredToken_thenForbidden() {
        // Manually create an expired pending registration
        PendingRegistration expiredRegistration = PendingRegistration.builder()
                .username(DEFAULT_USERNAME)
                .email(DEFAULT_EMAIL)
                .firstName(DEFAULT_FIRST_NAME)
                .lastName(DEFAULT_LAST_NAME)
                .passwordHash(passwordEncoder.encode(DEFAULT_PASSWORD))
                .verificationToken(UUID.randomUUID().toString())
                .createdAt(Instant.now().minus(1, ChronoUnit.HOURS))
                .expiresAt(Instant.now().minus(1, ChronoUnit.MINUTES))
                .build();
        pendingRegistrationRepository.save(expiredRegistration);

        ErrorResponseAssert.assertThat(
                RestAssured.given()
                        .queryParam("token", expiredRegistration.getVerificationToken())
                        .when()
                        .get(URI.replace("/accounts", "/verify-email"))
                        .then()
                        .statusCode(HttpStatus.FORBIDDEN.value())
                        .extract()
                        .as(Object.class)
        )
                .hasStatus(HttpStatus.FORBIDDEN)
                .messageContains("expired");

        // Verify it was deleted
        assertThat(pendingRegistrationRepository.findByUsername(DEFAULT_USERNAME)).isEmpty();
    }

    @Test
    void whenCreateAccount_withDuplicateUsername_inPendingRegistrations_thenConflict() {
        // Create first pending registration
        AccountCreateRequestDto createDto = defaultAccountCreateRequestDto();
        RestAssured.given()
                .contentType("application/json")
                .body(createDto)
                .when()
                .post(URI)
                .then()
                .statusCode(HttpStatus.ACCEPTED.value());

        // Try to create another with same username
        ErrorResponseAssert.assertThat(
                RestAssured.given()
                        .contentType("application/json")
                        .body(createDto.email("different@example.com"))
                        .when()
                        .post(URI)
                        .then()
                        .statusCode(HttpStatus.CONFLICT.value())
                        .extract()
                        .as(Object.class)
        )
                .hasStatus(HttpStatus.CONFLICT)
                .messageContains("already pending verification");
    }

    @Test
    void whenCreateAccount_withDuplicateEmail_inPendingRegistrations_thenConflict() {
        // Create first pending registration
        AccountCreateRequestDto createDto = defaultAccountCreateRequestDto();
        RestAssured.given()
                .contentType("application/json")
                .body(createDto)
                .when()
                .post(URI)
                .then()
                .statusCode(HttpStatus.ACCEPTED.value());

        // Try to create another with same email
        ErrorResponseAssert.assertThat(
                RestAssured.given()
                        .contentType("application/json")
                        .body(createDto.username("differentuser"))
                        .when()
                        .post(URI)
                        .then()
                        .statusCode(HttpStatus.CONFLICT.value())
                        .extract()
                        .as(Object.class)
        )
                .hasStatus(HttpStatus.CONFLICT)
                .messageContains("already pending verification");
    }

    @Test
    void whenCreateAccount_withExistingAccountUsername_thenConflict() {
        // Create an actual account first (directly in repo to bypass email verification)
        accountRepository.save(
                dev.heinisch.menumaestro.domain.account.Account.builder()
                        .username(DEFAULT_USERNAME)
                        .email(DEFAULT_EMAIL)
                        .firstName(DEFAULT_FIRST_NAME)
                        .lastName(DEFAULT_LAST_NAME)
                        .passwordHash(passwordEncoder.encode(DEFAULT_PASSWORD))
                        .isGlobalAdmin(false)
                        .build()
        );

        // Try to create pending registration with same username
        AccountCreateRequestDto createDto = defaultAccountCreateRequestDto();
        ErrorResponseAssert.assertThat(
                RestAssured.given()
                        .contentType("application/json")
                        .body(createDto.email("different@example.com"))
                        .when()
                        .post(URI)
                        .then()
                        .statusCode(HttpStatus.CONFLICT.value())
                        .extract()
                        .as(Object.class)
        )
                .hasStatus(HttpStatus.CONFLICT)
                .messageContains("already exists");
    }

    @Test
    void whenVerifyEmail_butUsernameAlreadyTaken_thenConflict() {
        // Create pending registration
        AccountCreateRequestDto createDto = defaultAccountCreateRequestDto();
        RestAssured.given()
                .contentType("application/json")
                .body(createDto)
                .when()
                .post(URI)
                .then()
                .statusCode(HttpStatus.ACCEPTED.value());

        PendingRegistration pending = pendingRegistrationRepository.findByUsername(DEFAULT_USERNAME).orElseThrow();
        String token = pending.getVerificationToken();

        // Manually create an account with the same username before verification completes
        accountRepository.save(
                dev.heinisch.menumaestro.domain.account.Account.builder()
                        .username(DEFAULT_USERNAME)
                        .email("other@example.com")
                        .firstName(DEFAULT_FIRST_NAME)
                        .lastName(DEFAULT_LAST_NAME)
                        .passwordHash(passwordEncoder.encode(DEFAULT_PASSWORD))
                        .isGlobalAdmin(false)
                        .build()
        );

        // Try to verify - should fail
        ErrorResponseAssert.assertThat(
                RestAssured.given()
                        .queryParam("token", token)
                        .when()
                        .get(URI.replace("/accounts", "/verify-email"))
                        .then()
                        .statusCode(HttpStatus.CONFLICT.value())
                        .extract()
                        .as(Object.class)
        )
                .hasStatus(HttpStatus.CONFLICT)
                .messageContains("already exists");

        // Pending registration should be deleted
        assertThat(pendingRegistrationRepository.findByUsername(DEFAULT_USERNAME)).isEmpty();
    }

    @Test
    void whenCleanupExpiredRegistrations_thenExpiredOnesDeleted() {
        // Create a valid pending registration
        PendingRegistration validRegistration = PendingRegistration.builder()
                .username("validuser")
                .email("valid@example.com")
                .firstName("Valid")
                .lastName("User")
                .passwordHash(passwordEncoder.encode(DEFAULT_PASSWORD))
                .verificationToken(UUID.randomUUID().toString())
                .createdAt(Instant.now())
                .expiresAt(Instant.now().plus(30, ChronoUnit.MINUTES))
                .build();
        pendingRegistrationRepository.save(validRegistration);

        // Create an expired pending registration
        PendingRegistration expiredRegistration = PendingRegistration.builder()
                .username("expireduser")
                .email("expired@example.com")
                .firstName("Expired")
                .lastName("User")
                .passwordHash(passwordEncoder.encode(DEFAULT_PASSWORD))
                .verificationToken(UUID.randomUUID().toString())
                .createdAt(Instant.now().minus(1, ChronoUnit.HOURS))
                .expiresAt(Instant.now().minus(1, ChronoUnit.MINUTES))
                .build();
        pendingRegistrationRepository.save(expiredRegistration);

        // Run cleanup
        pendingRegistrationService.cleanupExpiredRegistrations();

        // Verify expired was deleted
        assertThat(pendingRegistrationRepository.findByUsername("expireduser")).isEmpty();

        // Verify valid still exists
        assertThat(pendingRegistrationRepository.findByUsername("validuser")).isPresent();
    }

    @Test
    void whenPasswordIsEncoded_inPendingRegistration() {
        AccountCreateRequestDto createDto = defaultAccountCreateRequestDto();
        RestAssured.given()
                .contentType("application/json")
                .body(createDto)
                .when()
                .post(URI)
                .then()
                .statusCode(HttpStatus.ACCEPTED.value());

        PendingRegistration pending = pendingRegistrationRepository.findByUsername(DEFAULT_USERNAME).orElseThrow();

        // Verify password is encoded (not plain text)
        assertThat(pending.getPasswordHash()).isNotEqualTo(DEFAULT_PASSWORD);
        assertThat(passwordEncoder.matches(DEFAULT_PASSWORD, pending.getPasswordHash())).isTrue();
    }
}
