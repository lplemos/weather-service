package com.lplemos.weather_service.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

/**
 * Custom validation annotation for weather provider validation
 */
@Documented
@Constraint(validatedBy = ValidProviderValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidProvider {
    
    String message() default "Provider must be one of: openweathermap, accuweather, weatherbit";
    
    Class<?>[] groups() default {};
    
    Class<? extends Payload>[] payload() default {};
} 