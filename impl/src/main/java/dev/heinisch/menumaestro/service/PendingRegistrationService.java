package dev.heinisch.menumaestro.service;

import dev.heinisch.menumaestro.domain.account.Account;
import dev.heinisch.menumaestro.domain.account.PendingRegistration;
import dev.heinisch.menumaestro.exceptions.ConflictException;
import dev.heinisch.menumaestro.exceptions.ForbiddenException;
import dev.heinisch.menumaestro.exceptions.NotFoundException;
import dev.heinisch.menumaestro.mapper.AccountMapper;
import dev.heinisch.menumaestro.persistence.AccountRepository;
import dev.heinisch.menumaestro.persistence.PendingRegistrationRepository;
import dev.heinisch.menumaestro.properties.EmailVerificationProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openapitools.model.AccountCreateRequestDto;
import org.openapitools.model.AccountInfoDto;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Service
public class PendingRegistrationService {

    private final EmailVerificationProperties emailVerificationProperties;
    private final PendingRegistrationRepository pendingRegistrationRepository;
    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final AccountMapper accountMapper;

    @Transactional
    public void createPendingRegistration(AccountCreateRequestDto dto) {

        // Check if username or email already exists in Account table
        if (accountRepository.findById(dto.getUsername()).isPresent()) {
            throw new ConflictException(String.format("Username '%s' already exists!", dto.getUsername()));
        }
        if (accountRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new ConflictException(String.format("Email '%s' already exists!", dto.getEmail()));
        }

        // Check if username or email already exists in PendingRegistration table
        if (pendingRegistrationRepository.findByUsername(dto.getUsername()).isPresent()) {
            throw new ConflictException(String.format("Username '%s' is already pending verification!", dto.getUsername()));
        }
        if (pendingRegistrationRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new ConflictException(String.format("Email '%s' is already pending verification!", dto.getEmail()));
        }

        String verificationToken = UUID.randomUUID().toString();
        Instant now = Instant.now();
        Instant expiresAt = now.plus(emailVerificationProperties.getExpirationTime());

        PendingRegistration pendingRegistration = PendingRegistration.builder()
                .username(dto.getUsername())
                .email(dto.getEmail())
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .passwordHash(passwordEncoder.encode(dto.getPassword()))
                .verificationToken(verificationToken)
                .createdAt(now)
                .expiresAt(expiresAt)
                .build();

        pendingRegistrationRepository.save(pendingRegistration);
        emailService.sendEmailVerification(dto.getEmail(), verificationToken);

        log.info("Created pending registration for username: {}, email: {}", dto.getUsername(), dto.getEmail());
    }

    @Transactional
    public AccountInfoDto verifyEmailAndCreateAccount(String token) {

        PendingRegistration pendingRegistration = pendingRegistrationRepository.findByVerificationToken(token)
                .orElseThrow(() -> new NotFoundException("Invalid or expired verification token!"));

        if (pendingRegistration.isExpired()) {
            pendingRegistrationRepository.delete(pendingRegistration);
            throw new ForbiddenException("Verification token has expired!");
        }

        // Double-check that username and email are still available
        if (accountRepository.findById(pendingRegistration.getUsername()).isPresent()) {
            pendingRegistrationRepository.delete(pendingRegistration);
            throw new ConflictException(String.format("Username '%s' already exists!", pendingRegistration.getUsername()));
        }
        if (accountRepository.findByEmail(pendingRegistration.getEmail()).isPresent()) {
            pendingRegistrationRepository.delete(pendingRegistration);
            throw new ConflictException(String.format("Email '%s' already exists!", pendingRegistration.getEmail()));
        }

        // Create the actual account
        Account account = Account.builder()
                .username(pendingRegistration.getUsername())
                .email(pendingRegistration.getEmail())
                .firstName(pendingRegistration.getFirstName())
                .lastName(pendingRegistration.getLastName())
                .passwordHash(pendingRegistration.getPasswordHash())
                .isGlobalAdmin(false)
                .build();

        accountRepository.save(account);
        pendingRegistrationRepository.delete(pendingRegistration);

        log.info("Successfully verified email and created account for username: {}", account.getUsername());

        return accountMapper.toInfoDto(account);
    }

    @Transactional
    public void cleanupExpiredRegistrations() {
        List<PendingRegistration> expired = pendingRegistrationRepository.findAllByExpiresAtBefore(Instant.now());
        if (!expired.isEmpty()) {
            pendingRegistrationRepository.deleteAll(expired);
            log.info("Cleaned up {} expired pending registrations", expired.size());
        }
    }
}
