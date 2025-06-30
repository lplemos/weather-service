package com.lplemos.weather_service.model;

import jakarta.validation.constraints.NotNull;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Table("weather_data")
public record WeatherData(
    @Id Long id,
    @NotNull Long cityId,
    Double temperature,
    Double feelsLike,
    Integer humidity,
    Double pressure,
    Double windSpeed,
    String windDirection,
    String description,
    String icon,
    Integer visibility,
    Integer uvIndex,
    LocalDateTime timestamp,
    LocalDateTime createdAt,
    WeatherSource source
) {
    public WeatherData {
        if (timestamp == null) timestamp = LocalDateTime.now();
        if (createdAt == null) createdAt = LocalDateTime.now();
    }
} 