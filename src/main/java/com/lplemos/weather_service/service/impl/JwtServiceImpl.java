package com.lplemos.weather_service.service.impl;

import com.lplemos.weather_service.service.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.util.Date;

@Service
public class JwtServiceImpl implements JwtService {
    private static final Logger logger = LoggerFactory.getLogger(JwtServiceImpl.class);
    
    @Value("${jwt.secret:defaultSecretKey123456789012345678901234567890}")
    private String secret;
    
    @Value("${jwt.expiration:86400000}") // 24 hours in milliseconds
    private long expiration;
    
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }
    
    @Override
    public String generateToken(String username, String role) {
        return Jwts.builder()
                .claims()
                .add("role", role)
                .and()
                .subject(username)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey(), Jwts.SIG.HS256)
                .compact();
    }
    
    @Override
    public Mono<String> extractUsername(String token) {
        return Mono.fromCallable(() -> {
            try {
                Claims claims = Jwts.parser()
                        .verifyWith(getSigningKey())
                        .build()
                        .parseSignedClaims(token)
                        .getPayload();
                return claims.getSubject();
            } catch (Exception e) {
                logger.error("Error extracting username from token: {}", e.getMessage());
                return null;
            }
        });
    }
    
    @Override
    public Mono<String> extractRole(String token) {
        return Mono.fromCallable(() -> {
            try {
                Claims claims = Jwts.parser()
                        .verifyWith(getSigningKey())
                        .build()
                        .parseSignedClaims(token)
                        .getPayload();
                return claims.get("role", String.class);
            } catch (Exception e) {
                logger.error("Error extracting role from token: {}", e.getMessage());
                return null;
            }
        });
    }
    
    @Override
    public boolean isTokenValid(String token) {
        try {
            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            logger.error("Token validation failed: {}", e.getMessage());
            return false;
        }
    }
} 