package com.coderaah.medtrack.doctor.dto;

import com.coderaah.medtrack.doctor.domain.DoctorSpecialty;

import java.time.LocalDateTime;

public record DoctorSpecialtyResponse(
        Long id,
        Long doctorId,
        Long specialtyId,
        String specialtyCode,
        String specialtyName,
        boolean primarySpecialty,
        LocalDateTime createdAt) {

    public static DoctorSpecialtyResponse from(DoctorSpecialty doctorSpecialty) {
        return new DoctorSpecialtyResponse(
                doctorSpecialty.getId(),
                doctorSpecialty.getDoctor().getId(),
                doctorSpecialty.getSpecialty().getId(),
                doctorSpecialty.getSpecialty().getCode(),
                doctorSpecialty.getSpecialty().getName(),
                doctorSpecialty.isPrimarySpecialty(),
                doctorSpecialty.getCreatedAt());
    }
}
