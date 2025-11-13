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
        FILTER FOR SUBSCRIBE STATEMENTS + CORRECT TOPIC
           ============================================ */

        // only prevent unauthorized subscriptions
        if (accessor.getCommand() != StompCommand.SUBSCRIBE) {
            return message;
        }
        // only care about shopping list topics
        if (accessor.getDestination() == null || !accessor.getDestination().startsWith(websocketProperties.getTopics().getShoppingListTopicPrefix())) {
            return message;
        }
        /* ============================================ */

        log.debug("WebSocket SUBSCRIBE request received for shopping list topic");

        // Extract shopping list ID from destination
        final Long shoppingListId;
        try {
            String destination = accessor.getDestination();
            if (destination == null) {
                log.warn("WebSocket connection rejected: No destination provided");
                throw ForbiddenException.generic();
            }
            String[] parts = destination.split("/");
            shoppingListId = Long.parseLong(parts[parts.length - 1]);
        } catch (NumberFormatException e) {
            log.warn("WebSocket connection rejected: Invalid shopping list ID in destination");
            throw ForbiddenException.generic();
        }

        List<String> tokenList = accessor.getNativeHeader("X-Authorization");
        if (tokenList == null || tokenList.isEmpty() || tokenList.getFirst() == null) {
            log.warn("WebSocket connection rejected: No X-Authorization header provided");
            throw ForbiddenException.generic();
        }

        final String token = tokenList.getFirst();
        final Claims claims;
        try {
            claims = jwtService.extractAllClaims(token);
        } catch (Exception e) {
            log.warn("WebSocket connection rejected: Invalid JWT token - {}", e.getMessage());
            throw new UnauthorizedException("Invalid JWT token format or claims extraction failed");
        }

        if (jwtService.isTokenExpired(token)) {
            log.warn("WebSocket connection rejected: Token expired");
            throw new UnauthorizedException("Provided token is expired!");
        }

        if(claims.getAudience().contains(jwtProperties.getAccountAccessToken().getAudienceClaim())) {

            // extract roles & username
            final List<String> roles = ((List<?>) claims.get(jwtProperties.getAccountAccessToken().getRoleClaimName(), List.class))
                .stream().map(Object::toString).toList();
            final String username = claims.getSubject();

            // check if logged-in user has permission
            if (!roles.contains("ROLE_ADMIN") &&
                !organizationService.hasPermissionsForShoppingList(shoppingListId, username, OrganizationRole.MEMBER.name().toUpperCase())
            ) {
                log.warn("WebSocket connection rejected: User '{}' lacks permissions for shopping list {}", username, shoppingListId);
                throw ForbiddenException.generic();
            }

            log.info("WebSocket connection established: User '{}' connected to shopping list {}", username, shoppingListId);
            // logged-in user has permissions
            return message;
        }

        if(claims.getAudience().contains(jwtProperties.getShoppingListShareToken().getAudienceClaim())) {
            if (!jwtService.isValidShoppingListToken(shoppingListId, token)) {
                log.warn("WebSocket connection rejected: Invalid shopping list share token for shopping list {}", shoppingListId);
                throw ForbiddenException.generic();
            }

            log.info("WebSocket connection established: Anonymous user connected via share token to shopping list {}", shoppingListId);
            // shopping list share token is valid
            return message;
        }

        log.warn("WebSocket connection rejected: Token has wrong audience claim");
        // token audience does not correspond to any permitted token here
        throw new UnauthorizedException("Wrong token provided!");
    }

}
