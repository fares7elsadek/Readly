package com.fares_elsadek.Readly.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record BookRequestDto(
        @NotBlank(message = "Title is required")
        @Size(max = 200, message = "Title must not exceed 200 characters")
        String title,

        @NotBlank(message = "Author name is required")
        @Size(max = 100, message = "Author name must not exceed 100 characters")
        String authorName,

        @NotBlank(message = "ISBN is required")
        @Pattern(
                regexp = "^(97(8|9))?\\d{9}(\\d|X)$",
                message = "ISBN must be a valid format (ISBN-10 or ISBN-13)"
        )
        String isbn,

        @Size(max = 1000, message = "Synopsis must not exceed 1000 characters")
        String synopsis
) {
}
