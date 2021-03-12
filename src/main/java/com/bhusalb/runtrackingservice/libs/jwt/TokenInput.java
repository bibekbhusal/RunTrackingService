package com.bhusalb.runtrackingservice.libs.jwt;

import io.jsonwebtoken.SignatureAlgorithm;
import lombok.Builder;
import lombok.Data;

import javax.crypto.SecretKey;

@Data
@Builder
public class TokenInput {
    private String subject;
    private String issuer;
    private long expirationPeriod;
    private SecretKey secretKey;
    private SignatureAlgorithm algorithm;
}
