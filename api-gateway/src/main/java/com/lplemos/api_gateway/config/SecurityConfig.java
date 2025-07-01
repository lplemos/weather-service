package com.lplemos.api_gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        http
            .authorizeExchange(exchanges -> exchanges
                // Allow public endpoints
                .pathMatchers("/actuator/health/**", "/actuator/info/**", "/actuator/metrics/**").permitAll()
                .pathMatchers("/api/v1/weather/current/**", "/api/v1/weather/forecast/**", "/api/v1/weather/summary/**").permitAll()
                // Require authentication for all other endpoints
                .anyExchange().authenticated()
            )
            .csrf(csrf -> csrf.disable())
            .httpBasic(httpBasic -> httpBasic.disable())
            .formLogin(formLogin -> formLogin.disable());
        
        return http.build();
    }
} 