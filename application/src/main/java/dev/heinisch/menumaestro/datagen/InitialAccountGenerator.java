package dev.heinisch.menumaestro.datagen;

import dev.heinisch.menumaestro.domain.account.Account;
import dev.heinisch.menumaestro.persistence.AccountRepository;
import dev.heinisch.menumaestro.properties.InitialAccountProperties;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Component
public class InitialAccountGenerator {

    private final InitialAccountProperties initialAccountProperties;
    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;

    @PostConstruct
    @Transactional
    public void generateAccounts() {
        if (!initialAccountProperties.isEnabled()) {
            log.info("Initial account creation disabled!");
            return;
        }
        if (initialAccountProperties.getAccounts().isEmpty()) {
            log.info("No initial accounts found!");
            return;
        }
        log.info("Generating initial accounts...");
        for (var account : initialAccountProperties.getAccounts()) {
            if (accountRepository.findByEmail(account.getEmail()).isPresent()) {
                continue;
            }
            accountRepository.save(new Account(
                            account.getUsername(),
                            account.getEmail(),
                            account.getFirstName(),
                            account.getLastName(),
                            passwordEncoder.encode(account.getPassword()),
                            account.getIsGlobalAdmin(),
                            null,
                            null
                    )
            );
        }
        log.info("Finished generating initial accounts...");
    }
}
