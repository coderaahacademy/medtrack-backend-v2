package com.coderaah.medtrack.visit.service;

import com.coderaah.medtrack.appointment.domain.Appointment;
import com.coderaah.medtrack.appointment.domain.AppointmentStatus;
import com.coderaah.medtrack.appointment.exception.AppointmentNotFoundException;
import com.coderaah.medtrack.appointment.repository.AppointmentRepository;
import com.coderaah.medtrack.doctor.domain.DoctorProfile;
import com.coderaah.medtrack.doctor.exception.DoctorNotFoundException;
import com.coderaah.medtrack.doctor.repository.DoctorProfileRepository;
import com.coderaah.medtrack.patient.domain.PatientProfile;
import com.coderaah.medtrack.patient.exception.PatientNotFoundException;
import com.coderaah.medtrack.patient.repository.PatientProfileRepository;
import com.coderaah.medtrack.visit.domain.Visit;
import com.coderaah.medtrack.visit.domain.VisitStatus;
import com.coderaah.medtrack.visit.dto.CompleteVisitRequest;
import com.coderaah.medtrack.visit.dto.StartVisitFromAppointmentRequest;
import com.coderaah.medtrack.visit.dto.StartVisitRequest;
import com.coderaah.medtrack.visit.dto.UpdateVisitClinicalInfoRequest;
import com.coderaah.medtrack.visit.dto.VisitResponse;
import com.coderaah.medtrack.visit.exception.InvalidVisitOperationException;
import com.coderaah.medtrack.visit.exception.VisitNotFoundException;
import com.coderaah.medtrack.visit.repository.VisitRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class VisitService {

    private final VisitRepository visitRepository;
    private final PatientProfileRepository patientProfileRepository;
    private final DoctorProfileRepository doctorProfileRepository;
    private final AppointmentRepository appointmentRepository;

    public VisitService(
            VisitRepository visitRepository,
            PatientProfileRepository patientProfileRepository,
            DoctorProfileRepository doctorProfileRepository,
            AppointmentRepository appointmentRepository
    ) {
        this.visitRepository = visitRepository;
        this.patientProfileRepository = patientProfileRepository;
        this.doctorProfileRepository = doctorProfileRepository;
        this.appointmentRepository = appointmentRepository;
    }

    public VisitResponse startVisit(StartVisitRequest request) {
        PatientProfile patient = patientProfileRepository.findById(request.getPatientId())
                .orElseThrow(() -> new PatientNotFoundException("Patient not found"));

        DoctorProfile doctor = doctorProfileRepository.findById(request.getDoctorId())
                .orElseThrow(() -> new DoctorNotFoundException("Doctor not found"));

        Visit visit = new Visit(
                patient,
                doctor,
                request.getStartedAt() != null ? request.getStartedAt() : LocalDateTime.now(),
                VisitStatus.IN_PROGRESS
        );
        applyClinicalInfo(visit, request.getSymptoms(), request.getDiagnosis(), request.getClinicalNotes(), null, null);

        Visit savedVisit = visitRepository.save(visit);
        return toResponse(savedVisit);
    }

    public VisitResponse startVisitFromAppointment(Long appointmentId, StartVisitFromAppointmentRequest request) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new AppointmentNotFoundException("Appointment not found"));

        if (visitRepository.existsByAppointmentId(appointmentId)) {
            throw new InvalidVisitOperationException("Appointment already has a visit");
        }

        if (appointment.getStatus() == AppointmentStatus.CANCELLED || appointment.getStatus() == AppointmentStatus.NO_SHOW) {
            throw new InvalidVisitOperationException("Cannot start visit from cancelled or no-show appointment");
        }

        if (request != null && request.getPatientId() != null && !request.getPatientId().equals(appointment.getPatient().getId())) {
            throw new InvalidVisitOperationException("Appointment patient does not match visit patient");
        }

        if (request != null && request.getDoctorId() != null && !request.getDoctorId().equals(appointment.getDoctor().getId())) {
            throw new InvalidVisitOperationException("Appointment doctor does not match visit doctor");
        }

        Visit visit = new Visit(
                appointment.getPatient(),
                appointment.getDoctor(),
                appointment,
                request != null && request.getStartedAt() != null ? request.getStartedAt() : LocalDateTime.now(),
                VisitStatus.IN_PROGRESS
        );

        if (request != null) {
            applyClinicalInfo(visit, request.getSymptoms(), request.getDiagnosis(), request.getClinicalNotes(), null, null);
        }

        Visit savedVisit = visitRepository.save(visit);
        return toResponse(savedVisit);
    }

    @Transactional(readOnly = true)
    public VisitResponse getVisitById(Long id) {
        Visit visit = visitRepository.findById(id)
                .orElseThrow(() -> new VisitNotFoundException("Visit not found"));
        return toResponse(visit);
    }

    @Transactional(readOnly = true)
    public List<VisitResponse> getVisitsForPatient(Long patientId) {
        if (!patientProfileRepository.existsById(patientId)) {
            throw new PatientNotFoundException("Patient not found");
        }
        return visitRepository.findByPatientIdOrderByStartedAtDesc(patientId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<VisitResponse> getVisitsForDoctor(Long doctorId) {
        if (!doctorProfileRepository.existsById(doctorId)) {
            throw new DoctorNotFoundException("Doctor not found");
        }
        return visitRepository.findByDoctorIdOrderByStartedAtDesc(doctorId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public VisitResponse getVisitByAppointment(Long appointmentId) {
        if (!appointmentRepository.existsById(appointmentId)) {
            throw new AppointmentNotFoundException("Appointment not found");
        }
        Visit visit = visitRepository.findByAppointmentId(appointmentId)
                .orElseThrow(() -> new VisitNotFoundException("Visit not found for appointment"));
        return toResponse(visit);
    }

    public VisitResponse updateClinicalInformation(Long visitId, UpdateVisitClinicalInfoRequest request) {
        Visit visit = visitRepository.findById(visitId)
                .orElseThrow(() -> new VisitNotFoundException("Visit not found"));

        ensureVisitInProgress(visit);

        applyClinicalInfo(visit, request.getSymptoms(), request.getDiagnosis(), request.getClinicalNotes(), request.getStartedAt(), request.getEndedAt());

        Visit updatedVisit = visitRepository.save(visit);
        return toResponse(updatedVisit);
    }

    public VisitResponse completeVisit(Long visitId, CompleteVisitRequest request) {
        Visit visit = visitRepository.findById(visitId)
                .orElseThrow(() -> new VisitNotFoundException("Visit not found"));

        ensureVisitInProgress(visit);

        LocalDateTime endedAt = request != null && request.getEndedAt() != null ? request.getEndedAt() : LocalDateTime.now();
        if (endedAt.isBefore(visit.getStartedAt())) {
            throw new InvalidVisitOperationException("Visit end time cannot be before start time");
        }

        visit.setEndedAt(endedAt);
        visit.setStatus(VisitStatus.COMPLETED);
        Visit updatedVisit = visitRepository.save(visit);
        return toResponse(updatedVisit);
    }

    public VisitResponse cancelVisit(Long visitId) {
        Visit visit = visitRepository.findById(visitId)
                .orElseThrow(() -> new VisitNotFoundException("Visit not found"));

        ensureVisitInProgress(visit);
        visit.setStatus(VisitStatus.CANCELLED);
        Visit updatedVisit = visitRepository.save(visit);
        return toResponse(updatedVisit);
    }

    private void ensureVisitInProgress(Visit visit) {
        if (visit.getStatus() != VisitStatus.IN_PROGRESS) {
            throw new InvalidVisitOperationException("Only an in-progress visit can be modified");
        }
    }

    private void applyClinicalInfo(
            Visit visit,
            String symptoms,
            String diagnosis,
            String clinicalNotes,
            LocalDateTime startedAt,
            LocalDateTime endedAt
    ) {
        if (startedAt != null) {
            visit.setStartedAt(startedAt);
        }
        if (symptoms != null) {
            visit.setSymptoms(symptoms);
        }
        if (diagnosis != null) {
            visit.setDiagnosis(diagnosis);
        }
        if (clinicalNotes != null) {
            visit.setClinicalNotes(clinicalNotes);
        }
        if (endedAt != null) {
            visit.setEndedAt(endedAt);
        }
        if (visit.getEndedAt() != null && visit.getStartedAt() != null && visit.getEndedAt().isBefore(visit.getStartedAt())) {
            throw new InvalidVisitOperationException("Visit end time cannot be before start time");
        }
    }

    private VisitResponse toResponse(Visit visit) {
        VisitResponse response = new VisitResponse();
        response.setId(visit.getId());
        response.setPatientId(visit.getPatient().getId());
        response.setDoctorId(visit.getDoctor().getId());
        response.setAppointmentId(visit.getAppointment() != null ? visit.getAppointment().getId() : null);
        response.setSymptoms(visit.getSymptoms());
        response.setDiagnosis(visit.getDiagnosis());
        response.setClinicalNotes(visit.getClinicalNotes());
        response.setStartedAt(visit.getStartedAt());
        response.setEndedAt(visit.getEndedAt());
        response.setStatus(visit.getStatus());
        response.setCreatedAt(visit.getCreatedAt());
        response.setUpdatedAt(visit.getUpdatedAt());
        return response;
    }
}
