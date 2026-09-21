package com.coderaah.medtrack.doctor.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateSpecialtyRequest(
        @NotBlank @Size(max = 100) String code,
        @NotBlank @Size(max = 255) String name)
{
}
