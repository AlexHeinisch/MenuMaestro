package dev.heinisch.menumaestro.properties;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

@Getter
@Setter
@Configuration
@Validated
@ConfigurationProperties(prefix = "menumaestro.security.email-verification")
public class EmailVerificationProperties {
    @NotNull
    private Duration expirationTime;
}
