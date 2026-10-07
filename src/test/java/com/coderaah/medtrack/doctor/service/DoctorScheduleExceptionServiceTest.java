package com.coderaah.medtrack.doctor.service;

import com.coderaah.medtrack.doctor.domain.DoctorProfile;
import com.coderaah.medtrack.doctor.domain.DoctorScheduleException;
import com.coderaah.medtrack.doctor.domain.ScheduleExceptionReason;
import com.coderaah.medtrack.doctor.domain.ScheduleExceptionType;
import com.coderaah.medtrack.doctor.dto.DoctorScheduleExceptionRequest;
import com.coderaah.medtrack.doctor.dto.DoctorScheduleExceptionResponse;
import com.coderaah.medtrack.doctor.exception.CannotCancelPastScheduleException;
import com.coderaah.medtrack.doctor.exception.DoctorNotFoundException;
import com.coderaah.medtrack.doctor.exception.InvalidScheduleTimeRangeException;
import com.coderaah.medtrack.doctor.exception.ScheduleExceptionNotFoundException;
import com.coderaah.medtrack.doctor.repository.DoctorProfileRepository;
import com.coderaah.medtrack.doctor.repository.DoctorScheduleExceptionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DoctorScheduleExceptionServiceTest {

    @Mock
    private DoctorScheduleExceptionRepository exceptionRepository;

    @Mock
    private DoctorProfileRepository doctorProfileRepository;

    @InjectMocks
    private DoctorScheduleExceptionService exceptionService;

    private DoctorProfile doctor;

    @BeforeEach
    void setUp() {
        doctor = new DoctorProfile();
    }

    @Test
    void shouldCreateScheduleException() {

        LocalDateTime startsAt =
                LocalDateTime.now().plusDays(1);

        LocalDateTime endsAt =
                startsAt.plusHours(2);

        DoctorScheduleExceptionRequest request =
                new DoctorScheduleExceptionRequest(
                        startsAt,
                        endsAt,
                        ScheduleExceptionType.UNAVAILABLE,
                        ScheduleExceptionReason.VACATION,
                        "Annual vacation"
                );

        DoctorScheduleException exception =
                new DoctorScheduleException(
                        doctor,
                        startsAt,
                        endsAt,
                        request.exceptionType(),
                        request.reasonType(),
                        request.reason()
                );

        when(doctorProfileRepository.findById(1L))
                .thenReturn(Optional.of(doctor));

        when(exceptionRepository.save(any(DoctorScheduleException.class)))
                .thenReturn(exception);

        DoctorScheduleExceptionResponse response =
                exceptionService.createException(1L, request);

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

        LocalDateTime startsAt =
                LocalDateTime.of(2026, 9, 10, 14, 0);

        LocalDateTime endsAt =
                LocalDateTime.of(2026, 9, 10, 9, 0);

        DoctorScheduleExceptionRequest request =
                new DoctorScheduleExceptionRequest(
                        startsAt,
                        endsAt,
                        ScheduleExceptionType.UNAVAILABLE,
                        ScheduleExceptionReason.SICK_LEAVE,
                        "Sick leave"
                );

        assertThrows(
                InvalidScheduleTimeRangeException.class,
                () -> exceptionService.createException(1L, request)
        );

        verifyNoInteractions(
                doctorProfileRepository,
                exceptionRepository
        );
    }

    @Test
    void shouldRetrieveScheduleExceptions() {

        LocalDateTime startsAt =
                LocalDateTime.of(2026, 9, 10, 9, 0);

        LocalDateTime endsAt =
                LocalDateTime.of(2026, 9, 10, 13, 0);

        DoctorScheduleException exception =
                new DoctorScheduleException(
                        doctor,
                        startsAt,
                        endsAt,
                        ScheduleExceptionType.UNAVAILABLE,
                        ScheduleExceptionReason.TRAINING,
                        "Training"
                );

        when(doctorProfileRepository.findById(1L))
                .thenReturn(Optional.of(doctor));

        when(exceptionRepository.findByDoctorIdOrderByStartsAtAsc(1L))
                .thenReturn(List.of(exception));

        List<DoctorScheduleExceptionResponse> result =
                exceptionService.getExceptions(1L);

        assertEquals(1, result.size());
        assertEquals(startsAt, result.get(0).startsAt());
        assertEquals(endsAt, result.get(0).endsAt());
        assertEquals(
                ScheduleExceptionType.UNAVAILABLE,
                result.get(0).exceptionType()
        );
        assertEquals(
                ScheduleExceptionReason.TRAINING,
                result.get(0).reasonType()
        );
    }

    @Test
    void shouldCancelFutureScheduleException() {

        LocalDateTime startsAt =
                LocalDateTime.now().plusDays(2);

        LocalDateTime endsAt =
                startsAt.plusHours(3);

        DoctorScheduleException exception =
                new DoctorScheduleException(
                        doctor,
                        startsAt,
                        endsAt,
                        ScheduleExceptionType.UNAVAILABLE,
                        ScheduleExceptionReason.VACATION,
                        "Vacation"
                );

        when(doctorProfileRepository.findById(1L))
                .thenReturn(Optional.of(doctor));

        when(exceptionRepository.findByIdAndDoctorId(10L, 1L))
                .thenReturn(Optional.of(exception));

        exceptionService.cancelException(1L, 10L);

        verify(exceptionRepository).delete(exception);
    }

    @Test
    void shouldRejectCancellingPastScheduleException() {

        LocalDateTime startsAt =
                LocalDateTime.now().minusDays(1);

        LocalDateTime endsAt =
                LocalDateTime.now().plusHours(1);

        DoctorScheduleException exception =
                new DoctorScheduleException(
                        doctor,
                        startsAt,
                        endsAt,
                        ScheduleExceptionType.UNAVAILABLE,
                        ScheduleExceptionReason.SICK_LEAVE,
                        "Sick leave"
                );

        when(doctorProfileRepository.findById(1L))
                .thenReturn(Optional.of(doctor));

        when(exceptionRepository.findByIdAndDoctorId(10L, 1L))
                .thenReturn(Optional.of(exception));

        assertThrows(
                CannotCancelPastScheduleException.class,
                () -> exceptionService.cancelException(1L, 10L)
        );

        verify(exceptionRepository, never())
                .delete(any(DoctorScheduleException.class));
    }

    @Test
    void shouldRejectCancellingUnknownScheduleException() {

        when(doctorProfileRepository.findById(1L))
                .thenReturn(Optional.of(doctor));

        when(exceptionRepository.findByIdAndDoctorId(999L, 1L))
                .thenReturn(Optional.empty());

        assertThrows(
                ScheduleExceptionNotFoundException.class,
                () -> exceptionService.cancelException(1L, 999L)
        );

        verify(exceptionRepository, never())
                .delete(any(DoctorScheduleException.class));
    }

    @Test
    void shouldRejectUnknownDoctor() {

        LocalDateTime startsAt =
                LocalDateTime.now().plusDays(1);

        LocalDateTime endsAt =
                startsAt.plusHours(2);

        DoctorScheduleExceptionRequest request =
                new DoctorScheduleExceptionRequest(
                        startsAt,
                        endsAt,
                        ScheduleExceptionType.UNAVAILABLE,
                        ScheduleExceptionReason.VACATION,
                        "Annual vacation"
                );

        when(doctorProfileRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThrows(
                DoctorNotFoundException.class,
                () -> exceptionService.createException(999L, request)
        );

        verify(exceptionRepository, never())
                .save(any(DoctorScheduleException.class));
    }

    @Test
    void shouldRejectRetrievingScheduleExceptionsForUnknownDoctor() {

        when(doctorProfileRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThrows(
                DoctorNotFoundException.class,
                () -> exceptionService.getExceptions(999L)
        );

        verifyNoInteractions(exceptionRepository);
    }
}