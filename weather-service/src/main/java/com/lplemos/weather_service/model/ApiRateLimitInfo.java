package com.lplemos.weather_service.model;

import java.time.Instant;

/**
 * Model to capture API rate limiting information
 */
public record ApiRateLimitInfo(
    String provider,
    Integer requestsLimit,
    Integer requestsRemaining,
    Instant resetTime,
    Instant timestamp
) {
    
    public static ApiRateLimitInfo create(String provider, Integer limit, Integer remaining, Instant reset) {
        return new ApiRateLimitInfo(provider, limit, remaining, reset, Instant.now());
    }
    
    public static ApiRateLimitInfo create(String provider) {
        return new ApiRateLimitInfo(provider, null, null, null, Instant.now());
    }
    
    public boolean hasRateLimitInfo() {
        return requestsLimit != null && requestsRemaining != null;
    }
    
    public String getUsagePercentage() {
        if (!hasRateLimitInfo()) {
            return "Unknown";
        }
        double percentage = ((double) (requestsLimit - requestsRemaining) / requestsLimit) * 100;
        return String.format("%.1f%%", percentage);
    }
} 