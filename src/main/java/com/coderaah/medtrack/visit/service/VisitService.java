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
import com.coderaah.medtrack.visit.dto.StartVisitRequest;
import com.coderaah.medtrack.visit.dto.UpdateClinicalNotesRequest;
import com.coderaah.medtrack.visit.dto.VisitResponse;
import com.coderaah.medtrack.visit.exception.InvalidAppointmentForVisitException;
import com.coderaah.medtrack.visit.exception.InvalidVisitStatusException;
import com.coderaah.medtrack.visit.exception.VisitAlreadyExistsException;
import com.coderaah.medtrack.visit.exception.VisitNotFoundException;
import com.coderaah.medtrack.visit.repository.VisitRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class VisitService {

    private final VisitRepository visitRepository;
    private final PatientProfileRepository patientProfileRepository;
    private final DoctorProfileRepository doctorProfileRepository;
    private final AppointmentRepository appointmentRepository;

    public VisitService(VisitRepository visitRepository,
                        PatientProfileRepository patientProfileRepository,
                        DoctorProfileRepository doctorProfileRepository,
                        AppointmentRepository appointmentRepository) {
        this.visitRepository = visitRepository;
        this.patientProfileRepository = patientProfileRepository;
        this.doctorProfileRepository = doctorProfileRepository;
        this.appointmentRepository = appointmentRepository;
    }

    // ---------- Start ----------

    public VisitResponse startVisit(StartVisitRequest request) {
        PatientProfile patient = findPatient(request.getPatientId());
        DoctorProfile doctor = findDoctor(request.getDoctorId());

        Appointment appointment = null;
        if (request.getAppointmentId() != null) {
            appointment = findAppointment(request.getAppointmentId());
            validateAppointmentMatches(appointment, patient, doctor);
            validateAppointmentCanStartVisit(appointment);
        }

        return toResponse(createVisit(patient, doctor, appointment, request.getSymptoms()));
    }

    public VisitResponse startVisitFromAppointment(Long appointmentId) {
        Appointment appointment = findAppointment(appointmentId);
        validateAppointmentCanStartVisit(appointment);

        return toResponse(createVisit(
                appointment.getPatient(), appointment.getDoctor(), appointment, null));
    }

    // ---------- Read ----------

    @Transactional(readOnly = true)
    public VisitResponse getVisitById(Long id) {
        return toResponse(findVisit(id));
    }

    @Transactional(readOnly = true)
    public List<VisitResponse> getVisitsByPatient(Long patientId) {
        if (!patientProfileRepository.existsById(patientId)) {
            throw new PatientNotFoundException("Patient not found");
        }
        return visitRepository.findByPatient_IdOrderByStartedAtDesc(patientId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<VisitResponse> getVisitsByDoctor(Long doctorId) {
        if (!doctorProfileRepository.existsById(doctorId)) {
            throw new DoctorNotFoundException("Doctor not found");
        }
        return visitRepository.findByDoctor_IdOrderByStartedAtDesc(doctorId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public VisitResponse getVisitByAppointment(Long appointmentId) {
        if (!appointmentRepository.existsById(appointmentId)) {
            throw new AppointmentNotFoundException("Appointment not found with id: " + appointmentId);
        }
        Visit visit = visitRepository.findByAppointment_Id(appointmentId)
                .orElseThrow(() -> new VisitNotFoundException("No visit exists for this appointment"));
        return toResponse(visit);
    }

    // ---------- Update / Transitions ----------

    public VisitResponse updateClinicalNotes(Long id, UpdateClinicalNotesRequest request) {
        Visit visit = findVisit(id);
        requireInProgress(visit, "update clinical notes of");

        visit.setSymptoms(request.getSymptoms());
        visit.setDiagnosis(request.getDiagnosis());
        visit.setClinicalNotes(request.getClinicalNotes());

        return toResponse(visitRepository.save(visit));
    }

    public VisitResponse completeVisit(Long id) {
        Visit visit = findVisit(id);
        requireInProgress(visit, "complete");

        LocalDateTime endedAt = LocalDateTime.now();
        if (endedAt.isBefore(visit.getStartedAt())) {
            throw new InvalidVisitStatusException("endedAt must not be before startedAt");
        }

        visit.setEndedAt(endedAt);
        visit.setStatus(VisitStatus.COMPLETED);
        return toResponse(visitRepository.save(visit));
    }

    public VisitResponse cancelVisit(Long id) {
        Visit visit = findVisit(id);
        requireInProgress(visit, "cancel");

        visit.setStatus(VisitStatus.CANCELLED);
        return toResponse(visitRepository.save(visit));
    }

    // ---------- Helper ----------

    private Visit createVisit(PatientProfile patient, DoctorProfile doctor,
                              Appointment appointment, String symptoms) {
        Visit visit = new Visit(patient, doctor, appointment,
                LocalDateTime.now(), VisitStatus.IN_PROGRESS);
        visit.setSymptoms(symptoms);
        try {
            return visitRepository.saveAndFlush(visit);
        } catch (DataIntegrityViolationException ex) {
            if (appointment != null) {
                // zwei Requests gleichzeitig: der Unique-Constraint auf appointment_id greift
                throw new VisitAlreadyExistsException("A visit already exists for this appointment");
            }
            throw ex;
        }
    }

    private void validateAppointmentMatches(Appointment appointment,
                                            PatientProfile patient, DoctorProfile doctor) {
        if (!appointment.getPatient().getId().equals(patient.getId())) {
            throw new InvalidAppointmentForVisitException("Appointment belongs to a different patient");
        }
        if (!appointment.getDoctor().getId().equals(doctor.getId())) {
            throw new InvalidAppointmentForVisitException("Appointment belongs to a different doctor");
        }
    }

    private void validateAppointmentCanStartVisit(Appointment appointment) {
        AppointmentStatus status = appointment.getStatus();
        if (status == AppointmentStatus.CANCELLED) {
            throw new InvalidAppointmentForVisitException("A cancelled appointment cannot start a visit");
        }
        if (status == AppointmentStatus.NO_SHOW) {
            throw new InvalidAppointmentForVisitException("A no-show appointment cannot start a visit");
        }
        if (visitRepository.existsByAppointment_Id(appointment.getId())) {
            throw new VisitAlreadyExistsException("A visit already exists for this appointment");
        }
    }

    private void requireInProgress(Visit visit, String action) {
        if (visit.getStatus() != VisitStatus.IN_PROGRESS) {
            throw new InvalidVisitStatusException(
                    "Cannot " + action + " a visit with status " + visit.getStatus());
        }
    }

    private Visit findVisit(Long id) {
        return visitRepository.findById(id)
                .orElseThrow(() -> new VisitNotFoundException(id));
    }

    private PatientProfile findPatient(Long id) {
        return patientProfileRepository.findById(id)
                .orElseThrow(() -> new PatientNotFoundException("Patient not found"));
    }

    private DoctorProfile findDoctor(Long id) {
        return doctorProfileRepository.findById(id)
                .orElseThrow(() -> new DoctorNotFoundException("Doctor not found"));
    }

    private Appointment findAppointment(Long id) {
        return appointmentRepository.findById(id)
                .orElseThrow(() -> new AppointmentNotFoundException("Appointment not found with id: " + id));
    }

    private VisitResponse toResponse(Visit visit) {
        VisitResponse response = new VisitResponse();
        response.setId(visit.getId());
        response.setPatientId(visit.getPatient().getId());
        response.setDoctorId(visit.getDoctor().getId());
        response.setAppointmentId(visit.getAppointment() != null ? visit.getAppointment().getId() : null);
        response.setStatus(visit.getStatus());
        response.setSymptoms(visit.getSymptoms());
        response.setDiagnosis(visit.getDiagnosis());
        response.setClinicalNotes(visit.getClinicalNotes());
        response.setStartedAt(visit.getStartedAt());
        response.setEndedAt(visit.getEndedAt());
        response.setCreatedAt(visit.getCreatedAt());
        response.setUpdatedAt(visit.getUpdatedAt());
        return response;
    }
}