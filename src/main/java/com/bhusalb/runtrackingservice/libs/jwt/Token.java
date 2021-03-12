package com.bhusalb.runtrackingservice.libs.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;

import javax.crypto.SecretKey;
import java.util.Date;

@Data
class Token {

    @Setter (AccessLevel.PRIVATE)
    private String token;

    Token (final String token) {
        this.token = token;
    }

    public Token(final TokenInput input) {
        this.token = Jwts.builder()
            .setSubject(input.getSubject())
            .setIssuer(input.getIssuer())
            .setIssuedAt(new Date())
            .setExpiration(new Date(System.currentTimeMillis() + input.getExpirationPeriod()))
            .signWith(input.getSecretKey(), input.getAlgorithm())
            .compact();
    }

    public static Claims parse (final String token, final SecretKey key, final String issuer) {
        return Jwts.parserBuilder()
            .setSigningKey(key)
            .requireIssuer(issuer)
            .build().parseClaimsJws(token)
            .getBody();
    }
}
