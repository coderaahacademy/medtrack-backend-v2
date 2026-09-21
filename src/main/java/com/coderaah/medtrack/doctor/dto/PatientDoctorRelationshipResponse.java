package com.coderaah.medtrack.doctor.dto;

import com.coderaah.medtrack.doctor.domain.DoctorRelationshipType;
import com.coderaah.medtrack.doctor.domain.PatientDoctorRelationship;

import java.time.LocalDateTime;

public record PatientDoctorRelationshipResponse(
        Long id,
        Long patientId,
        Long doctorId,
        DoctorRelationshipType relationshipType,
        LocalDateTime startedAt,
        LocalDateTime endedAt,
        boolean active) {

    public static PatientDoctorRelationshipResponse from(PatientDoctorRelationship relationship) {
        return new PatientDoctorRelationshipResponse(
                relationship.getId(),
                relationship.getPatient().getId(),
                relationship.getDoctor().getId(),
                relationship.getRelationshipType(),
                relationship.getStartedAt(),
                relationship.getEndedAt(),
                relationship.isActive());
    }
}