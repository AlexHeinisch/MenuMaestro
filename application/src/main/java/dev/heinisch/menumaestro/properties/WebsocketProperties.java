package dev.heinisch.menumaestro.properties;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "menumaestro.websocket")
public class WebsocketProperties {

    @NotNull
    private String applicationPrefix;

    @NotNull
    private String websocketPath;

    @NotNull @Valid
    private TopicProperties topics;


    @Getter @Setter @Validated
    public static class TopicProperties {

        private String shoppingListTopicPrefix;
    }
}
