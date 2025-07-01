package com.lplemos.weather_service.config;

import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.web.server.context.ServerSecurityContextRepository;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class JwtSecurityContextRepository implements ServerSecurityContextRepository {
    
    @Override
    public Mono<SecurityContext> load(ServerWebExchange exchange) {
        SecurityContext context = exchange.getAttribute(SecurityContext.class.getName());
        return context != null ? Mono.just(context) : Mono.empty();
    }
    
    @Override
    public Mono<Void> save(ServerWebExchange exchange, SecurityContext context) {
        exchange.getAttributes().put(SecurityContext.class.getName(), context);
        return Mono.empty();
    }
} 