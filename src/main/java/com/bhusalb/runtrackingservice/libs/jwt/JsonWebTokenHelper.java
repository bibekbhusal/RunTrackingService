package com.bhusalb.runtrackingservice.libs.jwt;

import com.bhusalb.runtrackingservice.models.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.crypto.SecretKey;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.Optional;

@Component
@Slf4j
public class JsonWebTokenHelper {

    @Value ("${jwt.issuer}")
    private String issuer;

    @Value ("${jwt.expiration-period}")
    private int expirationPeriod;

    @Value ("${jwt.secret-file-name:jwt-secret}")
    private String jwtSecretFileName;

    private SecretKey key;

    @PostConstruct
    void completeInit () {
        try {
            log.info("Extracting secret key from file: {}.", jwtSecretFileName);
            final StringBuilder builder = new StringBuilder();
            final List<String> lines = Files.readAllLines(new File(jwtSecretFileName).toPath());
            lines.forEach(builder::append);
            key = Keys.hmacShaKeyFor(builder.toString().getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String generateToken (final User user) {
        final TokenInput input = TokenInput.builder()
            .subject(user.getId().toString())
            .issuer(issuer)
            .expirationPeriod(expirationPeriod)
            .secretKey(key)
            .algorithm(SignatureAlgorithm.HS512)
            .build();
        return new Token(input).getToken();
    }

    public String getUserId (final String token) {
        final Optional<Claims> claims = parseClaims(token);
        return claims.map(Claims::getSubject).orElse(null);
    }

    private Optional<Claims> parseClaims (final String token) {
        try {
            return Optional.of(Token.parse(token, key, issuer));
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
