/**
 * Exception handling package for the WeatherService API.
 * 
 * <p>This package contains custom exceptions and a global exception handler
 * to provide consistent error responses across the API.</p>
 * 
 * <h3>Custom Exceptions:</h3>
 * <ul>
 *   <li>{@link WeatherServiceException} - Base exception for all weather service errors</li>
 *   <li>{@link CityNotFoundException} - Thrown when a city is not found</li>
 *   <li>{@link WeatherProviderException} - Thrown when weather provider issues occur</li>
 *   <li>{@link InvalidRequestException} - Thrown for invalid request parameters</li>
 *   <li>{@link CacheException} - Thrown for cache-related errors</li>
 * </ul>
 * 
 * <h3>Global Exception Handler:</h3>
 * <ul>
 *   <li>{@link GlobalExceptionHandler} - Handles all exceptions and returns standardized error responses</li>
 * </ul>
 * 
 * <h3>Error Response Format:</h3>
 * <pre>
 * {
 *   "timestamp": "2024-01-01T12:00:00",
 *   "status": 404,
 *   "error": "Not Found",
 *   "errorCode": "CITY_NOT_FOUND",
 *   "message": "City not found: InvalidCity",
 *   "path": "/api/v1/weather/current"
 * }
 * </pre>
 * 
 * @author WeatherService Team
 * @version 1.0
 */
package com.lplemos.weather_service.exception; 