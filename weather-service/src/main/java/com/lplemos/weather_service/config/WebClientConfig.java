package com.lplemos.weather_service.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.regex.Pattern;

@Configuration
public class WebClientConfig {
    
    private static final Logger logger = LoggerFactory.getLogger(WebClientConfig.class);
    
    // Pattern to match API keys in URLs (common patterns)
    private static final Pattern API_KEY_PATTERN = Pattern.compile("(appid|api_key|key)=([^&]+)", Pattern.CASE_INSENSITIVE);
    
    @Bean
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder()
                .filter(logRequest())
                .filter(logResponse())
                .filter(logRateLimitHeaders())
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(2 * 1024 * 1024)); // 2MB
    }
    
    /**
     * CORS configuration to allow frontend requests
     */
    @Bean
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration corsConfig = new CorsConfiguration();
        
        // Allow all origins for development (restrict in production)
        corsConfig.addAllowedOriginPattern("*");
        
        // Allow common HTTP methods
        corsConfig.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD"));
        
        // Allow common headers
        corsConfig.setAllowedHeaders(Arrays.asList(
            "Origin", 
            "Content-Type", 
            "Accept", 
            "Authorization",
            "X-Requested-With",
            "Cache-Control",
            "Access-Control-Request-Method",
            "Access-Control-Request-Headers"
        ));
        
        // Allow credentials (cookies, auth headers)
        corsConfig.setAllowCredentials(true);
        
        // How long the browser can cache the CORS response
        corsConfig.setMaxAge(3600L);
        
        // Expose headers that the frontend might need
        corsConfig.setExposedHeaders(Arrays.asList(
            "Access-Control-Allow-Origin",
            "Access-Control-Allow-Credentials"
        ));
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfig);
        
        return new CorsWebFilter(source);
    }
    
    private ExchangeFilterFunction logRequest() {
        return ExchangeFilterFunction.ofRequestProcessor(clientRequest -> {
            String maskedUrl = maskApiKey(clientRequest.url().toString());
            logger.info("HTTP Request: {} {}", clientRequest.method(), maskedUrl);
            
            // Log headers but mask any sensitive information
            String maskedHeaders = maskSensitiveHeaders(clientRequest.headers().toString());
            logger.info("Headers: {}", maskedHeaders);
            
            return Mono.just(clientRequest);
        });
    }
    
    private ExchangeFilterFunction logResponse() {
        return ExchangeFilterFunction.ofResponseProcessor(clientResponse -> {
            logger.info("HTTP Response: {} {}", clientResponse.statusCode().value(), clientResponse.statusCode());
            logger.info("Headers: {}", clientResponse.headers().asHttpHeaders());
            return Mono.just(clientResponse);
        });
    }
    
    private ExchangeFilterFunction logRateLimitHeaders() {
        return ExchangeFilterFunction.ofResponseProcessor(clientResponse -> {
            // Log rate limiting headers
            clientResponse.headers().asHttpHeaders().forEach((name, values) -> {
                if (name.toLowerCase().contains("limit") || 
                    name.toLowerCase().contains("remaining") || 
                    name.toLowerCase().contains("reset") ||
                    name.toLowerCase().contains("x-ratelimit")) {
                    logger.info("Rate Limit Header - {}: {}", name, values);
                }
            });
            return Mono.just(clientResponse);
        });
    }
    
    /**
     * Masks API keys in URLs to prevent them from appearing in logs
     */
    private String maskApiKey(String url) {
        if (url == null || url.isEmpty()) {
            return url;
        }
        
        return API_KEY_PATTERN.matcher(url).replaceAll("$1=***MASKED***");
    }
    
    /**
     * Masks sensitive information in headers
     */
    private String maskSensitiveHeaders(String headers) {
        if (headers == null || headers.isEmpty()) {
            return headers;
        }
        
        // Mask common sensitive header patterns
        return headers
                .replaceAll("(?i)(authorization|api-key|x-api-key):\\s*[^\\s,]+", "$1: ***MASKED***")
                .replaceAll("(?i)(token|key|secret):\\s*[^\\s,]+", "$1: ***MASKED***");
    }
} 