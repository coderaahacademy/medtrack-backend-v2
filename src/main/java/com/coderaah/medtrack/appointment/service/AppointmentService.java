package com.coderaah.medtrack.appointment.service;
import com.coderaah.medtrack.appointment.domain.Appointment;
import com.coderaah.medtrack.appointment.domain.AppointmentStatus;
import com.coderaah.medtrack.appointment.dto.requestDto.AppointmentRequestDto;
import com.coderaah.medtrack.appointment.dto.responeDto.AppointmentResponseDto;
import com.coderaah.medtrack.appointment.mapper.AppointmentMapper;
import com.coderaah.medtrack.appointment.repository.AppointmentRepository;
import com.coderaah.medtrack.doctor.domain.DoctorProfile;
import com.coderaah.medtrack.doctor.domain.ScheduleExceptionType;
import com.coderaah.medtrack.doctor.repository.DoctorAvailabilityRuleRepository;
import com.coderaah.medtrack.doctor.repository.DoctorProfileRepository;
import com.coderaah.medtrack.doctor.repository.DoctorScheduleExceptionRepository;
import com.coderaah.medtrack.patient.domain.PatientProfile;
import com.coderaah.medtrack.patient.repository.PatientProfileRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
public class AppointmentService {

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

    // read QUERY


    public AppointmentResponseDto getById(Long id) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Appointment not found: " + id));
        return appointmentMapper.convertAppointmentToAppointmentResponseDto(appointment);
    }

    public List<AppointmentResponseDto> getByPatient(Long patientId) {
        return appointmentRepository.findByPatientId(patientId).stream()
                .map(appointmentMapper::convertAppointmentToAppointmentResponseDto)
                .toList();
    }

    public List<AppointmentResponseDto> getByDoctor(Long doctorId) {
        return appointmentRepository.findByDoctorId(doctorId).stream()
                .map(appointmentMapper::convertAppointmentToAppointmentResponseDto)
                .toList();
    }

    public List<AppointmentResponseDto> getByDoctorAndDate(Long doctorId, LocalDate date) {
        LocalDateTime dayStart = date.atStartOfDay();
        LocalDateTime dayEnd = date.atTime(LocalTime.MAX);
        return appointmentRepository
                .findByDoctorIdAndScheduledStartBetween(doctorId, dayStart, dayEnd).stream()
                .map(appointmentMapper::convertAppointmentToAppointmentResponseDto)
                .toList();
    }

    //CREATE AND VALIDATION

    @Transactional
    public AppointmentResponseDto createAppointment(AppointmentRequestDto dto) {

        LocalDateTime start = dto.getScheduledStart();
        LocalDateTime end = dto.getScheduledEnd();

        //start before end
        if (!start.isBefore(end)) {
            throw new IllegalArgumentException("scheduledStart must be before scheduledEnd");
        }

        // exsit doctor and patient
        PatientProfile patient = patientProfileRepository.findById(dto.getPatientId())
                .orElseThrow(() -> new EntityNotFoundException("Patient not found: " + dto.getPatientId()));
        DoctorProfile doctor = doctorProfileRepository.findById(dto.getDoctorId())
                .orElseThrow(() -> new EntityNotFoundException("Doctor not found: " + dto.getDoctorId()));

        // active doctor
        if (!doctor.isActive()) {
            throw new IllegalStateException("Doctor is not active");
        }

        Long doctorId = doctor.getId();

        // unavailable
        boolean insideUnavailable = scheduleExceptionRepository
                .findByDoctorIdOrderByStartsAtAsc(doctorId).stream()
                .filter(e -> e.getExceptionType() == ScheduleExceptionType.UNAVAILABLE)
                .anyMatch(e -> e.getStartsAt().isBefore(end) && e.getEndsAt().isAfter(start));
        if (insideUnavailable) {
            throw new IllegalStateException("Doctor is unavailable during this period");
        }

        //  AVAILABLE_OVERRIDE
        DayOfWeek day = start.getDayOfWeek();
        LocalTime startTime = start.toLocalTime();
        LocalTime endTime = end.toLocalTime();

        boolean withinRegularHours = availabilityRepository
                .findByDoctorIdAndActiveTrueOrderByDayOfWeekAscStartTimeAsc(doctorId).stream()
                .anyMatch(rule -> rule.getDayOfWeek() == day
                        && !startTime.isBefore(rule.getStartTime())
                        && !endTime.isAfter(rule.getEndTime()));

        boolean withinOverride = scheduleExceptionRepository
                .findByDoctorIdOrderByStartsAtAsc(doctorId).stream()
                .filter(e -> e.getExceptionType() == ScheduleExceptionType.AVAILABLE_OVERRIDE)
                .anyMatch(e -> !start.isBefore(e.getStartsAt()) && !end.isAfter(e.getEndsAt()));

        if (!withinRegularHours && !withinOverride) {
            throw new IllegalStateException("Appointment is outside the doctor's availability");
        }

        // interference
        List<AppointmentStatus> activeStatuses =
                List.of(AppointmentStatus.SCHEDULED, AppointmentStatus.CONFIRMED);
        List<Appointment> doctorAppointments =
                appointmentRepository.findByDoctorIdAndStatusIn(doctorId, activeStatuses);
        for (Appointment existing : doctorAppointments) {
            boolean overlaps = start.isBefore(existing.getScheduledEnd())
                    && existing.getScheduledStart().isBefore(end);
            if (overlaps) {
                throw new IllegalStateException("Doctor already has an overlapping appointment");
            }
        }

        // create and save
        Appointment appointment =
                appointmentMapper.convertAppointmentRequestDtoToAppointment(dto, patient, doctor);
        appointment.setStatus(AppointmentStatus.SCHEDULED);
        Appointment saved = appointmentRepository.save(appointment);

        return appointmentMapper.convertAppointmentToAppointmentResponseDto(saved);
    }

    // LIFECYCLE

    @Transactional
    public AppointmentResponseDto confirm(Long appointmentId, Long actorUserId) {
        return changeStatus(appointmentId, AppointmentStatus.CONFIRMED, actorUserId, null);
    }

    @Transactional
    public AppointmentResponseDto cancel(Long appointmentId, Long actorUserId, String reason) {
        return changeStatus(appointmentId, AppointmentStatus.CANCELLED, actorUserId, reason);
    }

    @Transactional
    public AppointmentResponseDto complete(Long appointmentId, Long actorUserId) {
        return changeStatus(appointmentId, AppointmentStatus.COMPLETED, actorUserId, null);
    }

    @Transactional
    public AppointmentResponseDto noShow(Long appointmentId, Long actorUserId) {
        return changeStatus(appointmentId, AppointmentStatus.NO_SHOW, actorUserId, null);
    }


    private AppointmentResponseDto changeStatus(Long appointmentId,
                                                AppointmentStatus newStatus,
                                                Long actorUserId,
                                                String reason) {

        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new EntityNotFoundException("Appointment not found: " + appointmentId));

        AppointmentStatus oldStatus = appointment.getStatus();

        // transition
        if (!isValidTransition(oldStatus, newStatus)) {
            throw new IllegalStateException(
                    "Invalid status transition: " + oldStatus + " -> " + newStatus);
        }

        // cancel and reason
        if (newStatus == AppointmentStatus.CANCELLED) {
            if (reason == null || reason.isBlank()) {
                throw new IllegalArgumentException("Cancellation reason is required");
            }
            appointment.setCancellationReason(reason);
        }

        appointment.setStatus(newStatus);
        Appointment saved = appointmentRepository.save(appointment);


        appointmentStatusHistoryService.record(saved, oldStatus, newStatus, actorUserId, reason);

        return appointmentMapper.convertAppointmentToAppointmentResponseDto(saved);
    }

    // transaction
    private boolean isValidTransition(AppointmentStatus from, AppointmentStatus to) {
        if (from == AppointmentStatus.SCHEDULED) {
            return to == AppointmentStatus.CONFIRMED
                    || to == AppointmentStatus.CANCELLED;
        }
        if (from == AppointmentStatus.CONFIRMED) {
            return to == AppointmentStatus.COMPLETED
                    || to == AppointmentStatus.CANCELLED
                    || to == AppointmentStatus.NO_SHOW;
        }
        // COMPLETED / CANCELLED / NO_SHOW
        return false;
    }
}
