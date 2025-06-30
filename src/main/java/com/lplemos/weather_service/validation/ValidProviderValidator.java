package com.lplemos.weather_service.validation;

import com.lplemos.weather_service.model.WeatherProviderType;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Validator implementation for ValidProvider annotation
 */
public class ValidProviderValidator implements ConstraintValidator<ValidProvider, String> {
    
    @Override
    public void initialize(ValidProvider constraintAnnotation) {
        // No initialization needed
    }
    
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        // Allow null/empty values (they will be handled by other validations)
        if (value == null || value.trim().isEmpty()) {
            return true;
        }
        
        try {
            WeatherProviderType.fromCode(value);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
} 