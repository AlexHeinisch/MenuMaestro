package dev.heinisch.menumaestro.account;

import dev.heinisch.menumaestro.BaseWebIntegrationTest;
import dev.heinisch.menumaestro.domain.account.PendingRegistration;
import dev.heinisch.menumaestro.persistence.PendingRegistrationRepository;
import dev.heinisch.menumaestro.service.PendingRegistrationService;
import dev.heinisch.menumaestro.utils.ErrorResponseAssert;
import dev.heinisch.menumaestro.utils.RestHelper;
import io.restassured.http.Method;
import jakarta.annotation.PostConstruct;
import org.junit.jupiter.api.Test;
import org.openapitools.model.AccountCreateRequestDto;
import org.openapitools.model.AccountInfoDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import static dev.heinisch.menumaestro.utils.test_constants.DefaultAccountTestData.DEFAULT_EMAIL;
import static dev.heinisch.menumaestro.utils.test_constants.DefaultAccountTestData.DEFAULT_FIRST_NAME;
import static dev.heinisch.menumaestro.utils.test_constants.DefaultAccountTestData.DEFAULT_LAST_NAME;
import static dev.heinisch.menumaestro.utils.test_constants.DefaultAccountTestData.DEFAULT_PASSWORD;
import static dev.heinisch.menumaestro.utils.test_constants.DefaultAccountTestData.DEFAULT_USERNAME;
import static dev.heinisch.menumaestro.utils.test_constants.DefaultAccountTestData.defaultAccountCreateRequestDto;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;

public class EmailVerificationIT extends BaseWebIntegrationTest {

    @Autowired
    private PendingRegistrationRepository pendingRegistrationRepository;

    @Autowired
    private PendingRegistrationService pendingRegistrationService;

    private RestHelper.BodyWithoutReturnRestHelper<AccountCreateRequestDto> rest;
    private RestHelper.QueryRestHelper verificationRest;

    @PostConstruct
    private void initRestHelper() {
        rest = new RestHelper.BodyWithoutReturnRestHelper<>(
                this.generateValidAuthorizationHeader(DEFAULT_USERNAME, List.of("ROLE_USER")),
                Method.POST,
                URI,
                HttpStatus.ACCEPTED
        );
        verificationRest = new RestHelper.QueryRestHelper(
                this.generateValidAuthorizationHeader(DEFAULT_USERNAME, List.of("ROLE_USER")),
                Method.GET,
                URI + "/verification",
                HttpStatus.CREATED
        );
    }

    @Override
    protected String getBasePath() {
        return "/accounts";
    }

    @Test
    void whenCreateAccount_withValidData_thenPendingRegistrationCreated() {
        // Arrange
        AccountCreateRequestDto createDto = defaultAccountCreateRequestDto();

        // Act
        rest.requestSuccessful(createDto);

        // Assert
        PendingRegistration pending = pendingRegistrationRepository.findByUsername(DEFAULT_USERNAME).orElseThrow();
        assertThat(pending.getUsername()).isEqualTo(DEFAULT_USERNAME);
        assertThat(pending.getEmail()).isEqualTo(DEFAULT_EMAIL);
        assertThat(pending.getFirstName()).isEqualTo(DEFAULT_FIRST_NAME);
        assertThat(pending.getLastName()).isEqualTo(DEFAULT_LAST_NAME);
        assertThat(pending.getVerificationToken()).isNotNull();
        assertThat(pending.getExpiresAt()).isAfter(Instant.now());

        verify(emailService).sendEmailVerification(anyString(), anyString());
        assertThat(accountRepository.findById(DEFAULT_USERNAME)).isEmpty();
    }

    @Test
    void whenVerifyEmail_withValidToken_thenAccountCreated() {
        // Arrange
        AccountCreateRequestDto createDto = defaultAccountCreateRequestDto();
        rest.requestSuccessful(createDto);

        PendingRegistration pending = pendingRegistrationRepository.findByUsername(DEFAULT_USERNAME).orElseThrow();
        String token = pending.getVerificationToken();

        // Act
        AccountInfoDto response = verificationRest.request("token=" + token)
                .then()
                .statusCode(HttpStatus.CREATED.value())
                .extract()
                .as(AccountInfoDto.class);

        // Assert
        assertThat(response.getUsername()).isEqualTo(DEFAULT_USERNAME);
        assertThat(response.getEmail()).isEqualTo(DEFAULT_EMAIL);
        assertThat(accountRepository.findById(DEFAULT_USERNAME)).isPresent();
        assertThat(pendingRegistrationRepository.findByUsername(DEFAULT_USERNAME)).isEmpty();
    }

