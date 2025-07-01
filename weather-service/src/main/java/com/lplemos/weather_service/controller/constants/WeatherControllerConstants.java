package com.lplemos.weather_service.controller.constants;

/**
 * Constants for WeatherController operations
 */
public final class WeatherControllerConstants {
    
    private WeatherControllerConstants() {
        // Utility class - prevent instantiation
    }
    
    // API Endpoints (Versioned)
    public static final String BASE_PATH = ApiVersionConstants.CURRENT_BASE_PATH + "/weather";
    public static final String V1_BASE_PATH = ApiVersionConstants.V1_BASE_PATH + "/weather";
    
    public static final String TEST_ENDPOINT = "/test";
    public static final String CURRENT_ENDPOINT = "/current";
    public static final String CURRENT_STRUCTURED_ENDPOINT = "/current/structured";
    public static final String SUMMARY_ENDPOINT = "/summary";
    public static final String FORECAST_ENDPOINT = "/forecast";
    public static final String CURRENT_BY_ID_ENDPOINT = "/current/{cityId}";
    public static final String PROVIDERS_ENDPOINT = "/providers";
    public static final String PROVIDER_HEALTH_ENDPOINT = "/providers/{provider}/health";
    public static final String SECURITY_TEST_ENDPOINT = "/security-test";
    
    // Request Parameters
    public static final String PARAM_CITY = "city";
    public static final String PARAM_PROVIDER = "provider";
    public static final String PARAM_CITY_ID = "cityId";
    public static final String PARAM_VERSION = ApiVersionConstants.VERSION_PARAM;
    
    // Default Values
    public static final String DEFAULT_TEST_CITY = "Coimbra";
    public static final String DEFAULT_VERSION = ApiVersionConstants.CURRENT_VERSION;
    
    // Path Variables
    public static final String PATH_VAR_CITY_ID = "cityId";
    public static final String PATH_VAR_PROVIDER = "provider";
    public static final String PATH_VAR_VERSION = "version";
} 