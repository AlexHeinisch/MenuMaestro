package dev.heinisch.menumaestro.websocket;

import dev.heinisch.menumaestro.domain.organization.OrganizationRole;
import dev.heinisch.menumaestro.exceptions.ForbiddenException;
import dev.heinisch.menumaestro.exceptions.UnauthorizedException;
import dev.heinisch.menumaestro.properties.JwtProperties;
import dev.heinisch.menumaestro.properties.WebsocketProperties;
import dev.heinisch.menumaestro.service.JwtService;
import dev.heinisch.menumaestro.service.OrganizationService;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class ShoppingListChannelInterceptor implements ChannelInterceptor {

    private final JwtService jwtService;
    private final JwtProperties jwtProperties;
    private final OrganizationService organizationService;
    private final WebsocketProperties websocketProperties;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {

        final StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

        /* ============================================
        FILTER FOR CONNECT STATEMENTS + CORRECT TOPIC
           ============================================ */

        // only prevent unauthorized connections
        if (accessor.getCommand() != StompCommand.CONNECT) {
            return message;
        }
        // only care about shopping list topics
        if (accessor.getDestination() != null && !accessor.getDestination().startsWith(websocketProperties.getTopics().getShoppingListTopicPrefix())) {
            return message;
        }
        /* ============================================ */



        List<String> tokenList = accessor.getNativeHeader("X-Authorization");
        if (tokenList == null || tokenList.isEmpty() || tokenList.getFirst() == null) {
            throw ForbiddenException.generic();
        }

        final String token = tokenList.getFirst();
        final Claims claims;
        try {
            claims = jwtService.extractAllClaims(token);
        } catch (Exception e) {
            throw new UnauthorizedException("Invalid JWT token format or claims extraction failed");
        }

        if (jwtService.isTokenExpired(token)) {
            throw new UnauthorizedException("Provided token is expired!");
        }

        if(claims.getAudience().equals(jwtProperties.getAccountAccessToken().getAudienceClaim())) {

            // extract roles & username
            final List<String> roles = ((List<?>) claims.get(jwtProperties.getAccountAccessToken().getRoleClaimName(), List.class))
                .stream().map(Object::toString).toList();
            final String username = claims.getSubject();

            // check if logged-in user has permission
            if (!roles.contains("ROLE_ADMIN") &&
                !organizationService.hasPermissionsForShoppingList(1L, username, OrganizationRole.MEMBER.name().toUpperCase())
            ) {
                throw ForbiddenException.generic();
            }

            // logged-in user has permissions
            return message;
        }

        if(claims.getAudience().equals(jwtProperties.getShoppingListShareToken().getAudienceClaim())) {
            if (!jwtService.isValidShoppingListToken(1L, token)) {
                throw ForbiddenException.generic();
            }

            // shopping list share token is valid
            return message;
        }

        // token audience does not correspond to any permitted token here
        throw new UnauthorizedException("Wrong token provided!");
    }

}
