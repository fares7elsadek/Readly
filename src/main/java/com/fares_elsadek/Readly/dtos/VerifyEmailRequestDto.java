package com.fares_elsadek.Readly.dtos;

import jakarta.validation.constraints.NotBlank;

public record VerifyEmailRequestDto (
        @NotBlank String token
) {}
