package com.lplemos.weather_service.repository;

import com.lplemos.weather_service.model.City;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface CityRepository extends ReactiveCrudRepository<City, Long> {
    
    Flux<City> findByIsActiveTrue();
    
    Flux<City> findByCountry(String country);
    
    Mono<City> findByNameAndCountry(String name, String country);
    
    Flux<City> findByNameContainingIgnoreCase(String name);
} 