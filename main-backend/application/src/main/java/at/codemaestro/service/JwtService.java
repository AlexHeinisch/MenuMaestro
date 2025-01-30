package at.codemaestro.service;

import at.codemaestro.domain.account.Account;
import at.codemaestro.domain.organization.OrganizationAccountRelation;
import at.codemaestro.exceptions.ApplicationException;
import at.codemaestro.properties.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

@RequiredArgsConstructor
@Service
public class JwtService {

    private final JwtProperties jwtProperties;


    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public String generateAccountAccessToken(Account account, List<OrganizationAccountRelation> organizationRoles) {
        return generateAccountAccessToken(account, organizationRoles,
                new Date(System.currentTimeMillis() + jwtProperties.getAccountAccessToken().getExpirationTime().toMillis()));
    }

    public String generateAccountAccessToken(Account account, List<OrganizationAccountRelation> organizationRoles, Date expiration) {
        HashMap<String, Object> roles = new HashMap<>();
        roles.put(jwtProperties.getAccountAccessToken().getRoleClaimName(),
                Stream.concat(
                        account.getAuthorities().stream().map(GrantedAuthority::getAuthority),
                        organizationRoles.stream().map(oar -> String.format("ORG::%d::%s", oar.getId().getOrganizationId(), oar.getRole().name()))
                ).toList()
        );
        return generateAccountAccessToken(roles, account.getUsername(), expiration);
    }

    public String generateAccountAccessToken(Map<String, Object> extraClaims, String username, Date expiration) {
        return buildToken(extraClaims, username, jwtProperties.getAccountAccessToken().getAudienceClaim(), expiration);
    }

    public String generateShoppingListAccessToken(Long shoppingListId) {
        return buildToken(new HashMap<>(), shoppingListId.toString(), jwtProperties.getShoppingListShareToken().getAudienceClaim(),
                new Date(System.currentTimeMillis() + jwtProperties.getShoppingListShareToken().getExpirationTime().toMillis()));
    }

    private String buildToken(
            Map<String, Object> extraClaims,
            String subject,
            String audience,
            Date expiration
    ) {
        try {
            return Jwts
                    .builder()
                    .setClaims(extraClaims)
                    .setSubject(subject)
                    .setAudience(audience)
                    .setIssuedAt(new Date(System.currentTimeMillis()))
                    .setExpiration(expiration)
                    .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                    .compact();
        } catch (Exception e) {
            // should not happen since key is validated at startup time
            throw new ApplicationException("The backend uses a weak key!", List.of(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public boolean isValidShoppingListToken(Long shoppingListId, String token) {
        try {
            if (isTokenExpired(token)) return false;
            var claims = extractAllClaims(token);
            if (!claims.getAudience().equals(jwtProperties.getShoppingListShareToken().getAudienceClaim()))
                return false;
            return claims.getSubject().equals(shoppingListId.toString());
        } catch (Exception e) {
            return false; // users passed something
        }
    }

    public boolean isValidShoppingListToken(String token) {
        try {
            if (isTokenExpired(token)) return false;
            var claims = extractAllClaims(token);
            return claims.getAudience().equals(jwtProperties.getShoppingListShareToken().getAudienceClaim());
        } catch (Exception e) {
            return false; // users passed something
        }
    }

    public boolean isTokenExpired(String token) {
        return extractClaim(token, Claims::getExpiration).before(new Date());
    }

    public Claims extractAllClaims(String token) {
        return Jwts
                .parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Key getSignInKey() {
        byte[] keyBytes = jwtProperties.getSecretKey().getBytes();
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
