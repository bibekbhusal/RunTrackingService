package com.bhusalb.runtrackingservice.libs.jwt;

import com.bhusalb.runtrackingservice.models.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Optional;

@Component
@Slf4j
// TODO: Define an auth token provider interface and implement it here.
public class JsonWebTokenHelper {

    // TODO: Pull from ENV
    private static final String JWT_ISSUER = "com.bhusalb";
    // TODO: Reduce it and pull from ENV
    private static final int VALIDITY_PERIOD_MILLIS = 1000 * 60 * 60 * 24; // 1 day
    // TODO: Retrieve from ENV
    private static final SecretKey KEY = Keys.hmacShaKeyFor(("zdtlD3JK56m6wTTgsNFhqzjqPzdtlD3JK56m6wTTgsNFhqzjqPzdtl" +
        "D3JK56m6wTTgsNFhqzjqPzdtlD3JK56m6wTTgsNFhqzjqP").getBytes(StandardCharsets.UTF_8));

    public String generateToken (final User user) {
        return Jwts.builder()
            .setSubject(String.format("%s,%s", user.getId(), user.getEmail()))
            .setIssuer(JWT_ISSUER)
            .setIssuedAt(new Date())
            .setExpiration(new Date(System.currentTimeMillis() + VALIDITY_PERIOD_MILLIS))
            .signWith(KEY, SignatureAlgorithm.HS512)
            .compact();
    }

    public String getUserId (final String token) {
        final Optional<Claims> claims = parseClaims(token);
        return claims.map(Claims::getSubject)
            .map(sub -> sub.split(","))
            .map(tokens -> tokens[0])
            .orElse(null);
    }

    public String getEmail (final String token) {
        final Optional<Claims> claims = parseClaims(token);
        return claims.map(Claims::getSubject)
            .map(sub -> sub.split(","))
            .map(tokens -> tokens[1])
            .orElse(null);
    }

    public Date getExpirationDate (final String token) {
        final Optional<Claims> claims = parseClaims(token);
        return claims.map(Claims::getExpiration).orElse(null);
    }

    public boolean validate (final String token) {
        return parseClaims(token).isPresent();
    }

    private Optional<Claims> parseClaims (final String token) {

        try {
            final Claims claims = Jwts.parserBuilder()
                .setSigningKey(KEY)
                .requireIssuer(JWT_ISSUER)
                .build().parseClaimsJws(token)
                .getBody();
            return Optional.of(claims);
        } catch (SignatureException ex) {
            log.error("Invalid JWT signature - {}", ex.getMessage());
        } catch (MalformedJwtException ex) {
            log.error("Invalid JWT token - {}", ex.getMessage());
        } catch (ExpiredJwtException ex) {
            log.error("Expired JWT token - {}", ex.getMessage());
        } catch (UnsupportedJwtException ex) {
            log.error("Unsupported JWT token - {}", ex.getMessage());
        } catch (IllegalArgumentException ex) {
            log.error("JWT claims string is empty - {}", ex.getMessage());
        }
        return Optional.empty();
    }
}
