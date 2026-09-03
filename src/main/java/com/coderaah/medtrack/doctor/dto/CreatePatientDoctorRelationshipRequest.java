package com.coderaah.medtrack.doctor.dto;

import com.coderaah.medtrack.doctor.domain.DoctorRelationshipType;
import jakarta.validation.constraints.NotNull;

public record CreatePatientDoctorRelationshipRequest(
        @NotNull Long doctorId,
        @NotNull DoctorRelationshipType
        relationshipType) {
}
