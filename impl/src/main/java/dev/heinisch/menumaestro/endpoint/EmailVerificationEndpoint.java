package dev.heinisch.menumaestro.endpoint;

import dev.heinisch.menumaestro.service.PendingRegistrationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openapitools.model.AccountInfoDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/accounts")
public class EmailVerificationEndpoint {

    private final PendingRegistrationService pendingRegistrationService;

    @GetMapping("/verification")
    public ResponseEntity<AccountInfoDto> verifyEmail(@RequestParam("token") String token) {
        log.info("GET /accounts/verification with token");

        AccountInfoDto accountInfo = pendingRegistrationService.verifyEmailAndCreateAccount(token);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(accountInfo);
    }
}
