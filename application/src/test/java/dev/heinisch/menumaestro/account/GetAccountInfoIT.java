package dev.heinisch.menumaestro.account;

import dev.heinisch.menumaestro.BaseWebIntegrationTest;
import dev.heinisch.menumaestro.utils.ErrorResponseAssert;
import dev.heinisch.menumaestro.utils.RestHelper;
import io.restassured.http.Headers;
import io.restassured.http.Method;
import jakarta.annotation.PostConstruct;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openapitools.model.AccountCreateRequest;
import org.openapitools.model.AccountInfoResponse;
import org.springframework.http.HttpStatus;

import java.util.List;

import static dev.heinisch.menumaestro.utils.test_constants.DefaultAccountTestData.DEFAULT_USERNAME;
import static dev.heinisch.menumaestro.utils.test_constants.DefaultAccountTestData.defaultAccountCreateRequestDto;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

public class GetAccountInfoIT extends BaseWebIntegrationTest {

    private RestHelper.BlankRestHelper<AccountInfoResponse> rest;

    @PostConstruct
    private void initRestHelper() {
        rest = new RestHelper.BlankRestHelper<>(
                AccountInfoResponse.class,
                this.generateValidAuthorizationHeader(DEFAULT_USERNAME, List.of("ROLE_USER")),
                Method.GET,
                URI + "/self",
                HttpStatus.OK
        );
    }

    @Override
    protected String getBasePath() {
        return "/accounts";
    }

    @BeforeEach
    void setup() {
        var dto = defaultAccountCreateRequestDto();
        accountService.createAccount(dto);
        accountService.confirmEmail(
                dto.getUsername(),
                pendingAccountRepository.findByEmail(dto.getEmail()).orElseThrow().getConfirmationToken()
        );
    }

    @Test
    void whenAuthenticatedUser_requestsAccountInfo_thenReturnAccountInfo() {
        var dto = rest.requestSuccessful();
        assertInfoDtoEqualsCreateDto(dto, defaultAccountCreateRequestDto());
    }

    @Test
    void whenUnauthenticatedUser_requestsAccountInfo_thenReturnUnauthorized() {
        var errorResponse = rest.requestFails(HttpStatus.FORBIDDEN, new Headers());
        ErrorResponseAssert.assertThat(errorResponse)
                .hasStatus(HttpStatus.FORBIDDEN);
    }

    private void assertInfoDtoEqualsCreateDto(AccountInfoResponse accountInfoDto, AccountCreateRequest accountCreateRequestDto) {
        assertAll(
                "Verify accountInfoDto matches accountCreateRequestDto",
                () -> assertThat(accountInfoDto.getLastName()).isEqualTo(accountCreateRequestDto.getLastName()),
                () -> assertThat(accountInfoDto.getFirstName()).isEqualTo(accountCreateRequestDto.getFirstName()),
                () -> assertThat(accountInfoDto.getUsername()).isEqualTo(accountCreateRequestDto.getUsername()),
                () -> assertThat(accountInfoDto.getEmail()).isEqualTo(accountCreateRequestDto.getEmail()),
                () -> assertThat(accountInfoDto.getIsGlobalAdmin()).isNotNull()
        );
    }
}
