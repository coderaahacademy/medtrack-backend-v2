package com.coderaah.medtrack.appointment.service;

import com.coderaah.medtrack.appointment.domain.Appointment;
import com.coderaah.medtrack.appointment.domain.AppointmentStatus;
import com.coderaah.medtrack.appointment.dto.AppointmentRequest;
import com.coderaah.medtrack.appointment.dto.AppointmentResponse;
import com.coderaah.medtrack.appointment.exception.AppointmentNotFoundException;
import com.coderaah.medtrack.appointment.exception.AppointmentOverlapException;
import com.coderaah.medtrack.appointment.exception.CancellationReasonRequiredException;
import com.coderaah.medtrack.appointment.exception.DoctorNotActiveException;
import com.coderaah.medtrack.appointment.exception.DoctorNotAvailableException;
import com.coderaah.medtrack.appointment.exception.InvalidAppointmentTimeException;
import com.coderaah.medtrack.appointment.exception.InvalidStatusTransitionException;
import com.coderaah.medtrack.appointment.mapper.AppointmentMapper;
import com.coderaah.medtrack.appointment.repository.AppointmentRepository;
import com.coderaah.medtrack.doctor.domain.DoctorAvailabilityRule;
import com.coderaah.medtrack.doctor.domain.DoctorProfile;
import com.coderaah.medtrack.doctor.domain.DoctorScheduleException;
import com.coderaah.medtrack.doctor.domain.ScheduleExceptionType;
import com.coderaah.medtrack.doctor.exception.DoctorNotFoundException;
import com.coderaah.medtrack.doctor.repository.DoctorAvailabilityRuleRepository;
import com.coderaah.medtrack.doctor.repository.DoctorProfileRepository;
import com.coderaah.medtrack.doctor.repository.DoctorScheduleExceptionRepository;
import com.coderaah.medtrack.patient.domain.PatientProfile;
import com.coderaah.medtrack.patient.exception.PatientNotFoundException;
import com.coderaah.medtrack.patient.repository.PatientProfileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
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
    @Mock AppointmentStatusHistoryService appointmentStatusHistoryService;
    @Mock AppointmentMapper appointmentMapper;

    @InjectMocks AppointmentService appointmentService;

    private static final Long DOCTOR_ID = 1L;
    private static final Long PATIENT_ID = 2L;
    private static final Long ACTOR_ID = 9L;

    private final LocalDateTime start = LocalDateTime.of(2026, 9, 10, 10, 0);
    private final LocalDateTime end = LocalDateTime.of(2026, 9, 10, 11, 0);

    private DoctorProfile doctor;
    private PatientProfile patient;
    private AppointmentRequest request;

    @BeforeEach
    void setUp() {
        doctor = new DoctorProfile();
        doctor.setId(DOCTOR_ID);
        doctor.setActive(true);

        patient = new PatientProfile();
        patient.setId(PATIENT_ID);

        request = new AppointmentRequest();
        request.setDoctorId(DOCTOR_ID);
        request.setPatientId(PATIENT_ID);
        request.setScheduledStart(start);
        request.setScheduledEnd(end);
    }

    private DoctorAvailabilityRule regularRule() {
        DoctorAvailabilityRule rule = new DoctorAvailabilityRule();
        rule.setDoctor(doctor);
        rule.setDayOfWeek(start.getDayOfWeek());
        rule.setStartTime(LocalTime.of(9, 0));
        rule.setEndTime(LocalTime.of(17, 0));
        rule.setActive(true);
        return rule;
    }

    private DoctorScheduleException scheduleException(ScheduleExceptionType type,
                                                      LocalDateTime from,
                                                      LocalDateTime to) {
        DoctorScheduleException exception = new DoctorScheduleException();
        exception.setDoctor(doctor);
        exception.setExceptionType(type);
        exception.setStartsAt(from);
        exception.setEndsAt(to);
        return exception;
    }

    private Appointment existingAppointment(LocalDateTime from, LocalDateTime to, AppointmentStatus status) {
        Appointment existing = new Appointment();
        existing.setScheduledStart(from);
        existing.setScheduledEnd(to);
        existing.setStatus(status);
        return existing;
    }

    private void givenPatientAndDoctorExist() {
        when(patientProfileRepository.findById(PATIENT_ID)).thenReturn(Optional.of(patient));
        when(doctorProfileRepository.findById(DOCTOR_ID)).thenReturn(Optional.of(doctor));
    }

    private void givenBookingWillBeSaved() {
        Appointment mapped = new Appointment();
        when(appointmentMapper.toEntity(request, patient, doctor)).thenReturn(mapped);
        when(appointmentRepository.save(mapped)).thenReturn(mapped);
        when(appointmentMapper.toResponse(mapped)).thenReturn(new AppointmentResponse());
    }

    // ---------- booking ----------

    @Test
    void createAppointment_savesScheduledAppointment_whenDoctorIsAvailable() {
        givenPatientAndDoctorExist();
        when(scheduleExceptionRepository.findByDoctorIdOrderByStartsAtAsc(DOCTOR_ID)).thenReturn(List.of());
        when(availabilityRepository.findByDoctorIdAndActiveTrueOrderByDayOfWeekAscStartTimeAsc(DOCTOR_ID))
                .thenReturn(List.of(regularRule()));
        when(appointmentRepository.findByDoctorIdAndStatusIn(anyLong(), any())).thenReturn(List.of());
        Appointment mapped = new Appointment();
        when(appointmentMapper.toEntity(request, patient, doctor)).thenReturn(mapped);
        when(appointmentRepository.save(mapped)).thenReturn(mapped);
        when(appointmentMapper.toResponse(mapped)).thenReturn(new AppointmentResponse());

        AppointmentResponse result = appointmentService.createAppointment(request);

        assertThat(result).isNotNull();
        assertThat(mapped.getStatus()).isEqualTo(AppointmentStatus.SCHEDULED);
        verify(appointmentRepository).save(mapped);
    }

    @Test
    void createAppointment_allowsBooking_whenCoveredByAvailableOverride() {
        givenPatientAndDoctorExist();
        when(scheduleExceptionRepository.findByDoctorIdOrderByStartsAtAsc(DOCTOR_ID))
                .thenReturn(List.of(scheduleException(ScheduleExceptionType.AVAILABLE_OVERRIDE,
                        start.minusHours(1), end.plusHours(1))));
        when(availabilityRepository.findByDoctorIdAndActiveTrueOrderByDayOfWeekAscStartTimeAsc(DOCTOR_ID))
                .thenReturn(List.of());
        when(appointmentRepository.findByDoctorIdAndStatusIn(anyLong(), any())).thenReturn(List.of());
        givenBookingWillBeSaved();

        AppointmentResponse result = appointmentService.createAppointment(request);

        assertThat(result).isNotNull();
        verify(appointmentRepository).save(any(Appointment.class));
    }

    @Test
    void createAppointment_rejects_whenAvailableOverrideDoesNotCoverTheWholeSlot() {
        givenPatientAndDoctorExist();
        when(scheduleExceptionRepository.findByDoctorIdOrderByStartsAtAsc(DOCTOR_ID))
                .thenReturn(List.of(scheduleException(ScheduleExceptionType.AVAILABLE_OVERRIDE,
                        start.plusMinutes(30), end.plusHours(1))));
        when(availabilityRepository.findByDoctorIdAndActiveTrueOrderByDayOfWeekAscStartTimeAsc(DOCTOR_ID))
                .thenReturn(List.of());

        assertThatThrownBy(() -> appointmentService.createAppointment(request))
                .isInstanceOf(DoctorNotAvailableException.class);

        verify(appointmentRepository, never()).save(any());
    }

    @Test
    void createAppointment_rejects_whenOutsideRegularAvailability() {
        givenPatientAndDoctorExist();
        when(scheduleExceptionRepository.findByDoctorIdOrderByStartsAtAsc(DOCTOR_ID)).thenReturn(List.of());
        when(availabilityRepository.findByDoctorIdAndActiveTrueOrderByDayOfWeekAscStartTimeAsc(DOCTOR_ID))
                .thenReturn(List.of());

        assertThatThrownBy(() -> appointmentService.createAppointment(request))
                .isInstanceOf(DoctorNotAvailableException.class)
                .hasMessageContaining("availability");

        verify(appointmentRepository, never()).save(any());
    }

    @Test
    void createAppointment_rejects_whenDoctorIsOnLeave_evenWithinRegularHours() {
        givenPatientAndDoctorExist();
        when(scheduleExceptionRepository.findByDoctorIdOrderByStartsAtAsc(DOCTOR_ID))
                .thenReturn(List.of(scheduleException(ScheduleExceptionType.UNAVAILABLE,
                        start.minusHours(2), end.plusHours(8))));

        assertThatThrownBy(() -> appointmentService.createAppointment(request))
                .isInstanceOf(DoctorNotAvailableException.class)
                .hasMessageContaining("unavailable");

        verify(appointmentRepository, never()).save(any());
    }

    @Test
    void createAppointment_rejects_whenOverlappingAnActiveAppointment() {
        givenPatientAndDoctorExist();
        when(scheduleExceptionRepository.findByDoctorIdOrderByStartsAtAsc(DOCTOR_ID)).thenReturn(List.of());
        when(availabilityRepository.findByDoctorIdAndActiveTrueOrderByDayOfWeekAscStartTimeAsc(DOCTOR_ID))
                .thenReturn(List.of(regularRule()));
        when(appointmentRepository.findByDoctorIdAndStatusIn(anyLong(), any()))
                .thenReturn(List.of(existingAppointment(
                        start.plusMinutes(30), end.plusMinutes(30), AppointmentStatus.CONFIRMED)));

        assertThatThrownBy(() -> appointmentService.createAppointment(request))
                .isInstanceOf(AppointmentOverlapException.class);

        verify(appointmentRepository, never()).save(any());
    }

    @Test
    void createAppointment_allowsBackToBackAppointments() {
        givenPatientAndDoctorExist();
        when(scheduleExceptionRepository.findByDoctorIdOrderByStartsAtAsc(DOCTOR_ID)).thenReturn(List.of());
        when(availabilityRepository.findByDoctorIdAndActiveTrueOrderByDayOfWeekAscStartTimeAsc(DOCTOR_ID))
                .thenReturn(List.of(regularRule()));
        when(appointmentRepository.findByDoctorIdAndStatusIn(anyLong(), any()))
                .thenReturn(List.of(existingAppointment(
                        start.minusHours(1), start, AppointmentStatus.SCHEDULED)));
        givenBookingWillBeSaved();

        assertThat(appointmentService.createAppointment(request)).isNotNull();
    }

    @Test
    void createAppointment_rejects_whenStartIsNotBeforeEnd() {
        request.setScheduledStart(end);
        request.setScheduledEnd(end);

        assertThatThrownBy(() -> appointmentService.createAppointment(request))
                .isInstanceOf(InvalidAppointmentTimeException.class);

        verify(appointmentRepository, never()).save(any());
    }

    @Test
    void createAppointment_rejects_whenDoctorIsNotActive() {
        doctor.setActive(false);
        givenPatientAndDoctorExist();

        assertThatThrownBy(() -> appointmentService.createAppointment(request))
                .isInstanceOf(DoctorNotActiveException.class);

        verify(appointmentRepository, never()).save(any());
    }

    @Test
    void createAppointment_rejects_whenPatientDoesNotExist() {
        when(patientProfileRepository.findById(PATIENT_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> appointmentService.createAppointment(request))
                .isInstanceOf(PatientNotFoundException.class);
    }

    @Test
    void createAppointment_rejects_whenDoctorDoesNotExist() {
        when(patientProfileRepository.findById(PATIENT_ID)).thenReturn(Optional.of(patient));
        when(doctorProfileRepository.findById(DOCTOR_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> appointmentService.createAppointment(request))
                .isInstanceOf(DoctorNotFoundException.class);
    }

    // ---------- queries ----------

    @Test
    void getById_throwsAppointmentNotFound_whenMissing() {
        when(appointmentRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> appointmentService.getById(999L))
                .isInstanceOf(AppointmentNotFoundException.class);
    }

    @Test
    void getByPatient_mapsEveryAppointment() {
        Appointment first = new Appointment();
        Appointment second = new Appointment();
        when(appointmentRepository.findByPatientId(PATIENT_ID)).thenReturn(List.of(first, second));
        when(appointmentMapper.toResponse(any(Appointment.class))).thenReturn(new AppointmentResponse());

        assertThat(appointmentService.getByPatient(PATIENT_ID)).hasSize(2);
    }

    @Test
    void getByDoctorAndDate_queriesTheWholeDay() {
        LocalDate day = LocalDate.of(2026, 9, 10);
        when(appointmentRepository.findByDoctorIdAndScheduledStartBetween(
                DOCTOR_ID, day.atStartOfDay(), day.atTime(LocalTime.MAX))).thenReturn(List.of());

        assertThat(appointmentService.getByDoctorAndDate(DOCTOR_ID, day)).isEmpty();
    }

    // ---------- lifecycle: valid transitions ----------

    private Appointment appointmentWithStatus(AppointmentStatus status) {
        Appointment appointment = new Appointment();
        appointment.setId(50L);
        appointment.setStatus(status);
        when(appointmentRepository.findById(50L)).thenReturn(Optional.of(appointment));
        when(appointmentRepository.save(appointment)).thenReturn(appointment);
        when(appointmentMapper.toResponse(appointment)).thenReturn(new AppointmentResponse());
        return appointment;
    }

    @Test
    void confirm_movesScheduledToConfirmed_andRecordsHistory() {
        Appointment appointment = appointmentWithStatus(AppointmentStatus.SCHEDULED);

        appointmentService.confirm(50L, ACTOR_ID);

        assertThat(appointment.getStatus()).isEqualTo(AppointmentStatus.CONFIRMED);
        verify(appointmentStatusHistoryService).record(
                appointment, AppointmentStatus.SCHEDULED, AppointmentStatus.CONFIRMED, ACTOR_ID, null);
    }

    @Test
    void cancel_movesScheduledToCancelled_storesReason_andRecordsHistory() {
        Appointment appointment = appointmentWithStatus(AppointmentStatus.SCHEDULED);

        appointmentService.cancel(50L, ACTOR_ID, "Patient requested");

        assertThat(appointment.getStatus()).isEqualTo(AppointmentStatus.CANCELLED);
        assertThat(appointment.getCancellationReason()).isEqualTo("Patient requested");
        verify(appointmentStatusHistoryService).record(
                appointment, AppointmentStatus.SCHEDULED, AppointmentStatus.CANCELLED, ACTOR_ID, "Patient requested");
    }

    @Test
    void cancel_alsoWorksFromConfirmed() {
        Appointment appointment = appointmentWithStatus(AppointmentStatus.CONFIRMED);

        appointmentService.cancel(50L, ACTOR_ID, "Doctor unavailable");

        assertThat(appointment.getStatus()).isEqualTo(AppointmentStatus.CANCELLED);
        verify(appointmentStatusHistoryService).record(
                appointment, AppointmentStatus.CONFIRMED, AppointmentStatus.CANCELLED, ACTOR_ID, "Doctor unavailable");
    }

    @Test
    void complete_movesConfirmedToCompleted_andRecordsHistory() {
        Appointment appointment = appointmentWithStatus(AppointmentStatus.CONFIRMED);

        appointmentService.complete(50L, ACTOR_ID);

        assertThat(appointment.getStatus()).isEqualTo(AppointmentStatus.COMPLETED);
        verify(appointmentStatusHistoryService).record(
                appointment, AppointmentStatus.CONFIRMED, AppointmentStatus.COMPLETED, ACTOR_ID, null);
    }

    @Test
    void noShow_movesConfirmedToNoShow_andRecordsHistory() {
        Appointment appointment = appointmentWithStatus(AppointmentStatus.CONFIRMED);

        appointmentService.noShow(50L, ACTOR_ID);

        assertThat(appointment.getStatus()).isEqualTo(AppointmentStatus.NO_SHOW);
        verify(appointmentStatusHistoryService).record(
                appointment, AppointmentStatus.CONFIRMED, AppointmentStatus.NO_SHOW, ACTOR_ID, null);
    }

    // ---------- lifecycle: invalid transitions ----------

    private void assertTransitionRejected(AppointmentStatus current, Runnable action) {
        Appointment appointment = new Appointment();
        appointment.setId(50L);
        appointment.setStatus(current);
        when(appointmentRepository.findById(50L)).thenReturn(Optional.of(appointment));

        assertThatThrownBy(action::run).isInstanceOf(InvalidStatusTransitionException.class);

        assertThat(appointment.getStatus()).isEqualTo(current);
        verify(appointmentRepository, never()).save(any());
        verify(appointmentStatusHistoryService, never()).record(any(), any(), any(), any(), any());
    }

    @Test
    void confirm_rejects_whenAlreadyCompleted() {
        assertTransitionRejected(AppointmentStatus.COMPLETED, () -> appointmentService.confirm(50L, ACTOR_ID));
    }

    @Test
    void complete_rejects_whenStillScheduled() {
        assertTransitionRejected(AppointmentStatus.SCHEDULED, () -> appointmentService.complete(50L, ACTOR_ID));
    }

    @Test
    void noShow_rejects_whenStillScheduled() {
        assertTransitionRejected(AppointmentStatus.SCHEDULED, () -> appointmentService.noShow(50L, ACTOR_ID));
    }

    @Test
    void cancel_rejects_whenAlreadyCompleted() {
        assertTransitionRejected(AppointmentStatus.COMPLETED,
                () -> appointmentService.cancel(50L, ACTOR_ID, "Too late"));
    }

    @Test
    void confirm_rejects_whenAlreadyCancelled() {
        assertTransitionRejected(AppointmentStatus.CANCELLED, () -> appointmentService.confirm(50L, ACTOR_ID));
    }

    @Test
    void noShow_rejects_whenAlreadyNoShow() {
        assertTransitionRejected(AppointmentStatus.NO_SHOW, () -> appointmentService.noShow(50L, ACTOR_ID));
    }

    @Test
    void cancel_rejects_whenReasonIsBlank() {
        assertThatThrownBy(() -> appointmentService.cancel(50L, ACTOR_ID, "  "))
                .isInstanceOf(CancellationReasonRequiredException.class);

        verify(appointmentRepository, never()).save(any());
    }

    @Test
    void confirm_throwsAppointmentNotFound_whenMissing() {
        when(appointmentRepository.findById(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> appointmentService.confirm(404L, ACTOR_ID))
                .isInstanceOf(AppointmentNotFoundException.class);
    }
}
