package com.adithya.trackfolio.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.security.Key;
import java.util.Date;

/**
 * Generate new tokens
 */
@Component
@Slf4j
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    private Key key;

    @PostConstruct
    public void init() {
        byte[] decoded = Decoders.BASE64.decode(secret);
        this.key = Keys.hmacShaKeyFor(decoded);
    }

    /**
     * Generates JWT
     *
     * @param email          : to encode in token
     * @param isRefreshToken : true for refresh token(valid 7 days) and false for access token(valid 1 hour)
     * @return signed JWT
     */
    public String generateToken(String email, boolean isRefreshToken) {
        long now = System.currentTimeMillis();
        long expiry = isRefreshToken ? 1000L * 60 * 60 * 24 * 7 : 1000L * 60 * 60;

        return Jwts.builder()
                .setSubject(email)
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(now + expiry))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Extract email(subject) from JWT
     *
     * @param token : refresh token
     * @return : extracted email embedded in token's subject
     */
    public String extractEmail(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .getSubject();
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("Failed to extract email from token: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid token. Please login again");
        }
    }

    /**
     * Validate refresh token
     *
     * @param token : refresh token
     * @return : true if valid , false if invalid
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (JwtException e) {
            return false;
        }
    }

    /**
     * Extracts email (subject) from token. Throws JwtException if invalid/expired.
     */
    public String tryExtractEmail(String token) throws JwtException {
        return extractAllClaims(token).getSubject(); // throws raw JwtException
    }

    /**
     * Parses token and returns all claims. Caller must handle JwtException.
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key) // `secretKey` must be your configured key
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}