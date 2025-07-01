package com.lplemos.weather_service.service;

import reactor.core.publisher.Mono;

public interface JwtService {
    String generateToken(String username, String role);
    Mono<String> extractUsername(String token);
    Mono<String> extractRole(String token);
    boolean isTokenValid(String token);
} 