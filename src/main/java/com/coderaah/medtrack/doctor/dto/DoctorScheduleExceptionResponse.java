package com.coderaah.medtrack.doctor.dto;

import com.coderaah.medtrack.doctor.domain.ScheduleExceptionReason;
import com.coderaah.medtrack.doctor.domain.ScheduleExceptionType;

import java.time.LocalDateTime;

public record DoctorScheduleExceptionResponse(
        Long id,
        Long doctorId,
        LocalDateTime startsAt,
        LocalDateTime endsAt,
        ScheduleExceptionType exceptionType,
        ScheduleExceptionReason reasonType,
        String reason
) {
}