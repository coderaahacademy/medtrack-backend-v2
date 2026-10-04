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
import com.coderaah.medtrack.doctor.domain.DoctorProfile;
import com.coderaah.medtrack.doctor.domain.ScheduleExceptionType;
import com.coderaah.medtrack.doctor.exception.DoctorNotFoundException;
import com.coderaah.medtrack.doctor.repository.DoctorAvailabilityRuleRepository;
import com.coderaah.medtrack.doctor.repository.DoctorProfileRepository;
import com.coderaah.medtrack.doctor.repository.DoctorScheduleExceptionRepository;
import com.coderaah.medtrack.patient.domain.PatientProfile;
import com.coderaah.medtrack.patient.exception.PatientNotFoundException;
import com.coderaah.medtrack.patient.repository.PatientProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
@Transactional
public class AppointmentService {

    private static final List<AppointmentStatus> ACTIVE_STATUSES =
            List.of(AppointmentStatus.SCHEDULED, AppointmentStatus.CONFIRMED);

    private final AppointmentRepository appointmentRepository;
    private final PatientProfileRepository patientProfileRepository;
    private final DoctorProfileRepository doctorProfileRepository;
    private final DoctorAvailabilityRuleRepository availabilityRepository;
    private final DoctorScheduleExceptionRepository scheduleExceptionRepository;
    private final AppointmentStatusHistoryService appointmentStatusHistoryService;
    private final AppointmentMapper appointmentMapper;

    public AppointmentService(AppointmentRepository appointmentRepository,
                              PatientProfileRepository patientProfileRepository,
                              DoctorProfileRepository doctorProfileRepository,
                              DoctorAvailabilityRuleRepository availabilityRepository,
                              DoctorScheduleExceptionRepository scheduleExceptionRepository,
                              AppointmentStatusHistoryService appointmentStatusHistoryService,
                              AppointmentMapper appointmentMapper) {
        this.appointmentRepository = appointmentRepository;
        this.patientProfileRepository = patientProfileRepository;
        this.doctorProfileRepository = doctorProfileRepository;
        this.availabilityRepository = availabilityRepository;
        this.scheduleExceptionRepository = scheduleExceptionRepository;
        this.appointmentStatusHistoryService = appointmentStatusHistoryService;
        this.appointmentMapper = appointmentMapper;
    }

    // ---------- queries ----------

    public AppointmentResponse getById(Long id) {
        return appointmentMapper.toResponse(findAppointment(id));
    }

    public List<AppointmentResponse> getByPatient(Long patientId) {
        return appointmentRepository.findByPatientId(patientId).stream()
                .map(appointmentMapper::toResponse)
                .toList();
    }

    public List<AppointmentResponse> getByDoctor(Long doctorId) {
        return appointmentRepository.findByDoctorId(doctorId).stream()
                .map(appointmentMapper::toResponse)
                .toList();
    }

    public List<AppointmentResponse> getByDoctorAndDate(Long doctorId, LocalDate date) {
        LocalDateTime dayStart = date.atStartOfDay();
        LocalDateTime dayEnd = date.atTime(LocalTime.MAX);
        return appointmentRepository
                .findByDoctorIdAndScheduledStartBetween(doctorId, dayStart, dayEnd).stream()
                .map(appointmentMapper::toResponse)
                .toList();
    }

    // ---------- booking ----------

    public AppointmentResponse createAppointment(AppointmentRequest request) {
        LocalDateTime start = request.getScheduledStart();
        LocalDateTime end = request.getScheduledEnd();

        if (!start.isBefore(end)) {
            throw new InvalidAppointmentTimeException("Appointment start must be before its end");
        }

        PatientProfile patient = patientProfileRepository.findById(request.getPatientId())
                .orElseThrow(() -> new PatientNotFoundException("Patient not found"));
        DoctorProfile doctor = doctorProfileRepository.findById(request.getDoctorId())
                .orElseThrow(() -> new DoctorNotFoundException("Doctor not found"));

        if (!doctor.isActive()) {
            throw new DoctorNotActiveException("Doctor is not active");
        }

        Long doctorId = doctor.getId();
        assertDoctorAvailable(doctorId, start, end);
        assertNoOverlap(doctorId, start, end);

        Appointment appointment = appointmentMapper.toEntity(request, patient, doctor);
        appointment.setStatus(AppointmentStatus.SCHEDULED);

        return appointmentMapper.toResponse(appointmentRepository.save(appointment));
    }

