package com.coderaah.medtrack.doctor.dto;

import jakarta.validation.constraints.NotNull;

public record AssignDoctorSpecialtyRequest(
        @NotNull Long specialtyId,
        boolean primarySpecialty) {
}
