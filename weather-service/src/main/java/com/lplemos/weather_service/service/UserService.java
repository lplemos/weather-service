package com.lplemos.weather_service.service;

import com.lplemos.weather_service.dto.UserLoginRequest;
import com.lplemos.weather_service.dto.UserLoginResponse;
import com.lplemos.weather_service.dto.UserRegisterRequest;
import com.lplemos.weather_service.dto.UserRegisterResponse;
import com.lplemos.weather_service.model.User;
import reactor.core.publisher.Mono;

public interface UserService {
    Mono<UserLoginResponse> login(UserLoginRequest request);
    Mono<UserRegisterResponse> register(UserRegisterRequest request);
    Mono<User> findByUsername(String username);
} 