    @Test
    void whenVerifyEmail_withInvalidToken_thenNotFound() {
        // Arrange
        String invalidToken = UUID.randomUUID().toString();

        // Act & Assert
        ErrorResponseAssert.assertThat(verificationRest.requestFails("token=" + invalidToken, HttpStatus.NOT_FOUND))
                .hasStatus(HttpStatus.NOT_FOUND)
                .messageContains("Invalid or expired verification token");
    }

    @Test
    void whenVerifyEmail_withExpiredToken_thenForbidden() {
        // Arrange
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

        // Act & Assert
        ErrorResponseAssert.assertThat(verificationRest.requestFails("token=" + expiredRegistration.getVerificationToken(), HttpStatus.FORBIDDEN))
                .hasStatus(HttpStatus.FORBIDDEN)
                .messageContains("expired");

        assertThat(pendingRegistrationRepository.findByUsername(DEFAULT_USERNAME)).isEmpty();
    }

    @Test
    void whenCreateAccount_withDuplicateUsername_inPendingRegistrations_thenConflict() {
        // Arrange
        AccountCreateRequestDto createDto = defaultAccountCreateRequestDto();
        rest.requestSuccessful(createDto);

        // Act & Assert
        ErrorResponseAssert.assertThat(rest.requestFails(createDto.email("different@example.com"), HttpStatus.CONFLICT))
                .hasStatus(HttpStatus.CONFLICT)
                .messageContains("already pending verification");
    }

    @Test
    void whenCreateAccount_withDuplicateEmail_inPendingRegistrations_thenConflict() {
        // Arrange
        AccountCreateRequestDto createDto = defaultAccountCreateRequestDto();
        rest.requestSuccessful(createDto);

        // Act & Assert
        ErrorResponseAssert.assertThat(rest.requestFails(createDto.username("differentuser"), HttpStatus.CONFLICT))
                .hasStatus(HttpStatus.CONFLICT)
                .messageContains("already pending verification");
    }

    @Test
    void whenCreateAccount_withExistingAccountUsername_thenConflict() {
        // Arrange
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

        AccountCreateRequestDto createDto = defaultAccountCreateRequestDto();

        // Act & Assert
        ErrorResponseAssert.assertThat(rest.requestFails(createDto.email("different@example.com"), HttpStatus.CONFLICT))
                .hasStatus(HttpStatus.CONFLICT)
                .messageContains("already exists");
    }

    @Test
    void whenVerifyEmail_butUsernameAlreadyTaken_thenConflict() {
        // Arrange
        AccountCreateRequestDto createDto = defaultAccountCreateRequestDto();
        rest.requestSuccessful(createDto);

        PendingRegistration pending = pendingRegistrationRepository.findByUsername(DEFAULT_USERNAME).orElseThrow();
        String token = pending.getVerificationToken();

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

        // Act & Assert
        ErrorResponseAssert.assertThat(verificationRest.requestFails("token=" + token, HttpStatus.CONFLICT))
                .hasStatus(HttpStatus.CONFLICT)
                .messageContains("already exists");

        assertThat(pendingRegistrationRepository.findByUsername(DEFAULT_USERNAME)).isEmpty();
    }

    @Test
    void whenCleanupExpiredRegistrations_thenExpiredOnesDeleted() {
        // Arrange
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

        // Act
        pendingRegistrationService.cleanupExpiredRegistrations();

        // Assert
        assertThat(pendingRegistrationRepository.findByUsername("expireduser")).isEmpty();
        assertThat(pendingRegistrationRepository.findByUsername("validuser")).isPresent();
    }

    @Test
    void whenPasswordIsEncoded_inPendingRegistration() {
        // Arrange
        AccountCreateRequestDto createDto = defaultAccountCreateRequestDto();

        // Act
        rest.requestSuccessful(createDto);

        // Assert
        PendingRegistration pending = pendingRegistrationRepository.findByUsername(DEFAULT_USERNAME).orElseThrow();
        assertThat(pending.getPasswordHash()).isNotEqualTo(DEFAULT_PASSWORD);
        assertThat(passwordEncoder.matches(DEFAULT_PASSWORD, pending.getPasswordHash())).isTrue();
    }
}
