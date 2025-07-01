package com.lplemos.weather_service.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record WeatherResponse(
    @JsonProperty("coord") Coordinates coord,
    @JsonProperty("weather") List<Weather> weather,
    @JsonProperty("main") MainWeather main,
    @JsonProperty("wind") Wind wind,
    @JsonProperty("clouds") Clouds clouds,
    @JsonProperty("sys") SystemInfo sys,
    @JsonProperty("name") String name,
    @JsonProperty("id") Integer id,
    @JsonProperty("cod") Integer cod
) {
    
    public record Coordinates(
        @JsonProperty("lon") Double lon,
        @JsonProperty("lat") Double lat
    ) {}
    
    public record Weather(
        @JsonProperty("id") Integer id,
        @JsonProperty("main") String main,
        @JsonProperty("description") String description,
        @JsonProperty("icon") String icon
    ) {}
    
    public record MainWeather(
        @JsonProperty("temp") Double temp,
        @JsonProperty("feels_like") Double feelsLike,
        @JsonProperty("temp_min") Double tempMin,
        @JsonProperty("temp_max") Double tempMax,
        @JsonProperty("pressure") Integer pressure,
        @JsonProperty("humidity") Integer humidity
    ) {}
    
    public record Wind(
        @JsonProperty("speed") Double speed,
        @JsonProperty("deg") Integer deg
    ) {}
    
    public record Clouds(
        @JsonProperty("all") Integer all
    ) {}
    
    public record SystemInfo(
        @JsonProperty("country") String country,
        @JsonProperty("sunrise") Long sunrise,
        @JsonProperty("sunset") Long sunset
    ) {}
} 