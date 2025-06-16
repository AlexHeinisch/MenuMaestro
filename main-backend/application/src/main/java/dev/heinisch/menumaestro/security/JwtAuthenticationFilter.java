package dev.heinisch.menumaestro.security;

import dev.heinisch.menumaestro.exceptions.UnauthorizedException;
import dev.heinisch.menumaestro.properties.JwtProperties;
import dev.heinisch.menumaestro.service.JwtService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtProperties jwtProperties;
    private final JwtService jwtService;
    private final HandlerExceptionResolver handlerExceptionResolver;

    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        @NotNull HttpServletResponse response,
        @NotNull FilterChain filterChain
    ) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        final String jwt = authHeader.substring(7);

        final Claims claims;
        try {
            claims = jwtService.extractAllClaims(jwt);
        } catch (Exception e) {
            handlerExceptionResolver.resolveException(
                request,
                response,
                null,
                new UnauthorizedException("Invalid JWT token format or claims extraction failed")
            );
            return;
        }

        if (jwtService.isTokenExpired(jwt)) {
            handlerExceptionResolver.resolveException(
                request,
                response,
                null,
                new UnauthorizedException("Provided token is expired!")
            );
            return;
        }

        if (!claims.getAudience().equals(jwtProperties.getAccountAccessToken().getAudienceClaim())) {
            handlerExceptionResolver.resolveException(
                request,
                response,
                null,
                new UnauthorizedException("Wrong token provided!")
            );
            return;
        }

        String username = claims.getSubject();
        List<SimpleGrantedAuthority> roles = ((List<?>) claims.get(jwtProperties.getAccountAccessToken().getRoleClaimName(), List.class))
            .stream()
            .map(Object::toString)
            .map(SimpleGrantedAuthority::new)
            .toList();

        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
            username,
            null,
            roles
        );
        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authToken);
        filterChain.doFilter(request, response);
    }
}
