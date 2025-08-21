package com.fares_elsadek.Readly.config.properties;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "app.email")
@Validated
public record EmailProperties(
        @NotBlank String from,
        @Positive int verificationTokenTtlMinutes,
        @Positive int maxRetryAttempts
) {}
