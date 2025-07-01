package com.lplemos.weather_service.integrations.weather;

/**
 * Constants for OpenWeatherMap API integration
 */
public final class OpenWeatherMapConstants {
    
    private OpenWeatherMapConstants() {
        // Utility class - prevent instantiation
    }
    
    // API Endpoints
    public static final String WEATHER_ENDPOINT = "/weather";
    public static final String FORECAST_ENDPOINT = "/forecast";
    
    // Query Parameters
    public static final String PARAM_CITY = "q";
    public static final String PARAM_API_KEY = "appid";
    public static final String PARAM_UNITS = "units";
    public static final String PARAM_LANGUAGE = "lang";
    public static final String PARAM_CITY_ID = "id";
    
    // Log Messages
    public static final String LOG_PREFIX = "[OpenWeatherMap]";
    public static final String LOG_MAKING_REQUEST = "{} Making {} request: {}";
    public static final String LOG_WEATHER_RECEIVED = "{} Current weather received for {}: {}";
    public static final String LOG_STRUCTURED_RECEIVED = "{} Structured weather received for {}: {}°C";
    public static final String LOG_SUMMARY_RECEIVED = "{} Weather summary for {}: {} - {}";
    public static final String LOG_FORECAST_RECEIVED = "{} 5-day forecast received for {}: {}";
    public static final String LOG_CITY_ID_RECEIVED = "{} Current weather received for city ID {}: {}";
    public static final String LOG_ERROR_FETCHING = "{} Error fetching {} for {}: {}";
    
    // Default Values
    public static final String DEFAULT_HEALTH_CHECK_CITY = "London";
    public static final int HEALTH_CHECK_TIMEOUT_SECONDS = 5;
    
    // Request Types for Logging
    public static final String REQUEST_TYPE_CURRENT_WEATHER = "current weather";
    public static final String REQUEST_TYPE_STRUCTURED_WEATHER = "structured weather";
    public static final String REQUEST_TYPE_FORECAST = "forecast";
    public static final String REQUEST_TYPE_WEATHER_BY_ID = "weather by ID";
    public static final String REQUEST_TYPE_WEATHER_BY_COORDS = "weather by coordinates";
    public static final String REQUEST_TYPE_FORECAST_BY_COORDS = "forecast by coordinates";
} 