package com.lplemos.weather_service.model;

/**
 * Enumeration of supported weather data providers
 */
public enum WeatherProviderType {
    
    OPENWEATHERMAP("openweathermap", "OpenWeatherMap"),
    WEATHERAPI("weatherapi", "WeatherAPI"),
    ACCUWEATHER("accuweather", "AccuWeather"),
    METEO("meteo", "Meteo API");
    
    private final String code;
    private final String displayName;
    
    WeatherProviderType(String code, String displayName) {
        this.code = code;
        this.displayName = displayName;
    }
    
    public String getCode() {
        return code;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public static WeatherProviderType fromCode(String code) {
        for (WeatherProviderType type : values()) {
            if (type.code.equalsIgnoreCase(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown weather provider: " + code);
    }
} 