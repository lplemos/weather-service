package com.lplemos.weather_service.model;

/**
 * Represents the source of weather data in the database
 * This tracks where the weather data came from when stored
 */
public enum WeatherSource {
    OPENWEATHERMAP("OpenWeatherMap"),
    WEATHERAPI("WeatherAPI"),
    ACCUWEATHER("AccuWeather"),
    METEO("Meteo API"),
    MANUAL("Manual"),
    HISTORICAL("Historical");
    
    private final String displayName;
    
    WeatherSource(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    /**
     * Convert from WeatherProviderType to WeatherSource
     */
    public static WeatherSource fromProviderType(WeatherProviderType providerType) {
        return switch (providerType) {
            case OPENWEATHERMAP -> OPENWEATHERMAP;
            case WEATHERAPI -> WEATHERAPI;
            case ACCUWEATHER -> ACCUWEATHER;
            case METEO -> METEO;
        };
    }
} 