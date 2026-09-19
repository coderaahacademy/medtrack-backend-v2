package com.coderaah.medtrack.doctor.dto;

import java.time.DayOfWeek;
import java.time.LocalTime;

public record DoctorAvailabilityRuleResponse(
        Long id,
        Long doctorId,
        DayOfWeek dayOfWeek,
        LocalTime startTime,
        LocalTime endTime,
        boolean active
) {
}