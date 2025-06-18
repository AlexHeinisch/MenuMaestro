package dev.heinisch.menumaestro.integration_test;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Use the weakest setting of the production component in tests for faster test runs
 */
@Slf4j
@Configuration
public class TestPasswordEncoderConfiguration {
    @Primary
    @Bean
    PasswordEncoder testPasswordEncoder() {
        log.info("Replacing password encoder for test!");
        return new BCryptPasswordEncoder(4);
    }
}
