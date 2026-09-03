package com.coderaah.medtrack.doctor.dto;

import com.coderaah.medtrack.doctor.domain.ScheduleExceptionReason;
import com.coderaah.medtrack.doctor.domain.ScheduleExceptionType;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record DoctorScheduleExceptionRequest(
        @NotNull LocalDateTime startsAt,
        @NotNull LocalDateTime endsAt,
        @NotNull ScheduleExceptionType exceptionType,
        ScheduleExceptionReason reasonType,
        String reason
) {
}