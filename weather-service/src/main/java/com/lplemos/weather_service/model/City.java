package com.lplemos.weather_service.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Table("cities")
public record City(
    @Id Long id,
    @NotBlank String name,
    @NotBlank String country,
    @NotNull Double latitude,
    @NotNull Double longitude,
    String timezone,
    Boolean isActive,
    LocalDateTime createdAt
) {
    public City {
        if (isActive == null) isActive = true;
        if (createdAt == null) createdAt = LocalDateTime.now();
    }
} 