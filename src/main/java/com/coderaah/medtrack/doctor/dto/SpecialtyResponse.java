package com.coderaah.medtrack.doctor.dto;

import com.coderaah.medtrack.doctor.domain.Specialty;

import java.time.LocalDateTime;

public record SpecialtyResponse(
        Long id,
        String code,
        String name,
        boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {

    public static SpecialtyResponse from(Specialty specialty) {
        return new SpecialtyResponse(
                specialty.getId(),
                specialty.getCode(),
                specialty.getName(),
                specialty.isActive(),
                specialty.getCreatedAt(),
                specialty.getUpdatedAt());
    }
}
