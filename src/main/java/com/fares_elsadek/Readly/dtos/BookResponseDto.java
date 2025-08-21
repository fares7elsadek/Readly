package com.fares_elsadek.Readly.dtos;

public record BookResponseDto(
        String id,
        String title,
        String authorName,
        String isbn,
        String synopsis,
        String bookCover,
        Boolean shareable,
        UserDto owner
) {
}
