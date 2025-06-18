package dev.heinisch.menumaestro.properties;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "menumaestro.mail")
public class EmailProperties {

    @NotNull
    private String username;

    @NotNull
    private String password;

    @NotNull
    private String host;

    @NotNull
    private int port;
}
