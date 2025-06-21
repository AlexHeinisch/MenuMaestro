package dev.heinisch.menumaestro.service;

import dev.heinisch.menumaestro.domain.account.Account;
import dev.heinisch.menumaestro.domain.organization.OrganizationAccountRelation;
import dev.heinisch.menumaestro.exceptions.UnauthorizedException;
import dev.heinisch.menumaestro.exceptions.ValidationException;
import dev.heinisch.menumaestro.persistence.AccountRepository;
import dev.heinisch.menumaestro.persistence.OrganizationAccountRelationRepository;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openapitools.model.AccessTokenDto;
import org.openapitools.model.LoginRequestDto;
import org.openapitools.model.TokenResponseDto;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneOffset;
import java.util.Date;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final AccountRepository accountRepository;
    private final OrganizationAccountRelationRepository organizationAccountRelationRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Transactional
    public TokenResponseDto login(LoginRequestDto dto) {
        Account account = accountRepository.findById(dto.getUsername())
        .orElseThrow(() -> new UnauthorizedException("Username or password is incorrect."));

        if (!passwordEncoder.matches(dto.getPassword(), account.getPasswordHash())) {
            throw new UnauthorizedException("Username or password is incorrect.");
        }

        List<OrganizationAccountRelation> orgRoles = organizationAccountRelationRepository.findByUsername(account.getUsername());

        String token = jwtService.generateAccountAccessToken(account, orgRoles);
        return new TokenResponseDto()
            .accessToken(new AccessTokenDto()
                .token(token)
                .expiryDate(jwtService.extractClaim(token, Claims::getExpiration).toInstant().atOffset(ZoneOffset.UTC))
            );
    }

    @Transactional(readOnly = true)
    public TokenResponseDto refreshLogin(String username, String authHeader) {
        Account account = accountRepository.findById(username)
                .orElseThrow(() -> new UnauthorizedException("Account not found, likely deleted."));
        List<OrganizationAccountRelation> orgRoles = organizationAccountRelationRepository.findByUsername(account.getUsername());
        if (authHeader.startsWith("Bearer ")) {
            authHeader = authHeader.substring(7);
        } else {
            throw new ValidationException("No bearer-auth header found.");
        }
        Date expiry = Date.from(jwtService.extractClaim(authHeader, Claims::getExpiration).toInstant());
        String token = jwtService.generateAccountAccessToken(account, orgRoles, expiry);
        return new TokenResponseDto()
                .accessToken(new AccessTokenDto()
                        .token(token)
                        .expiryDate(jwtService.extractClaim(token, Claims::getExpiration).toInstant().atOffset(ZoneOffset.UTC))
                );
    }

}
