package com.lplemos.weather_service.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for GlobalExceptionHandler
 */
class GlobalExceptionHandlerTest {
    
    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();
    
    @Test
    void testHandleCityNotFoundException() {
        // Given
        CityNotFoundException ex = new CityNotFoundException("InvalidCity");
        
        // When
        Mono<ResponseEntity<Map<String, Object>>> result = handler.handleCityNotFoundException(ex);
        
        // Then
        StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
                    Map<String, Object> body = response.getBody();
                    assertNotNull(body);
                    assertEquals(404, body.get("status"));
                    assertEquals("CITY_NOT_FOUND", body.get("errorCode"));
                    assertEquals("City not found: InvalidCity", body.get("message"));
                    assertNotNull(body.get("timestamp"));
                })
                .verifyComplete();
    }
    
    @Test
    void testHandleInvalidRequestException() {
        // Given
        InvalidRequestException ex = new InvalidRequestException("city", "City name cannot be empty");
        
        // When
        Mono<ResponseEntity<Map<String, Object>>> result = handler.handleInvalidRequestException(ex);
        
        // Then
        StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
                    Map<String, Object> body = response.getBody();
                    assertNotNull(body);
                    assertEquals(400, body.get("status"));
                    assertEquals("INVALID_REQUEST", body.get("errorCode"));
                    assertEquals("Invalid request - city: City name cannot be empty", body.get("message"));
                })
                .verifyComplete();
    }
    
    @Test
    void testHandleWeatherProviderException() {
        // Given
        WeatherProviderException ex = new WeatherProviderException("openweathermap", "API key invalid");
        
        // When
        Mono<ResponseEntity<Map<String, Object>>> result = handler.handleWeatherProviderException(ex);
        
        // Then
        StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
                    Map<String, Object> body = response.getBody();
                    assertNotNull(body);
                    assertEquals(503, body.get("status"));
                    assertEquals("WEATHER_PROVIDER_ERROR", body.get("errorCode"));
                    assertEquals("Weather provider error for openweathermap: API key invalid", body.get("message"));
                })
                .verifyComplete();
    }
    
    @Test
    void testHandleCacheException() {
        // Given
        CacheException ex = new CacheException("get", "Redis", new RuntimeException("Connection failed"));
        
        // When
        Mono<ResponseEntity<Map<String, Object>>> result = handler.handleCacheException(ex);
        
        // Then
        StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
                    Map<String, Object> body = response.getBody();
                    assertNotNull(body);
                    assertEquals(503, body.get("status"));
                    assertEquals("CACHE_ERROR", body.get("errorCode"));
                    assertEquals("Cache error during get on Redis", body.get("message"));
                })
                .verifyComplete();
    }
    
    @Test
    void testHandleIllegalArgumentException() {
        // Given
        IllegalArgumentException ex = new IllegalArgumentException("Invalid argument provided");
        
        // When
        Mono<ResponseEntity<Map<String, Object>>> result = handler.handleIllegalArgumentException(ex);
        
        // Then
        StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
                    Map<String, Object> body = response.getBody();
                    assertNotNull(body);
                    assertEquals(400, body.get("status"));
                    assertEquals("INVALID_ARGUMENT", body.get("errorCode"));
                    assertEquals("Invalid argument provided", body.get("message"));
                })
                .verifyComplete();
    }
    
    @Test
    void testHandleGenericException() {
        // Given
        Exception ex = new RuntimeException("Unexpected error");
        
        // When
        Mono<ResponseEntity<Map<String, Object>>> result = handler.handleGenericException(ex);
        
        // Then
        StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
                    Map<String, Object> body = response.getBody();
                    assertNotNull(body);
                    assertEquals(500, body.get("status"));
                    assertEquals("INTERNAL_SERVER_ERROR", body.get("errorCode"));
                    assertEquals("An unexpected error occurred", body.get("message"));
                })
                .verifyComplete();
    }
} 