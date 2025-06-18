package dev.heinisch.menumaestro.properties;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "menumaestro.security.jwt")
public class JwtProperties {

    @NotNull
    @Length(min = 30)
    private String secretKey;

    @NotNull
    @Valid
    private AccountAccessTokenProperties accountAccessToken;

    @NotNull
    @Valid
    private ShoppingListShareTokenProperties shoppingListShareToken;

    @Getter
    @Setter
    @Validated
    public static class AccountAccessTokenProperties {
        private String roleClaimName;
        private Duration expirationTime;
        private String audienceClaim;
    }

    @Getter
    @Setter
    @Validated
    public static class ShoppingListShareTokenProperties {
        private Duration expirationTime;
        private String audienceClaim;
    }

}
