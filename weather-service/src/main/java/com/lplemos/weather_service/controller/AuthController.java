package com.lplemos.weather_service.controller;

import com.lplemos.weather_service.dto.UserLoginRequest;
import com.lplemos.weather_service.dto.UserLoginResponse;
import com.lplemos.weather_service.dto.UserRegisterRequest;
import com.lplemos.weather_service.dto.UserRegisterResponse;
import com.lplemos.weather_service.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    
    private final UserService userService;
    
    public AuthController(UserService userService) {
        this.userService = userService;
    }
    
    @PostMapping("/login")
    public Mono<ResponseEntity<UserLoginResponse>> login(@RequestBody UserLoginRequest request) {
        logger.info("Login request received for user: {}", request.getUsername());
        
        return userService.login(request)
                .map(response -> {
                    logger.info("Login successful for user: {}", request.getUsername());
                    return ResponseEntity.ok(response);
                })
                .onErrorResume(error -> {
                    logger.error("Login failed for user: {}", request.getUsername(), error);
                    return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
                });
    }
    
    @PostMapping("/register")
    public Mono<ResponseEntity<UserRegisterResponse>> register(@RequestBody UserRegisterRequest request) {
        logger.info("Registration request received for user: {}", request.getUsername());
        
        return userService.register(request)
                .map(response -> {
                    logger.info("Registration successful for user: {}", request.getUsername());
                    return ResponseEntity.status(HttpStatus.CREATED).body(response);
                })
                .onErrorResume(error -> {
                    logger.error("Registration failed for user: {}", request.getUsername(), error);
                    return Mono.just(ResponseEntity.badRequest().build());
                });
    }
} 