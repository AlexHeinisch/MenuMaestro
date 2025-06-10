package at.codemaestro.properties;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "initial-accounts")
public class InitialAccountProperties {

    private List<@Valid SingleAccount> accounts;

    @Getter
    @Setter
    @Validated
    public static class SingleAccount {
        @NotBlank
        private String username, firstName, lastName;
        @NotBlank @Email
        private String email;
        @Length(min = 6)
        private String password;
        @NotNull
        private Boolean isGlobalAdmin;
    }
}
