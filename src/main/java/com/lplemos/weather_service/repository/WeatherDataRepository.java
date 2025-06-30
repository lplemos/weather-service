package com.lplemos.weather_service.repository;

import com.lplemos.weather_service.model.WeatherData;
import com.lplemos.weather_service.model.WeatherSource;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Repository
public interface WeatherDataRepository extends ReactiveCrudRepository<WeatherData, Long> {
    
    Flux<WeatherData> findByCityIdOrderByTimestampDesc(Long cityId);
    
    Flux<WeatherData> findByCityIdAndTimestampBetweenOrderByTimestampDesc(
        Long cityId, LocalDateTime start, LocalDateTime end);
    
    @Query("SELECT * FROM weather_data WHERE city_id = :cityId ORDER BY timestamp DESC LIMIT 1")
    Mono<WeatherData> findLatestByCityId(Long cityId);
    
    @Query("SELECT DISTINCT city_id FROM weather_data ORDER BY city_id")
    Flux<Long> findAllCityIds();
    
    Flux<WeatherData> findByTimestampBetweenOrderByTimestampDesc(
        LocalDateTime start, LocalDateTime end);
    
    @Query("SELECT * FROM weather_data WHERE city_id = :cityId AND source = :source ORDER BY timestamp DESC LIMIT 1")
    Mono<WeatherData> findLatestByCityIdAndSource(Long cityId, WeatherSource source);
} 