    private void assertDoctorAvailable(Long doctorId, LocalDateTime start, LocalDateTime end) {
        var scheduleExceptions = scheduleExceptionRepository.findByDoctorIdOrderByStartsAtAsc(doctorId);

        boolean insideUnavailable = scheduleExceptions.stream()
                .filter(e -> e.getExceptionType() == ScheduleExceptionType.UNAVAILABLE)
                .anyMatch(e -> e.getStartsAt().isBefore(end) && e.getEndsAt().isAfter(start));
        if (insideUnavailable) {
            throw new DoctorNotAvailableException("Doctor is unavailable during this period");
        }

        DayOfWeek day = start.getDayOfWeek();
        LocalTime startTime = start.toLocalTime();
        LocalTime endTime = end.toLocalTime();

        boolean withinRegularHours = availabilityRepository
                .findByDoctorIdAndActiveTrueOrderByDayOfWeekAscStartTimeAsc(doctorId).stream()
                .anyMatch(rule -> rule.getDayOfWeek() == day
                        && !startTime.isBefore(rule.getStartTime())
                        && !endTime.isAfter(rule.getEndTime()));

        boolean withinOverride = scheduleExceptions.stream()
                .filter(e -> e.getExceptionType() == ScheduleExceptionType.AVAILABLE_OVERRIDE)
                .anyMatch(e -> !start.isBefore(e.getStartsAt()) && !end.isAfter(e.getEndsAt()));

        if (!withinRegularHours && !withinOverride) {
            throw new DoctorNotAvailableException("Appointment is outside the doctor's availability");
        }
    }

    private void assertNoOverlap(Long doctorId, LocalDateTime start, LocalDateTime end) {
        boolean overlaps = appointmentRepository.findByDoctorIdAndStatusIn(doctorId, ACTIVE_STATUSES).stream()
                .anyMatch(existing -> start.isBefore(existing.getScheduledEnd())
                        && existing.getScheduledStart().isBefore(end));
        if (overlaps) {
            throw new AppointmentOverlapException("Doctor already has an overlapping appointment");
        }
    }

    // ---------- lifecycle ----------

    public AppointmentResponse confirm(Long appointmentId, Long actorUserId) {
        return changeStatus(appointmentId, AppointmentStatus.CONFIRMED, actorUserId, null);
    }

    public AppointmentResponse cancel(Long appointmentId, Long actorUserId, String reason) {
        if (reason == null || reason.isBlank()) {
            throw new CancellationReasonRequiredException("Cancellation reason is required");
        }
        return changeStatus(appointmentId, AppointmentStatus.CANCELLED, actorUserId, reason);
    }

    public AppointmentResponse complete(Long appointmentId, Long actorUserId) {
        return changeStatus(appointmentId, AppointmentStatus.COMPLETED, actorUserId, null);
    }

    public AppointmentResponse noShow(Long appointmentId, Long actorUserId) {
        return changeStatus(appointmentId, AppointmentStatus.NO_SHOW, actorUserId, null);
    }

    private AppointmentResponse changeStatus(Long appointmentId,
                                             AppointmentStatus newStatus,
                                             Long actorUserId,
                                             String reason) {
        Appointment appointment = findAppointment(appointmentId);
        AppointmentStatus oldStatus = appointment.getStatus();

        if (!isValidTransition(oldStatus, newStatus)) {
            throw new InvalidStatusTransitionException(
                    "Invalid status transition: " + oldStatus + " -> " + newStatus);
        }

        if (newStatus == AppointmentStatus.CANCELLED) {
            appointment.setCancellationReason(reason);
        }
        appointment.setStatus(newStatus);
        Appointment saved = appointmentRepository.save(appointment);

        appointmentStatusHistoryService.record(saved, oldStatus, newStatus, actorUserId, reason);

        return appointmentMapper.toResponse(saved);
    }

    private boolean isValidTransition(AppointmentStatus from, AppointmentStatus to) {
        return switch (from) {
            case SCHEDULED -> to == AppointmentStatus.CONFIRMED || to == AppointmentStatus.CANCELLED;
            case CONFIRMED -> to == AppointmentStatus.COMPLETED
                    || to == AppointmentStatus.CANCELLED
                    || to == AppointmentStatus.NO_SHOW;
            case COMPLETED, CANCELLED, NO_SHOW -> false;
        };
    }

    private Appointment findAppointment(Long id) {
        return appointmentRepository.findById(id)
                .orElseThrow(() -> new AppointmentNotFoundException("Appointment not found"));
    }
}
