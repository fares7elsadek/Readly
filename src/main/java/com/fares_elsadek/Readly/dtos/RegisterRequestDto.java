package com.fares_elsadek.Readly.dtos;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequestDto(
        @Email @NotBlank String email,
        @NotBlank @Size(min = 8, max = 72) String password
) {}
