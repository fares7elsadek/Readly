package com.fares_elsadek.Readly.config.properties;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "app")
@Validated
public record AppProperties(
        @NotBlank String baseUrl,
        @Positive int maxLoginAttempts,
        @Positive int loginAttemptWindowMinutes
) {}
