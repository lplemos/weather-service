package com.lplemos.weather_service.controller.constants;

/**
 * Constants for API versioning
 */
public final class ApiVersionConstants {
    
    private ApiVersionConstants() {
        // Utility class - prevent instantiation
    }
    
    // API Versions
    public static final String V1 = "v1";
    
    // API Base Paths
    public static final String API_BASE_PATH = "/api";
    public static final String V1_BASE_PATH = API_BASE_PATH + "/" + V1;
    
    // Current API Version (for default routing)
    public static final String CURRENT_VERSION = V1;
    public static final String CURRENT_BASE_PATH = V1_BASE_PATH;
    
    // Version Headers (for future use)
    public static final String VERSION_HEADER = "X-API-Version";
    public static final String VERSION_ACCEPT_HEADER = "Accept-Version";
    
    // Version Query Parameter (for future use)
    public static final String VERSION_PARAM = "version";
} 