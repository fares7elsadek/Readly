package com.fares_elsadek.Readly.dtos;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

import java.util.List;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record LoginResponseDto(
        String accessToken,
        String refreshToken,
        String tokenType,
        Long expiresIn,
        UserInfo user
) {
    @Builder
    public record UserInfo(
            String id,
            String email,
            List<String> roles
    ) {}
}
