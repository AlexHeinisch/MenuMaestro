package dev.heinisch.menumaestro.endpoint;

import dev.heinisch.menumaestro.exceptions.ValidationException;
import dev.heinisch.menumaestro.mapper.AccountMapper;
import dev.heinisch.menumaestro.service.AccountService;
import dev.heinisch.menumaestro.validation.PropertyChecker;
import dev.heinisch.menumaestro.validation.UserConstraints;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openapitools.api.AccountsApi;
import org.openapitools.model.AccountCreateRequest;
import org.openapitools.model.AccountEditRequest;
import org.openapitools.model.AccountInfoResponse;
import org.openapitools.model.AccountSummaryListPaginatedResponse;
import org.openapitools.model.ResetPasswordCommitRequest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class AccountEndpoint implements AccountsApi {

    private final AccountService accountService;
    private final AccountMapper accountMapper;

    @Override
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    @Transactional(readOnly = true)
    public ResponseEntity<AccountInfoResponse> getAccountInfo() {
        log.info("GET /accounts/self");
        return ResponseEntity
            .status(HttpStatus.OK)
            .body(accountService.getAccountInfo(SecurityContextHolder.getContext().getAuthentication().getName()));
    }

    @Override
    @Transactional
    public ResponseEntity<AccountInfoResponse> createAccount(AccountCreateRequest accountCreateRequestDto) {
        log.info("POST /accounts");
        log.debug("Request-Body: {}", accountCreateRequestDto);

        validateCreateAccountDto(accountCreateRequestDto);
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(accountService.createAccount(accountCreateRequestDto));
    }

    @Override
    @PreAuthorize("hasRole('ROLE_ADMIN') or (hasRole('ROLE_USER') and #username == principal)")
    public ResponseEntity<AccountInfoResponse> editAccount(String username, AccountEditRequest accountEditRequestDto) {
        log.info("PUT /accounts/{}", username);
        log.debug("Request-Body: {}", accountEditRequestDto);

        validateEditAccountDto(username, accountEditRequestDto);
        return ResponseEntity
            .status(HttpStatus.OK)
            .body(accountService.editAccount(username, accountEditRequestDto));
    }

    @Override
    public ResponseEntity<Void> resetPasswordCommit(String username, String token, ResetPasswordCommitRequest resetPasswordCommitRequest) {
        log.info("PUT /accounts/{}/reset-password/{}", username, token);
        log.debug("Request-Body: {}", resetPasswordCommitRequest);

        validateCommitPasswordReset(username, resetPasswordCommitRequest);

        accountService.commitPasswordReset(username, token, resetPasswordCommitRequest.getPassword());
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<Void> resetPasswordInitiate(String username) {
        log.info("POST /accounts/{}/reset-password", username);

        PropertyChecker.begin()
            .append(UserConstraints.validUsername(username))
            .finalize(ValidationException::fromPropertyChecker);

        accountService.initiatePasswordReset(username);
        return ResponseEntity.noContent().build();
    }

    @Override
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_USER')")
    public ResponseEntity<AccountSummaryListPaginatedResponse> searchAccounts(Integer page, Integer size, String name, Long excludingOrganization) {
        log.info("GET /accounts");
        log.debug("Search-Params: name={} page={} size={}", name, page, size);

        Pageable p = page == null && size == null
            ? Pageable.unpaged()
            : PageRequest.of(page == null ? 0 : page, size == null ? 20 : size);

        return ResponseEntity
            .status(HttpStatus.OK)
            .body(accountMapper.mapPageable(accountService.searchUsersByAnyNameExcludingInOrganization(name, excludingOrganization, p)));
    }

    @Override
    @PreAuthorize("hasRole('ROLE_ADMIN') or (hasRole('ROLE_USER') and #username == principal)")
    public ResponseEntity<Void> deleteAccount(String username) {
        log.info("DELETE /accounts/{}", username);

        accountService.deleteAccount(username);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<Void> confirmEmail(String username, String token) {
        log.info("POST /accounts/{}/confirm-email", username);

        PropertyChecker.begin()
                .append(UserConstraints.validUsername(username))
                .checkThat(token, "token").length(32).notNull().notBlank().done()
                .finalize(ValidationException::fromPropertyChecker);

        accountService.confirmEmail(username, token);

        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<Void> resendToken(String username) {
        log.info("GET /accounts/{}/confirm-email", username);

        PropertyChecker.begin()
                .append(UserConstraints.validUsername(username))
                .finalize(ValidationException::fromPropertyChecker);

        accountService.sendConfirmationToken(username);

        return ResponseEntity.noContent().build();
    }

    private void validateCommitPasswordReset(String username, ResetPasswordCommitRequest dto) {
        PropertyChecker.begin()
            .append(UserConstraints.validUsername(username))
            .append(UserConstraints.validPassword(dto.getPassword()))
            .finalize(ValidationException::fromPropertyChecker);
    }

    private void validateCreateAccountDto(AccountCreateRequest dto) {
        PropertyChecker.begin()
            .append(UserConstraints.validUsername(dto.getUsername()))
            .append(UserConstraints.validEmail(dto.getEmail()))
            .append(UserConstraints.validFirstName(dto.getFirstName()))
            .append(UserConstraints.validLastName(dto.getLastName()))
            .append(UserConstraints.validPassword(dto.getPassword()))
            .finalize(ValidationException::fromPropertyChecker);
    }

    private void validateEditAccountDto(String username, AccountEditRequest dto) {
        PropertyChecker.begin()
            .append(UserConstraints.validUsername(username))
            .append(UserConstraints.validEmailNullable(dto.getEmail()))
            .append(UserConstraints.validFirstNameNullable(dto.getFirstName()))
            .append(UserConstraints.validLastNameNullable(dto.getLastName()))
            .append(UserConstraints.validPasswordNullable(dto.getNewPassword()))
            .append(UserConstraints.validPassword(dto.getOldPassword()))
            .finalize(ValidationException::fromPropertyChecker);
    }

}
