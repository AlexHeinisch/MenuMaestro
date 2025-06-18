package dev.heinisch.menumaestro.websocket;

import dev.heinisch.menumaestro.properties.CorsProperties;
import dev.heinisch.menumaestro.properties.WebsocketProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.converter.MessageConverter;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import java.util.List;

@Slf4j
@Configuration
@RequiredArgsConstructor
@EnableWebSocketMessageBroker
public class WebsocketConfiguration implements WebSocketMessageBrokerConfigurer {

    private final ShoppingListChannelInterceptor interceptor;
    private final WebsocketProperties websocketProperties;
    private final CorsProperties corsProperties;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker(websocketProperties.getTopics().getShoppingListTopicPrefix()); // Topic for broadcasting messages
        config.setApplicationDestinationPrefixes(websocketProperties.getApplicationPrefix()); // Prefix for message mapping
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint(websocketProperties.getWebsocketPath())
            .setAllowedOriginPatterns(corsProperties.getAllowedCrossOriginPatterns().toArray(new String[0])).withSockJS();
        registry.addEndpoint(websocketProperties.getWebsocketPath())
            .setAllowedOriginPatterns(corsProperties.getAllowedCrossOriginPatterns().toArray(new String[0]));
    }

    @Override
    public boolean configureMessageConverters(List<MessageConverter> messageConverters) {
        // Add a Jackson-based message converter
        messageConverters.add(new MappingJackson2MessageConverter());
        return true; // Returning true to indicate that default converters are overridden
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(interceptor);
    }
}
