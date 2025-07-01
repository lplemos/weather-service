package com.lplemos.weather_service.model;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

public record WeatherSummary(
    String city,
    String country,
    Double temperature,
    Double feelsLike,
    String description,
    Integer humidity,
    Double windSpeed,
    LocalDateTime timestamp
) {
    
    public static WeatherSummary fromWeatherResponse(WeatherResponse response) {
        return new WeatherSummary(
            response.name(),
            response.sys().country(),
            response.main().temp(),
            response.main().feelsLike(),
            response.weather().get(0).description(),
            response.main().humidity(),
            response.wind().speed(),
            LocalDateTime.ofInstant(Instant.now(), ZoneId.systemDefault())
        );
    }
    
    public String getFormattedTemperature() {
        return String.format("%.1f°C", temperature);
    }
    
    public String getFormattedFeelsLike() {
        return String.format("%.1f°C", feelsLike);
    }
    
    public String getFormattedWindSpeed() {
        return String.format("%.1f m/s", windSpeed);
    }
} 