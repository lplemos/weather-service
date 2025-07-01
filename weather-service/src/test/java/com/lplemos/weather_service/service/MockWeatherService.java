package com.lplemos.weather_service.service;

import com.lplemos.weather_service.integrations.weather.WeatherProvider;
import com.lplemos.weather_service.model.WeatherProviderType;
import com.lplemos.weather_service.model.WeatherResponse;
import com.lplemos.weather_service.model.WeatherSummary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Mock implementation of WeatherService for testing purposes
 * Provides predictable test data without making external API calls
 */
@Service
@Profile("test")
public class MockWeatherService implements WeatherService {
    
    @Override
    public Mono<Map<String, Object>> getCurrentWeather(String cityName) {
        return Mono.just(createMockWeatherData(cityName));
    }
    
    @Override
    public Mono<Map<String, Object>> getCurrentWeather(String cityName, WeatherProviderType providerType) {
        return Mono.just(createMockWeatherData(cityName));
    }
    
    @Override
    public Mono<WeatherResponse> getCurrentWeatherStructured(String cityName) {
        return Mono.just(createMockWeatherResponse(cityName));
    }
    
    @Override
    public Mono<WeatherResponse> getCurrentWeatherStructured(String cityName, WeatherProviderType providerType) {
        return Mono.just(createMockWeatherResponse(cityName));
    }
    
    @Override
    public Mono<WeatherSummary> getWeatherSummary(String cityName) {
        return Mono.just(createMockWeatherSummary(cityName));
    }
    
    @Override
    public Mono<WeatherSummary> getWeatherSummary(String cityName, WeatherProviderType providerType) {
        return Mono.just(createMockWeatherSummary(cityName));
    }
    
    @Override
    public Mono<Map<String, Object>> getWeatherForecast(String cityName) {
        return Mono.just(createMockForecastData(cityName));
    }
    
    @Override
    public Mono<Map<String, Object>> getWeatherForecast(String cityName, WeatherProviderType providerType) {
        return Mono.just(createMockForecastData(cityName));
    }
    
    @Override
    public Mono<Map<String, Object>> getCurrentWeatherById(Integer cityId) {
        return Mono.just(createMockWeatherData("Mock City " + cityId));
    }
    
    @Override
    public Mono<Map<String, Object>> getCurrentWeatherById(Integer cityId, WeatherProviderType providerType) {
        return Mono.just(createMockWeatherData("Mock City " + cityId));
    }
    
    @Override
    public Mono<Map<String, Object>> getCurrentWeatherByCoords(Double lat, Double lon) {
        return Mono.just(createMockWeatherData("Mock City at " + lat + "," + lon));
    }
    
    @Override
    public Mono<Map<String, Object>> getCurrentWeatherByCoords(Double lat, Double lon, WeatherProviderType providerType) {
        return Mono.just(createMockWeatherData("Mock City at " + lat + "," + lon));
    }
    
    @Override
    public Mono<Map<String, Object>> getWeatherForecastByCoords(Double lat, Double lon) {
        return Mono.just(createMockForecastData("Mock City at " + lat + "," + lon));
    }
    
    @Override
    public Mono<Map<String, Object>> getWeatherForecastByCoords(Double lat, Double lon, WeatherProviderType providerType) {
        return Mono.just(createMockForecastData("Mock City at " + lat + "," + lon));
    }
    
    @Override
    public reactor.core.publisher.Mono<java.util.Map<String, Object>> getCurrentWeatherByCoords(Double lat, Double lon, com.lplemos.weather_service.model.WeatherProviderType providerType, String language) {
        // Mock: retorna um Mono vazio ou um mapa simulado
        return reactor.core.publisher.Mono.empty();
    }
    
    @Override
    public reactor.core.publisher.Mono<java.util.Map<String, Object>> getWeatherForecastByCoords(Double lat, Double lon, com.lplemos.weather_service.model.WeatherProviderType providerType, String language) {
        // Mock: retorna um Mono vazio ou um mapa simulado
        return reactor.core.publisher.Mono.empty();
    }
    
    @Override
    public reactor.core.publisher.Mono<java.util.Map<String, Object>> getWeatherForecast(String cityName, com.lplemos.weather_service.model.WeatherProviderType providerType, String language) {
        // Mock: retorna um Mono vazio ou um mapa simulado
        return reactor.core.publisher.Mono.empty();
    }
    
    @Override
    public reactor.core.publisher.Mono<java.util.Map<String, Object>> getCurrentWeather(String cityName, com.lplemos.weather_service.model.WeatherProviderType providerType, String language) {
        // Mock: retorna um Mono vazio ou um mapa simulado
        return reactor.core.publisher.Mono.empty();
    }
    
    @Override
    public WeatherProvider getProvider(WeatherProviderType providerType) {
        return new MockWeatherProvider();
    }
    
    @Override
    public List<String> getAvailableProviders() {
        return List.of("MockProvider", "OpenWeatherMap");
    }
    
    @Override
    public Mono<Boolean> isProviderAvailable(WeatherProviderType providerType) {
        return Mono.just(true);
    }
    
