package com.coderaah.medtrack.security.dto;

import com.coderaah.medtrack.identity.domain.Role;
import java.util.List;

public record LoginResponse(
        String accessToken,
        String tokenType,
        long expiresIn,
        Long userId,
        String email,
        List<Role> roles) {
}