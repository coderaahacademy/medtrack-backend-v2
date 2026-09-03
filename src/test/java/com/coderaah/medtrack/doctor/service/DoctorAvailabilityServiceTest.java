package com.coderaah.medtrack.doctor.service;

import com.coderaah.medtrack.doctor.domain.DoctorAvailabilityRule;
import com.coderaah.medtrack.doctor.domain.DoctorProfile;
import com.coderaah.medtrack.doctor.dto.DoctorAvailabilityRuleRequest;
import com.coderaah.medtrack.doctor.dto.DoctorAvailabilityRuleResponse;
import com.coderaah.medtrack.doctor.repository.DoctorAvailabilityRuleRepository;
import com.coderaah.medtrack.doctor.repository.DoctorProfileRepository;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DoctorAvailabilityServiceTest {

    @Mock
    private DoctorAvailabilityRuleRepository availabilityRuleRepository;

    @Mock
    private DoctorProfileRepository doctorProfileRepository;

    @InjectMocks
    private DoctorAvailabilityService service;

    @Test
    void shouldAddAvailabilityRule() {

        Long doctorId = 1L;

        DoctorProfile doctor = new DoctorProfile();
        doctor.setId(doctorId);

        DoctorAvailabilityRuleRequest request =
                new DoctorAvailabilityRuleRequest(
                        DayOfWeek.MONDAY,
                        LocalTime.of(9, 0),
                        LocalTime.of(13, 0)
                );

        when(doctorProfileRepository.findById(doctorId))
                .thenReturn(Optional.of(doctor));

        DoctorAvailabilityRule savedRule =
                new DoctorAvailabilityRule(
                        doctor,
                        request.dayOfWeek(),
                        request.startTime(),
                        request.endTime()
                );

        savedRule.setId(1L);
        when(availabilityRuleRepository.save(any(DoctorAvailabilityRule.class)))
                .thenReturn(savedRule);

        DoctorAvailabilityRuleResponse response =
                service.addRule(doctorId, request);

        assertNotNull(response);
        assertEquals(1L, response.id());
        assertEquals(doctorId, response.doctorId());
        assertEquals(DayOfWeek.MONDAY, response.dayOfWeek());
        assertEquals(LocalTime.of(9, 0), response.startTime());
        assertEquals(LocalTime.of(13, 0), response.endTime());
        assertTrue(response.active());
    }

    @Test
    void shouldRejectInvalidAvailabilityTimeRange() {

        Long doctorId = 1L;

        DoctorAvailabilityRuleRequest request =
                new DoctorAvailabilityRuleRequest(
                        DayOfWeek.MONDAY,
                        LocalTime.of(13, 0),
                        LocalTime.of(9, 0)
                );

        assertThrows(
                IllegalArgumentException.class,
                () -> service.addRule(doctorId, request)
        );
    }

    @Test
    void shouldGetAvailabilityRules() {

        Long doctorId = 1L;

        DoctorProfile doctor = new DoctorProfile();
        doctor.setId(doctorId);

        DoctorAvailabilityRule rule =
                new DoctorAvailabilityRule(
                        doctor,
                        DayOfWeek.MONDAY,
                        LocalTime.of(9, 0),
                        LocalTime.of(13, 0)
                );

        rule.setId(1L);

        when(doctorProfileRepository.findById(doctorId))
                .thenReturn(Optional.of(doctor));

        when(availabilityRuleRepository
                .findByDoctorIdOrderByDayOfWeekAscStartTimeAsc(doctorId))
                .thenReturn(List.of(rule));

        List<DoctorAvailabilityRuleResponse> response =
                service.getRules(doctorId);

        assertNotNull(response);
        assertEquals(1, response.size());
        assertEquals(1L, response.get(0).id());
        assertEquals(doctorId, response.get(0).doctorId());
        assertEquals(DayOfWeek.MONDAY, response.get(0).dayOfWeek());
        assertEquals(
                LocalTime.of(9, 0),
                response.get(0).startTime()
        );
        assertEquals(
                LocalTime.of(13, 0),
                response.get(0).endTime()
        );
    }

    @Test
    void shouldUpdateAvailabilityRule() {

        Long doctorId = 1L;
        Long ruleId = 1L;

        DoctorProfile doctor = new DoctorProfile();
        doctor.setId(doctorId);

        DoctorAvailabilityRule existingRule =
                new DoctorAvailabilityRule(
                        doctor,
                        DayOfWeek.MONDAY,
                        LocalTime.of(9, 0),
                        LocalTime.of(13, 0)
                );

        existingRule.setId(ruleId);

        DoctorAvailabilityRuleRequest request =
                new DoctorAvailabilityRuleRequest(
                        DayOfWeek.TUESDAY,
                        LocalTime.of(10, 0),
                        LocalTime.of(16, 0)
                );

        when(doctorProfileRepository.findById(doctorId))
                .thenReturn(Optional.of(doctor));

        when(availabilityRuleRepository
                .findByIdAndDoctorId(ruleId, doctorId))
                .thenReturn(Optional.of(existingRule));

        when(availabilityRuleRepository.save(existingRule))
                .thenReturn(existingRule);

        DoctorAvailabilityRuleResponse response =
                service.updateRule(
                        doctorId,
                        ruleId,
                        request
                );

        assertNotNull(response);
        assertEquals(ruleId, response.id());
        assertEquals(doctorId, response.doctorId());
        assertEquals(DayOfWeek.TUESDAY, response.dayOfWeek());
        assertEquals(
                LocalTime.of(10, 0),
                response.startTime()
        );
        assertEquals(
                LocalTime.of(16, 0),
                response.endTime()
        );
        assertTrue(response.active());
    }

    @Test
    void shouldDeactivateAvailabilityRule() {

        Long doctorId = 1L;
        Long ruleId = 1L;

        DoctorProfile doctor = new DoctorProfile();
        doctor.setId(doctorId);

        DoctorAvailabilityRule rule =
                new DoctorAvailabilityRule(
                        doctor,
                        DayOfWeek.MONDAY,
                        LocalTime.of(9, 0),
                        LocalTime.of(13, 0)
                );

        rule.setId(ruleId);

        when(doctorProfileRepository.findById(doctorId))
                .thenReturn(Optional.of(doctor));

        when(availabilityRuleRepository
                .findByIdAndDoctorId(ruleId, doctorId))
                .thenReturn(Optional.of(rule));

        service.deactivateRule(doctorId, ruleId);

        assertFalse(rule.isActive());

        verify(availabilityRuleRepository).save(rule);
    }
}