    @Override
    public WeatherProvider getDefaultProvider() {
        return new MockWeatherProvider();
    }
    
    private Map<String, Object> createMockWeatherData(String cityName) {
        return Map.of(
            "coord", Map.of("lon", -8.4195, "lat", 40.2056),
            "weather", List.of(Map.of(
                "id", 800,
                "main", "Clear",
                "description", "clear sky",
                "icon", "01d"
            )),
            "main", Map.of(
                "temp", 25.0,
                "feels_like", 26.0,
                "temp_min", 20.0,
                "temp_max", 30.0,
                "pressure", 1013,
                "humidity", 60
            ),
            "wind", Map.of("speed", 2.5, "deg", 180),
            "clouds", Map.of("all", 0),
            "sys", Map.of(
                "country", "PT",
                "sunrise", 1751260040L,
                "sunset", 1751314026L
            ),
            "name", cityName,
            "cod", 200
        );
    }
    
    private WeatherResponse createMockWeatherResponse(String cityName) {
        return new WeatherResponse(
            new WeatherResponse.Coordinates(-8.4195, 40.2056),
            List.of(new WeatherResponse.Weather(800, "Clear", "clear sky", "01d")),
            new WeatherResponse.MainWeather(25.0, 26.0, 20.0, 30.0, 1013, 60),
            new WeatherResponse.Wind(2.5, 180),
            new WeatherResponse.Clouds(0),
            new WeatherResponse.SystemInfo("PT", 1751260040L, 1751314026L),
            cityName,
            2740637,
            200
        );
    }
    
    private WeatherSummary createMockWeatherSummary(String cityName) {
        return new WeatherSummary(
            cityName,
            "PT",
            25.0,
            26.0,
            "clear sky",
            60,
            2.5,
            LocalDateTime.now()
        );
    }
    
    private Map<String, Object> createMockForecastData(String cityName) {
        return Map.of(
            "city", Map.of("name", cityName, "country", "PT"),
            "list", List.of(
                Map.of(
                    "dt", 1751276201L,
                    "main", Map.of("temp", 25.0, "humidity", 60),
                    "weather", List.of(Map.of("main", "Clear", "description", "clear sky"))
                )
            )
        );
    }
    
    private static class MockWeatherProvider implements WeatherProvider {
        @Override
        public Mono<Map<String, Object>> getCurrentWeather(String cityName) {
            return Mono.just(Map.of("name", cityName, "temp", 25.0));
        }
        
        @Override
        public Mono<Map<String, Object>> getCurrentWeather(String cityName, String language) {
            return Mono.just(Map.of("name", cityName, "temp", 25.0, "language", language));
        }
        
        @Override
        public Mono<WeatherResponse> getCurrentWeatherStructured(String cityName) {
            return Mono.empty();
        }
        
        @Override
        public Mono<WeatherResponse> getCurrentWeatherStructured(String cityName, String language) {
            return Mono.empty();
        }
        
        @Override
        public Mono<WeatherSummary> getWeatherSummary(String cityName) {
            return Mono.empty();
        }
        
        @Override
        public Mono<WeatherSummary> getWeatherSummary(String cityName, String language) {
            return Mono.empty();
        }
        
        @Override
        public Mono<Map<String, Object>> getWeatherForecast(String cityName) {
            return Mono.just(Map.of("city", cityName));
        }
        
        @Override
        public Mono<Map<String, Object>> getWeatherForecast(String cityName, String language) {
            return Mono.just(Map.of("city", cityName, "language", language));
        }
        
        @Override
        public Mono<Map<String, Object>> getCurrentWeatherById(Integer cityId) {
            return Mono.just(Map.of("id", cityId, "temp", 25.0));
        }
        
        @Override
        public Mono<Map<String, Object>> getCurrentWeatherById(Integer cityId, String language) {
            return Mono.just(Map.of("id", cityId, "temp", 25.0, "language", language));
        }
        
        @Override
        public Mono<Map<String, Object>> getCurrentWeatherByCoords(Double lat, Double lon) {
            return Mono.just(Map.of("name", "Mock City at " + lat + "," + lon, "temp", 25.0));
        }
        
        @Override
        public Mono<Map<String, Object>> getCurrentWeatherByCoords(Double lat, Double lon, String language) {
            return Mono.just(Map.of("name", "Mock City at " + lat + "," + lon, "temp", 25.0, "language", language));
        }
        
        @Override
        public Mono<Map<String, Object>> getWeatherForecastByCoords(Double lat, Double lon) {
            return Mono.just(Map.of("city", "Mock City at " + lat + "," + lon));
        }
        
        @Override
        public Mono<Map<String, Object>> getWeatherForecastByCoords(Double lat, Double lon, String language) {
            return Mono.just(Map.of("city", "Mock City at " + lat + "," + lon, "language", language));
        }
        
        @Override
        public String getProviderName() {
            return "MockProvider";
        }
        
        @Override
        public Mono<Boolean> isAvailable() {
            return Mono.just(true);
        }
    }
} 