package com.wordle.wordle.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtUtil {

    private final SecretKey key;
    private final long expiryMillis;

    public JwtUtil(@Value("${wordle.jwt.secret}") String secret,
                   @Value("${wordle.jwt.expiry-hours}") long expiryHours) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expiryMillis = expiryHours * 3600L * 1000L;
    }

    public String generateToken(JwtPrincipal principal) {
        Date now = new Date();
        return Jwts.builder()
                .subject(principal.username())
                .claim("uid", principal.userId())
                .claim("role", principal.role())
                .issuedAt(now)
                .expiration(new Date(now.getTime() + expiryMillis))
                .signWith(key)
                .compact();
    }

    public JwtPrincipal parse(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        Long uid = claims.get("uid", Number.class).longValue();
        String username = claims.getSubject();
        String role = claims.get("role", String.class);
        return new JwtPrincipal(uid, username, role);
    }
}