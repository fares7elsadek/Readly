package com.fares_elsadek.Readly.dtos;

import com.fares_elsadek.Readly.entity.Book;

public record BookHistoryDto(String id,Book book, boolean returned, boolean returnApproved, UserDto user) {
}
