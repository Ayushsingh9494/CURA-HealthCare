package com.cura.auth.dto;

import com.cura.user.entity.Role;

public record TokenResponse(
        Long userId,
        String email,
        Role role,
        String accessToken,
        String refreshToken
) {
}
