package com.lplemos.weather_service.model;

public enum WeatherSource {
    OPENWEATHER_MAP("OpenWeatherMap"),
    WEATHER_API("WeatherAPI"),
    ACCUWEATHER("AccuWeather"),
    MANUAL("Manual"),
    HISTORICAL("Historical");
    
    private final String displayName;
    
    WeatherSource(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
} 