package com.coderaah.medtrack.doctor.service;

import com.coderaah.medtrack.doctor.domain.DoctorProfile;
import com.coderaah.medtrack.doctor.domain.DoctorScheduleException;
import com.coderaah.medtrack.doctor.domain.ScheduleExceptionReason;
import com.coderaah.medtrack.doctor.domain.ScheduleExceptionType;
import com.coderaah.medtrack.doctor.dto.DoctorScheduleExceptionRequest;
import com.coderaah.medtrack.doctor.dto.DoctorScheduleExceptionResponse;
import com.coderaah.medtrack.doctor.repository.DoctorProfileRepository;
import com.coderaah.medtrack.doctor.repository.DoctorScheduleExceptionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DoctorScheduleExceptionServiceTest {

    @Mock
    private DoctorScheduleExceptionRepository exceptionRepository;

    @Mock
    private DoctorProfileRepository doctorProfileRepository;

    @InjectMocks
    private DoctorScheduleExceptionService service;

    @Test
    void shouldCreateScheduleException() {

        Long doctorId = 1L;

        DoctorProfile doctor = new DoctorProfile();

        LocalDateTime startsAt =
                LocalDateTime.of(2026, 9, 10, 9, 0);

        LocalDateTime endsAt =
                LocalDateTime.of(2026, 9, 12, 18, 0);

        DoctorScheduleExceptionRequest request =
                new DoctorScheduleExceptionRequest(
                        startsAt,
                        endsAt,
                        ScheduleExceptionType.UNAVAILABLE,
                        ScheduleExceptionReason.VACATION,
                        "Annual vacation"
                );

        when(doctorProfileRepository.findById(doctorId))
                .thenReturn(Optional.of(doctor));

        DoctorScheduleException savedException =
                new DoctorScheduleException(
                        doctor,
                        request.startsAt(),
                        request.endsAt(),
                        request.exceptionType(),
                        request.reasonType(),
                        request.reason()
                );

        savedException.setId(1L);

        when(exceptionRepository.save(any(DoctorScheduleException.class)))
                .thenReturn(savedException);

        DoctorScheduleExceptionResponse response =
                service.createException(doctorId, request);

        assertNotNull(response);
        assertEquals(1L, response.id());
        assertEquals(startsAt, response.startsAt());
        assertEquals(endsAt, response.endsAt());
        assertEquals(
                ScheduleExceptionType.UNAVAILABLE,
                response.exceptionType()
        );
        assertEquals(
                ScheduleExceptionReason.VACATION,
                response.reasonType()
        );
        assertEquals("Annual vacation", response.reason());
    }

    @Test
    void shouldRejectInvalidScheduleExceptionTimeRange() {

        Long doctorId = 1L;

        DoctorScheduleExceptionRequest request =
                new DoctorScheduleExceptionRequest(
                        LocalDateTime.of(2026, 9, 12, 18, 0),
                        LocalDateTime.of(2026, 9, 10, 9, 0),
                        ScheduleExceptionType.UNAVAILABLE,
                        ScheduleExceptionReason.SICK_LEAVE,
                        "Sick leave"
                );

        assertThrows(
                IllegalArgumentException.class,
                () -> service.createException(doctorId, request)
        );
    }
}