package com.lplemos.weather_service.service.impl;

import com.lplemos.weather_service.dto.UserLoginRequest;
import com.lplemos.weather_service.dto.UserLoginResponse;
import com.lplemos.weather_service.dto.UserRegisterRequest;
import com.lplemos.weather_service.dto.UserRegisterResponse;
import com.lplemos.weather_service.model.User;
import com.lplemos.weather_service.repository.UserRepository;
import com.lplemos.weather_service.service.JwtService;
import com.lplemos.weather_service.service.UserService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class UserServiceImpl implements UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);
    
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    
    public UserServiceImpl(UserRepository userRepository, JwtService jwtService, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
    }
    
    @Override
    public Mono<UserLoginResponse> login(UserLoginRequest request) {
        logger.info("Attempting login for user: {}", request.getUsername());
        
        return userRepository.findByUsername(request.getUsername())
                .filter(user -> passwordEncoder.matches(request.getPassword(), user.password()))
                .map(user -> {
                    String token = jwtService.generateToken(user.username(), user.role().name());
                    logger.info("Login successful for user: {}", user.username());
                    return new UserLoginResponse(token, user.username(), user.role().name());
                })
                .switchIfEmpty(Mono.error(new RuntimeException("Invalid credentials")));
    }
    
    @Override
    public Mono<UserRegisterResponse> register(UserRegisterRequest request) {
        logger.info("Attempting registration for user: {}", request.getUsername());
        
        return userRepository.existsByUsername(request.getUsername())
                .flatMap(exists -> {
                    if (exists) {
                        return Mono.error(new RuntimeException("Username already exists"));
                    }
                    
                    String encodedPassword = passwordEncoder.encode(request.getPassword());
                    User newUser = new User(request.getUsername(), encodedPassword, request.getRole());
                    
                    return userRepository.save(newUser)
                            .map(user -> {
                                logger.info("Registration successful for user: {}", user.username());
                                return new UserRegisterResponse(user.username(), user.role().name(), "User registered successfully");
                            });
                });
    }
    
    @Override
    public Mono<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }
} 