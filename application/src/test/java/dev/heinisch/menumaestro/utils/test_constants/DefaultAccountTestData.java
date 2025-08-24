package dev.heinisch.menumaestro.utils.test_constants;

import dev.heinisch.menumaestro.domain.account.Account;
import org.openapitools.model.AccountCreateRequest;
import org.openapitools.model.AccountEditRequest;
import org.openapitools.model.LoginRequest;

import java.time.Instant;

public class DefaultAccountTestData {

    public static final String DEFAULT_USERNAME = "username";
    public static final String DEFAULT_EMAIL = "test@example.com";
    public static final String DEFAULT_FIRST_NAME = "Max";
    public static final String DEFAULT_LAST_NAME = "Mustermann";
    public static final String DEFAULT_PASSWORD = "hallo123";
    public static final Instant DEFAULT_PASSWORD_RESET_VALID_UNTIL_DATE = Instant.now().plusSeconds(300);
    public static final String DEFAULT_PASSWORD_RESET_TOKEN = "token";

    public static final String DEFAULT_NEW_EMAIL = "mm@example.com";
    public static final String DEFAULT_NEW_FIRST_NAME = "Michelle";
    public static final String DEFAULT_NEW_LAST_NAME = "Spring";
    public static final String DEFAULT_NEW_PASSWORD = "hallo1234";

    public static final String DEFAULT_USERNAME_2 = "other";
    public static final String DEFAULT_EMAIL_2 = "other@example.com";
    public static final String DEFAULT_FIRST_NAME_2 = "Alina";
    public static final String DEFAULT_LAST_NAME_2 = "Alles";

    public static final String DEFAULT_USERNAME_3 = "some";
    public static final String DEFAULT_EMAIL_3 = "some@example.com";
    public static final String DEFAULT_FIRST_NAME_3 = "Lucas";
    public static final String DEFAULT_LAST_NAME_3 = "Lol";

    public static Account defaultAccount() {
        return Account.builder()
                .username(DEFAULT_USERNAME)
                .email(DEFAULT_EMAIL)
                .firstName(DEFAULT_FIRST_NAME)
                .lastName(DEFAULT_LAST_NAME)
                .isGlobalAdmin(false)
                .passwordHash("") //TODO make better pw hash?
                .build();
    }

    public static Account defaultAccount2() {
        return Account.builder()
                .username(DEFAULT_USERNAME_2)
                .email(DEFAULT_EMAIL_2)
                .firstName(DEFAULT_FIRST_NAME_2)
                .lastName(DEFAULT_LAST_NAME_2)
                .isGlobalAdmin(false)
                .passwordHash("") //TODO make better pw hash?
                .build();
    }

    public static Account defaultAccount3() {
        return Account.builder()
                .username(DEFAULT_USERNAME_3)
                .email(DEFAULT_EMAIL_3)
                .firstName(DEFAULT_FIRST_NAME_3)
                .lastName(DEFAULT_LAST_NAME_3)
                .isGlobalAdmin(false)
                .passwordHash("") //TODO make better pw hash?
                .build();
    }

    public static AccountCreateRequest defaultAccountCreateRequestDto() {
        return new AccountCreateRequest()
                .username(DEFAULT_USERNAME)
                .email(DEFAULT_EMAIL)
                .firstName(DEFAULT_FIRST_NAME)
                .lastName(DEFAULT_LAST_NAME)
                .password(DEFAULT_PASSWORD);
    }

    public static AccountEditRequest defaultEditRequestDto() {
        return new AccountEditRequest()
                .email(DEFAULT_NEW_EMAIL)
                .firstName(DEFAULT_NEW_FIRST_NAME)
                .lastName(DEFAULT_NEW_LAST_NAME)
                .newPassword(DEFAULT_NEW_PASSWORD)
                .oldPassword(DEFAULT_PASSWORD);
    }

    public static LoginRequest defaultLoginRequestDto() {
        return new LoginRequest()
                .username(DEFAULT_USERNAME)
                .password(DEFAULT_PASSWORD);
    }

}
