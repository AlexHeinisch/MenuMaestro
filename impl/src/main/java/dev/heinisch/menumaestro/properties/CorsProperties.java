package dev.heinisch.menumaestro.properties;


import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "menumaestro.cors")
public class CorsProperties {

    @NotNull
    private List<String> allowedCrossOriginPatterns;

}
