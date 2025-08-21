package com.fares_elsadek.Readly.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ResendVerificationRequestDto(
        @Email @NotBlank String email
) {}
