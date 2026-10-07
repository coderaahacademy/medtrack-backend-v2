package com.coderaah.medtrack.visit.service;

import com.coderaah.medtrack.doctor.domain.DoctorProfile;
import com.coderaah.medtrack.doctor.exception.DoctorNotFoundException;
import com.coderaah.medtrack.doctor.repository.DoctorProfileRepository;
import com.coderaah.medtrack.patient.domain.PatientProfile;
import com.coderaah.medtrack.patient.exception.PatientNotFoundException;
import com.coderaah.medtrack.patient.repository.PatientProfileRepository;
import com.coderaah.medtrack.visit.domain.Visit;
import com.coderaah.medtrack.visit.domain.VisitStatus;
import com.coderaah.medtrack.visit.dto.ClinicalNotesRequest;
import com.coderaah.medtrack.visit.dto.StartVisitRequest;
import com.coderaah.medtrack.visit.dto.VisitResponse;
import com.coderaah.medtrack.visit.exception.InvalidVisitStateException;
import com.coderaah.medtrack.visit.exception.VisitNotFoundException;
import com.coderaah.medtrack.visit.repository.VisitRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class VisitServiceImpl implements VisitService {

    private final VisitRepository visitRepository;
    private final PatientProfileRepository patientProfileRepository;
    private final DoctorProfileRepository doctorProfileRepository;

    public VisitServiceImpl(VisitRepository visitRepository,
                            PatientProfileRepository patientProfileRepository,
                            DoctorProfileRepository doctorProfileRepository) {
        this.visitRepository = visitRepository;
        this.patientProfileRepository = patientProfileRepository;
        this.doctorProfileRepository = doctorProfileRepository;
    }

    @Override
    public VisitResponse startVisit(StartVisitRequest request) {
        PatientProfile patient = patientProfileRepository.findById(request.getPatientId())
                .orElseThrow(() -> new PatientNotFoundException(
                        "Patient not found with id: " + request.getPatientId()));

        DoctorProfile doctor = doctorProfileRepository.findById(request.getDoctorId())
                .orElseThrow(() -> new DoctorNotFoundException(
                        "Doctor not found with id: " + request.getDoctorId()));

        Visit visit = new Visit(patient, doctor, LocalDateTime.now(), VisitStatus.IN_PROGRESS);
        visit.setSymptoms(request.getSymptoms());

        return toResponse(visitRepository.save(visit));
    }

    @Override
    @Transactional(readOnly = true)
    public VisitResponse getVisitById(Long id) {
        return toResponse(getVisitOrThrow(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<VisitResponse> getVisitsForPatient(Long patientId) {
        if (!patientProfileRepository.existsById(patientId)) {
            throw new PatientNotFoundException("Patient not found with id: " + patientId);
        }
        return visitRepository.findByPatientIdOrderByStartedAtDesc(patientId)
                .stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<VisitResponse> getVisitsForDoctor(Long doctorId) {
        if (!doctorProfileRepository.existsById(doctorId)) {
            throw new DoctorNotFoundException("Doctor not found with id: " + doctorId);
        }
        return visitRepository.findByDoctorIdOrderByStartedAtDesc(doctorId)
                .stream().map(this::toResponse).toList();
    }

    @Override
    public VisitResponse updateClinicalNotes(Long id, ClinicalNotesRequest request) {
        Visit visit = getVisitOrThrow(id);

        if (visit.getStatus() != VisitStatus.IN_PROGRESS) {
            throw new InvalidVisitStateException(
                    "Cannot edit clinical information on a visit with status " + visit.getStatus());
        }

        if (request.getSymptoms() != null) {
            visit.setSymptoms(request.getSymptoms());
        }
        if (request.getDiagnosis() != null) {
            visit.setDiagnosis(request.getDiagnosis());
        }
        if (request.getClinicalNotes() != null) {
            visit.setClinicalNotes(request.getClinicalNotes());
        }

        return toResponse(visitRepository.save(visit));
    }

    @Override
    public VisitResponse completeVisit(Long id) {
        Visit visit = getVisitOrThrow(id);

        if (visit.getStatus() != VisitStatus.IN_PROGRESS) {
            throw new InvalidVisitStateException("Only an IN_PROGRESS visit can be completed");
        }

        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(visit.getStartedAt())) {
            throw new InvalidVisitStateException("endedAt cannot be before startedAt");
        }

        visit.setEndedAt(now);
        visit.setStatus(VisitStatus.COMPLETED);

        return toResponse(visitRepository.save(visit));
    }

    @Override
    public VisitResponse cancelVisit(Long id) {
        Visit visit = getVisitOrThrow(id);

        if (visit.getStatus() != VisitStatus.IN_PROGRESS) {
            throw new InvalidVisitStateException("Only an IN_PROGRESS visit can be cancelled");
        }

        visit.setStatus(VisitStatus.CANCELLED);

        return toResponse(visitRepository.save(visit));
    }

    private Visit getVisitOrThrow(Long id) {
        return visitRepository.findById(id)
                .orElseThrow(() -> new VisitNotFoundException(id));
    }

    private VisitResponse toResponse(Visit visit) {
        VisitResponse response = new VisitResponse();
        response.setId(visit.getId());
        response.setPatientId(visit.getPatient().getId());
        response.setDoctorId(visit.getDoctor().getId());
        response.setAppointmentId(visit.getAppointment() != null ? visit.getAppointment().getId() : null);
        response.setStartedAt(visit.getStartedAt());
        response.setEndedAt(visit.getEndedAt());
        response.setSymptoms(visit.getSymptoms());
        response.setDiagnosis(visit.getDiagnosis());
        response.setClinicalNotes(visit.getClinicalNotes());
        response.setStatus(visit.getStatus());
        response.setCreatedAt(visit.getCreatedAt());
        response.setUpdatedAt(visit.getUpdatedAt());
        return response;
    }
}