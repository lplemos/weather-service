package com.lplemos.weather_service.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;

@Configuration
@ConfigurationProperties(prefix = "weather.openweathermap")
public class WeatherApiConfig {
    
    private static final Logger logger = LoggerFactory.getLogger(WeatherApiConfig.class);
    
    private String apiKey;
    private String baseUrl;
    private String units;
    private String language;
    
    @PostConstruct
    public void logConfiguration() {
        logger.info("Weather API Configuration loaded:");
        logger.info("Base URL: {}", baseUrl);
        logger.info("Units: {}", units);
        logger.info("Language: {}", language);
        logger.info("API Key: {}", apiKey != null && !apiKey.isEmpty() ? "***CONFIGURED***" : "***NOT CONFIGURED***");
    }
    
    // Getters and Setters
    public String getApiKey() {
        return apiKey;
    }
    
    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }
    
    public String getBaseUrl() {
        return baseUrl;
    }
    
    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }
    
    public String getUnits() {
        return units;
    }
    
    public void setUnits(String units) {
        this.units = units;
    }
    
    public String getLanguage() {
        return language;
    }
    
    public void setLanguage(String language) {
        this.language = language;
    }
} 