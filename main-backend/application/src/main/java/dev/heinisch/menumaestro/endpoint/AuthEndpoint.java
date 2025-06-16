package dev.heinisch.menumaestro.endpoint;

import dev.heinisch.menumaestro.exceptions.ValidationException;
import dev.heinisch.menumaestro.service.AuthService;
import dev.heinisch.menumaestro.validation.PropertyChecker;
import dev.heinisch.menumaestro.validation.UserConstraints;
import lombok.AllArgsConstructor;
import org.openapitools.api.AuthApi;
import org.openapitools.model.LoginRequestDto;
import org.openapitools.model.TokenResponseDto;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1")
public class AuthEndpoint implements AuthApi {

    private final AuthService authService;

    @Override
    public ResponseEntity<TokenResponseDto> login(LoginRequestDto loginRequestDto) {
        validateLoginRequestDto(loginRequestDto);
        return ResponseEntity.ok(authService.login(loginRequestDto));
    }

    @Override
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<TokenResponseDto> refreshRoles(String authorization) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(authService.refreshLogin(username, authorization));
    }

    private void validateLoginRequestDto(LoginRequestDto dto) {
        PropertyChecker.begin()
            .append(UserConstraints.validUsername(dto.getUsername()))
            .append(UserConstraints.validPassword(dto.getPassword()))
            .finalize(ValidationException::fromPropertyChecker);
    }

}
