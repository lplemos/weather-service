package com.lplemos.weather_service.config;

import com.lplemos.weather_service.service.JwtService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.Collections;

@Component
@Order(-100)
public class JwtAuthenticationFilter implements WebFilter {
    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    
    private final JwtService jwtService;
    
    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
        logger.info("JwtAuthenticationFilter: Constructor called - filter initialized");
    }
    
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String path = exchange.getRequest().getPath().value();
        logger.info("JwtAuthenticationFilter: Processing request to {}", path);
        
        // Skip authentication for public endpoints
        if (isPublicEndpoint(path)) {
            logger.info("JwtAuthenticationFilter: Public endpoint detected, skipping authentication");
            return chain.filter(exchange);
        }
        
        String authHeader = exchange.getRequest().getHeaders().getFirst("Authorization");
        logger.info("JwtAuthenticationFilter: Authorization header: {}", authHeader != null ? "present" : "null");
        
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            logger.info("JwtAuthenticationFilter: No Bearer token found, continuing without authentication");
            return chain.filter(exchange);
        }
        
        String token = authHeader.substring(7);
        logger.info("JwtAuthenticationFilter: Extracted token: {}", token.substring(0, Math.min(20, token.length())) + "...");
        
        if (!jwtService.isTokenValid(token)) {
            logger.warn("JwtAuthenticationFilter: Invalid JWT token provided");
            return chain.filter(exchange);
        }
        
        logger.info("JwtAuthenticationFilter: Token is valid, extracting claims...");
        
        return jwtService.extractUsername(token)
                .zipWith(jwtService.extractRole(token))
                .flatMap(tuple -> {
                    String username = tuple.getT1();
                    String role = tuple.getT2();
                    
                    logger.info("JwtAuthenticationFilter: Extracted username: {}, role: {}", username, role);
                    
                    if (username != null && role != null) {
                        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            username, null, Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role))
                        );
                        
                        SecurityContext securityContext = new SecurityContextImpl(authentication);
                        exchange.getAttributes().put(SecurityContext.class.getName(), securityContext);
                        
                        logger.info("JwtAuthenticationFilter: Authentication set for user: {} with role: {}", username, role);
                    } else {
                        logger.warn("JwtAuthenticationFilter: Could not extract username or role from token");
                    }
                    
                    return chain.filter(exchange);
                })
                .onErrorResume(error -> {
                    logger.error("JwtAuthenticationFilter: Error processing JWT token: {}", error.getMessage(), error);
                    return chain.filter(exchange);
                });
    }
    
    private boolean isPublicEndpoint(String path) {
        return path.startsWith("/auth/") ||
               path.startsWith("/api/v1/weather/current") ||
               path.startsWith("/api/v1/weather/forecast") ||
               path.startsWith("/api/v1/weather/summary") ||
               path.startsWith("/api/v1/weather/version") ||
               path.startsWith("/api/v1/weather/status") ||
               path.startsWith("/api/v1/weather/providers") ||
               path.startsWith("/api/v1/weather/test");
    }
} 