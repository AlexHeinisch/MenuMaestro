package at.codemaestro.properties;

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
@ConfigurationProperties(prefix = "security.password-reset")
public class PasswordResetProperties {
    @NotNull
    private Duration expirationTime;
}
