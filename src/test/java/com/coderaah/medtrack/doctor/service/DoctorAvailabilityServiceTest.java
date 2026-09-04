package com.coderaah.medtrack.doctor.service;

import com.coderaah.medtrack.doctor.domain.DoctorAvailabilityRule;
import com.coderaah.medtrack.doctor.domain.DoctorProfile;
import com.coderaah.medtrack.doctor.dto.DoctorAvailabilityRuleRequest;
import com.coderaah.medtrack.doctor.dto.DoctorAvailabilityRuleResponse;
import com.coderaah.medtrack.doctor.exception.AvailabilityRuleNotFoundException;
import com.coderaah.medtrack.doctor.exception.DoctorNotFoundException;
import com.coderaah.medtrack.doctor.exception.InvalidScheduleTimeRangeException;
import com.coderaah.medtrack.doctor.repository.DoctorAvailabilityRuleRepository;
import com.coderaah.medtrack.doctor.repository.DoctorProfileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DoctorAvailabilityServiceTest {

    @Mock
    private DoctorAvailabilityRuleRepository availabilityRuleRepository;

    @Mock
    private DoctorProfileRepository doctorProfileRepository;

    @InjectMocks
    private DoctorAvailabilityService availabilityService;

    private DoctorProfile doctor;

    @BeforeEach
    void setUp() {
        doctor = new DoctorProfile();
    }

    @Test
    void shouldAddAvailabilityRule() {

        DoctorAvailabilityRuleRequest request =
                new DoctorAvailabilityRuleRequest(
                        DayOfWeek.MONDAY,
                        LocalTime.of(9, 0),
                        LocalTime.of(13, 0)
                );

        DoctorAvailabilityRule rule =
                new DoctorAvailabilityRule(
                        doctor,
                        request.dayOfWeek(),
                        request.startTime(),
                        request.endTime()
                );

        when(doctorProfileRepository.findById(1L))
                .thenReturn(Optional.of(doctor));

        when(availabilityRuleRepository.save(any(DoctorAvailabilityRule.class)))
                .thenReturn(rule);

        DoctorAvailabilityRuleResponse response =
                availabilityService.addRule(1L, request);

        assertEquals(DayOfWeek.MONDAY, response.dayOfWeek());
        assertEquals(LocalTime.of(9, 0), response.startTime());
        assertEquals(LocalTime.of(13, 0), response.endTime());
        assertTrue(response.active());
    }

    @Test
    void shouldRejectInvalidAvailabilityTimeRange() {

        DoctorAvailabilityRuleRequest request =
                new DoctorAvailabilityRuleRequest(
                        DayOfWeek.MONDAY,
                        LocalTime.of(14, 0),
                        LocalTime.of(9, 0)
                );

        assertThrows(
                InvalidScheduleTimeRangeException.class,
                () -> availabilityService.addRule(1L, request)
        );

        verifyNoInteractions(
                doctorProfileRepository,
                availabilityRuleRepository
        );
    }

    @Test
    void shouldGetAvailabilityRules() {

        DoctorAvailabilityRule rule =
                new DoctorAvailabilityRule(
                        doctor,
                        DayOfWeek.MONDAY,
                        LocalTime.of(9, 0),
                        LocalTime.of(13, 0)
                );

        when(doctorProfileRepository.findById(1L))
                .thenReturn(Optional.of(doctor));

        when(availabilityRuleRepository
                .findByDoctorIdAndActiveTrueOrderByDayOfWeekAscStartTimeAsc(1L))
                .thenReturn(List.of(rule));

        List<DoctorAvailabilityRuleResponse> result =
                availabilityService.getRules(1L);

        assertEquals(1, result.size());
        assertEquals(DayOfWeek.MONDAY, result.get(0).dayOfWeek());
        assertEquals(LocalTime.of(9, 0), result.get(0).startTime());
        assertEquals(LocalTime.of(13, 0), result.get(0).endTime());
        assertTrue(result.get(0).active());
    }

    @Test
    void shouldNotReturnInactiveAvailabilityRules() {

        DoctorAvailabilityRule activeRule =
                new DoctorAvailabilityRule(
                        doctor,
                        DayOfWeek.MONDAY,
                        LocalTime.of(9, 0),
                        LocalTime.of(13, 0)
                );

        when(doctorProfileRepository.findById(1L))
                .thenReturn(Optional.of(doctor));

        when(availabilityRuleRepository
                .findByDoctorIdAndActiveTrueOrderByDayOfWeekAscStartTimeAsc(1L))
                .thenReturn(List.of(activeRule));

        List<DoctorAvailabilityRuleResponse> result =
                availabilityService.getRules(1L);

        assertEquals(1, result.size());
        assertTrue(result.get(0).active());
    }

    @Test
    void shouldUpdateAvailabilityRule() {

        DoctorAvailabilityRuleRequest request =
                new DoctorAvailabilityRuleRequest(
                        DayOfWeek.TUESDAY,
                        LocalTime.of(10, 0),
                        LocalTime.of(16, 0)
                );

        DoctorAvailabilityRule rule =
                new DoctorAvailabilityRule(
                        doctor,
                        DayOfWeek.MONDAY,
                        LocalTime.of(9, 0),
                        LocalTime.of(13, 0)
                );

        when(doctorProfileRepository.findById(1L))
                .thenReturn(Optional.of(doctor));

        when(availabilityRuleRepository
                .findByIdAndDoctorId(10L, 1L))
                .thenReturn(Optional.of(rule));

        when(availabilityRuleRepository.save(rule))
                .thenReturn(rule);

        DoctorAvailabilityRuleResponse response =
                availabilityService.updateRule(
                        1L,
                        10L,
                        request
                );

        assertEquals(DayOfWeek.TUESDAY, response.dayOfWeek());
        assertEquals(LocalTime.of(10, 0), response.startTime());
        assertEquals(LocalTime.of(16, 0), response.endTime());
    }

    @Test
    void shouldDeactivateAvailabilityRule() {

        DoctorAvailabilityRule rule =
                new DoctorAvailabilityRule(
                        doctor,
                        DayOfWeek.MONDAY,
                        LocalTime.of(9, 0),
                        LocalTime.of(13, 0)
                );

        when(doctorProfileRepository.findById(1L))
                .thenReturn(Optional.of(doctor));

        when(availabilityRuleRepository
                .findByIdAndDoctorId(10L, 1L))
                .thenReturn(Optional.of(rule));

        availabilityService.deactivateRule(1L, 10L);

        assertFalse(rule.isActive());

        verify(availabilityRuleRepository)
                .save(rule);
    }

    @Test
    void shouldRejectUnknownDoctor() {

        DoctorAvailabilityRuleRequest request =
                new DoctorAvailabilityRuleRequest(
                        DayOfWeek.MONDAY,
                        LocalTime.of(9, 0),
                        LocalTime.of(13, 0)
                );

        when(doctorProfileRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThrows(
                DoctorNotFoundException.class,
                () -> availabilityService.addRule(999L, request)
        );

        verify(availabilityRuleRepository, never())
                .save(any(DoctorAvailabilityRule.class));
    }

    @Test
    void shouldRejectUpdatingUnknownAvailabilityRule() {

        DoctorAvailabilityRuleRequest request =
                new DoctorAvailabilityRuleRequest(
                        DayOfWeek.TUESDAY,
                        LocalTime.of(10, 0),
                        LocalTime.of(16, 0)
                );

        when(doctorProfileRepository.findById(1L))
                .thenReturn(Optional.of(doctor));

        when(availabilityRuleRepository
                .findByIdAndDoctorId(999L, 1L))
                .thenReturn(Optional.empty());

        assertThrows(
                AvailabilityRuleNotFoundException.class,
                () -> availabilityService.updateRule(
                        1L,
                        999L,
                        request
                )
        );

        verify(availabilityRuleRepository, never())
                .save(any(DoctorAvailabilityRule.class));
    }
}