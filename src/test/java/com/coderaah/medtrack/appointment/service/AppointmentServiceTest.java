package com.coderaah.medtrack.appointment.service;

import com.coderaah.medtrack.appointment.domain.Appointment;
import com.coderaah.medtrack.appointment.domain.AppointmentStatus;
import com.coderaah.medtrack.appointment.domain.AppointmentStatusHistory;
import com.coderaah.medtrack.appointment.dto.requestDto.AppointmentRequestDto;
import com.coderaah.medtrack.appointment.dto.responeDto.AppointmentResponseDto;
import com.coderaah.medtrack.appointment.mapper.AppointmentMapper;
import com.coderaah.medtrack.appointment.repository.AppointmentRepository;
import com.coderaah.medtrack.appointment.repository.AppointmentStatusHistoryRepository;
import com.coderaah.medtrack.doctor.domain.DoctorAvailabilityRule;
import com.coderaah.medtrack.doctor.domain.DoctorProfile;
import com.coderaah.medtrack.doctor.domain.DoctorScheduleException;
import com.coderaah.medtrack.doctor.domain.ScheduleExceptionType;
import com.coderaah.medtrack.doctor.repository.DoctorAvailabilityRuleRepository;
import com.coderaah.medtrack.doctor.repository.DoctorProfileRepository;
import com.coderaah.medtrack.doctor.repository.DoctorScheduleExceptionRepository;
import com.coderaah.medtrack.identity.domain.UserAccount;
import com.coderaah.medtrack.identity.repository.UserAccountRepository;
import com.coderaah.medtrack.patient.domain.PatientProfile;
import com.coderaah.medtrack.patient.repository.PatientProfileRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AppointmentServiceTest {


    @Mock AppointmentRepository appointmentRepository;
    @Mock PatientProfileRepository patientProfileRepository;
    @Mock DoctorProfileRepository doctorProfileRepository;
    @Mock DoctorAvailabilityRuleRepository availabilityRepository;
    @Mock DoctorScheduleExceptionRepository scheduleExceptionRepository;
    @Mock AppointmentStatusHistoryRepository statusHistoryRepository;
    @Mock UserAccountRepository userAccountRepository;
    @Mock AppointmentMapper appointmentMapper;

    @InjectMocks AppointmentService appointmentService;

    private final Long doctorId = 1L;
    private final Long patientId = 2L;
    private final Long actorUserId = 9L;

    private final LocalDateTime start = LocalDateTime.of(2026, 9, 10, 10, 0);
    private final LocalDateTime end = LocalDateTime.of(2026, 9, 10, 11, 0);

    private DoctorProfile doctor;
    private PatientProfile patient;
    private AppointmentRequestDto request;

    @BeforeEach
    void setUp() {
        doctor = new DoctorProfile();
        doctor.setId(doctorId);
        doctor.setActive(true);

        patient = new PatientProfile();
        patient.setId(patientId);

        request = new AppointmentRequestDto();
        request.setDoctorId(doctorId);
        request.setPatientId(patientId);
        request.setScheduledStart(start);
        request.setScheduledEnd(end);
    }


    private DoctorAvailabilityRule matchingRule() {
        DoctorAvailabilityRule rule = new DoctorAvailabilityRule();
        rule.setDoctor(doctor);
        rule.setDayOfWeek(start.getDayOfWeek());
        rule.setStartTime(LocalTime.of(9, 0));
        rule.setEndTime(LocalTime.of(17, 0));
        rule.setActive(true);
        return rule;
    }

    // CREATE

    @Test
    void createAppointment_succeeds_whenDoctorAvailableAndNoConflict() {
        when(patientProfileRepository.findById(patientId)).thenReturn(Optional.of(patient));
        when(doctorProfileRepository.findById(doctorId)).thenReturn(Optional.of(doctor));
        when(scheduleExceptionRepository.findByDoctorIdOrderByStartsAtAsc(doctorId))
                .thenReturn(List.of());
        when(availabilityRepository
                .findByDoctorIdAndActiveTrueOrderByDayOfWeekAscStartTimeAsc(doctorId))
                .thenReturn(List.of(matchingRule()));
        when(appointmentRepository.findByDoctorIdAndStatusIn(any(), any()))
                .thenReturn(List.of());

        Appointment mapped = new Appointment();
        when(appointmentMapper.convertAppointmentRequestDtoToAppointment(request, patient, doctor))
                .thenReturn(mapped);
        when(appointmentRepository.save(mapped)).thenReturn(mapped);
        when(appointmentMapper.convertAppointmentToAppointmentResponseDto(mapped))
                .thenReturn(new AppointmentResponseDto());

        AppointmentResponseDto result = appointmentService.createAppointment(request);

        assertThat(result).isNotNull();
        assertThat(mapped.getStatus()).isEqualTo(AppointmentStatus.SCHEDULED);
        verify(appointmentRepository).save(mapped);
    }

    @Test
    void createAppointment_rejects_whenStartIsNotBeforeEnd() {
        request.setScheduledStart(end);
        request.setScheduledEnd(end);

        assertThatThrownBy(() -> appointmentService.createAppointment(request))
                .isInstanceOf(IllegalArgumentException.class);

        verify(appointmentRepository, never()).save(any());
    }

    @Test
    void createAppointment_rejects_whenDoctorIsNotActive() {
        doctor.setActive(false);
        when(patientProfileRepository.findById(patientId)).thenReturn(Optional.of(patient));
        when(doctorProfileRepository.findById(doctorId)).thenReturn(Optional.of(doctor));

        assertThatThrownBy(() -> appointmentService.createAppointment(request))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void createAppointment_rejects_whenOutsideAvailability() {
        when(patientProfileRepository.findById(patientId)).thenReturn(Optional.of(patient));
        when(doctorProfileRepository.findById(doctorId)).thenReturn(Optional.of(doctor));
        when(scheduleExceptionRepository.findByDoctorIdOrderByStartsAtAsc(doctorId))
                .thenReturn(List.of());
        when(availabilityRepository
                .findByDoctorIdAndActiveTrueOrderByDayOfWeekAscStartTimeAsc(doctorId))
                .thenReturn(List.of());

        assertThatThrownBy(() -> appointmentService.createAppointment(request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("availability");
    }

    @Test
    void createAppointment_rejects_whenDoctorIsOnLeave() {
        DoctorScheduleException leave = new DoctorScheduleException();
        leave.setDoctor(doctor);
        leave.setExceptionType(ScheduleExceptionType.UNAVAILABLE);
        leave.setStartsAt(LocalDateTime.of(2026, 9, 10, 8, 0));
        leave.setEndsAt(LocalDateTime.of(2026, 9, 10, 18, 0));

        when(patientProfileRepository.findById(patientId)).thenReturn(Optional.of(patient));
        when(doctorProfileRepository.findById(doctorId)).thenReturn(Optional.of(doctor));
        when(scheduleExceptionRepository.findByDoctorIdOrderByStartsAtAsc(doctorId))
                .thenReturn(List.of(leave));

        assertThatThrownBy(() -> appointmentService.createAppointment(request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("unavailable");
    }

    @Test
    void createAppointment_rejects_whenOverlappingAnotherAppointment() {
        Appointment existing = new Appointment();
        existing.setScheduledStart(LocalDateTime.of(2026, 9, 10, 10, 30));
        existing.setScheduledEnd(LocalDateTime.of(2026, 9, 10, 11, 30));
        existing.setStatus(AppointmentStatus.CONFIRMED);

        when(patientProfileRepository.findById(patientId)).thenReturn(Optional.of(patient));
        when(doctorProfileRepository.findById(doctorId)).thenReturn(Optional.of(doctor));
        when(scheduleExceptionRepository.findByDoctorIdOrderByStartsAtAsc(doctorId))
                .thenReturn(List.of());
        when(availabilityRepository
                .findByDoctorIdAndActiveTrueOrderByDayOfWeekAscStartTimeAsc(doctorId))
                .thenReturn(List.of(matchingRule()));
        when(appointmentRepository.findByDoctorIdAndStatusIn(any(), any()))
                .thenReturn(List.of(existing));

        assertThatThrownBy(() -> appointmentService.createAppointment(request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("overlapping");
    }

    // LIFECYCLE


    @Test
    void confirm_movesScheduledToConfirmed_andWritesHistory() {
        Appointment appointment = new Appointment();
        appointment.setId(50L);
        appointment.setStatus(AppointmentStatus.SCHEDULED);

        when(appointmentRepository.findById(50L)).thenReturn(Optional.of(appointment));
        when(appointmentRepository.save(any())).thenReturn(appointment);
        when(userAccountRepository.findById(actorUserId)).thenReturn(Optional.of(new UserAccount()));
        when(appointmentMapper.convertAppointmentToAppointmentResponseDto(any()))
                .thenReturn(new AppointmentResponseDto());

        appointmentService.confirm(50L, actorUserId);

        assertThat(appointment.getStatus()).isEqualTo(AppointmentStatus.CONFIRMED);
        verify(statusHistoryRepository).save(any(AppointmentStatusHistory.class));
    }

    @Test
    void confirm_rejects_whenAppointmentIsAlreadyCompleted() {
        Appointment appointment = new Appointment();
        appointment.setId(51L);
        appointment.setStatus(AppointmentStatus.COMPLETED);

        when(appointmentRepository.findById(51L)).thenReturn(Optional.of(appointment));

        assertThatThrownBy(() -> appointmentService.confirm(51L, actorUserId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid status transition");

        verify(statusHistoryRepository, never()).save(any());
    }

    @Test
    void cancel_rejects_whenReasonIsBlank() {
        Appointment appointment = new Appointment();
        appointment.setId(52L);
        appointment.setStatus(AppointmentStatus.SCHEDULED);

        when(appointmentRepository.findById(52L)).thenReturn(Optional.of(appointment));

        assertThatThrownBy(() -> appointmentService.cancel(52L, actorUserId, "  "))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void getById_throws_whenNotFound() {
        when(appointmentRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> appointmentService.getById(999L))
                .isInstanceOf(EntityNotFoundException.class);
    }
}
