package com.lplemos.weather_service.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("users")
public record User(
    @Id Long id,
    @Column("username") String username,
    @Column("password") String password,
    @Column("role") UserRole role
) {
    public User(String username, String password, UserRole role) {
        this(null, username, password, role);
    }
} 