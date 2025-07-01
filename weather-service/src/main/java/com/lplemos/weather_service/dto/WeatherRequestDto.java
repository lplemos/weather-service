package com.lplemos.weather_service.dto;

import com.lplemos.weather_service.validation.ValidProvider;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * DTO for weather request parameters with validation
 */
public class WeatherRequestDto {
    
    @NotBlank(message = "City name cannot be empty")
    @Size(min = 2, max = 100, message = "City name must be between 2 and 100 characters")
    @Pattern(regexp = "^[a-zA-ZÀ-ÿ\\s\\-']+$", message = "City name can only contain letters, spaces, hyphens, and apostrophes")
    private String city;
    
    @ValidProvider
    private String provider;
    
    public WeatherRequestDto() {}
    
    public WeatherRequestDto(String city) {
        this.city = city;
    }
    
    public WeatherRequestDto(String city, String provider) {
        this.city = city;
        this.provider = provider;
    }
    
    public String getCity() {
        return city;
    }
    
    public void setCity(String city) {
        this.city = city;
    }
    
    public String getProvider() {
        return provider;
    }
    
    public void setProvider(String provider) {
        this.provider = provider;
    }
    
    @Override
    public String toString() {
        return "WeatherRequestDto{" +
                "city='" + city + '\'' +
                ", provider='" + provider + '\'' +
                '}';
    }
} 