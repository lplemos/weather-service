package com.lplemos.weather_service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http, com.lplemos.weather_service.config.JwtSecurityContextRepository jwtSecurityContextRepository) {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeExchange(authz -> authz
                .pathMatchers("/auth/**").permitAll()
                // Public weather endpoints
                .pathMatchers("/api/v1/weather/current/**").permitAll()
                .pathMatchers("/api/v1/weather/forecast/**").permitAll()
                .pathMatchers("/api/v1/weather/summary/**").permitAll()
                .pathMatchers("/api/v1/weather/version/**").permitAll()
                .pathMatchers("/api/v1/weather/status/**").permitAll()
                .pathMatchers("/api/v1/weather/providers/**").permitAll()
                .pathMatchers("/api/v1/weather/test/**").permitAll()
                // Cache read endpoints - accessible to USER and ADMIN
                .pathMatchers("/api/v1/weather/cache/stats").hasAnyRole("USER", "ADMIN")
                .pathMatchers("/api/v1/weather/cache/health").hasAnyRole("USER", "ADMIN")
                .pathMatchers("/api/v1/weather/hierarchical/cache/stats").hasAnyRole("USER", "ADMIN")
                .pathMatchers("/api/v1/weather/hierarchical/cache/health").hasAnyRole("USER", "ADMIN")
                // Cache write endpoints - only ADMIN
                .pathMatchers("/api/v1/weather/cache/city").hasRole("ADMIN")
                .pathMatchers("/api/v1/weather/cache/all").hasRole("ADMIN")
                .pathMatchers("/api/v1/weather/hierarchical/cache/city").hasRole("ADMIN")
                .pathMatchers("/api/v1/weather/hierarchical/cache/all").hasRole("ADMIN")
                // Other weather endpoints - accessible to USER and ADMIN
                .pathMatchers("/api/v1/weather/**").hasAnyRole("USER", "ADMIN")
                .anyExchange().authenticated()
            )
            .httpBasic(httpBasic -> httpBasic.disable())
            .formLogin(formLogin -> formLogin.disable())
            .securityContextRepository(jwtSecurityContextRepository);
        
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
} 