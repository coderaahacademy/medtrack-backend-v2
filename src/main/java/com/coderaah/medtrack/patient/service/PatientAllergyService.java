package com.coderaah.medtrack.patient.service;

import com.coderaah.medtrack.patient.domain.AllergyStatus;
import com.coderaah.medtrack.patient.domain.PatientAllergy;
import com.coderaah.medtrack.patient.domain.PatientProfile;
import com.coderaah.medtrack.patient.dto.AllergyRequest;
import com.coderaah.medtrack.patient.dto.AllergyResponse;
import com.coderaah.medtrack.patient.exception.AllergyNotFoundException;
import com.coderaah.medtrack.patient.exception.PatientNotFoundException;
import com.coderaah.medtrack.patient.repository.PatientAllergyRepository;
import com.coderaah.medtrack.patient.repository.PatientProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class PatientAllergyService {

    private final PatientAllergyRepository patientAllergyRepository;
    private final PatientProfileRepository patientProfileRepository;

    public PatientAllergyService(PatientAllergyRepository patientAllergyRepository,PatientProfileRepository patientProfileRepository) {
        this.patientAllergyRepository = patientAllergyRepository;
        this.patientProfileRepository = patientProfileRepository;
    }

    public AllergyResponse createAllergy(Long patientId, AllergyRequest request) {

        PatientProfile patient = getPatientOrThrow(patientId);

        AllergyStatus initialStatus = request.getStatus() != null ? request.getStatus() : AllergyStatus.ACTIVE;

        PatientAllergy allergy = new PatientAllergy(
                patient,
                request.getAllergen(),
                request.getSeverity(),
                initialStatus
        );
        allergy.setReaction(request.getReaction());
        allergy.setRecordedAt(request.getRecordedAt() != null ? request.getRecordedAt() : LocalDateTime.now());

        PatientAllergy saved = patientAllergyRepository.save(allergy);

        return toResponse(saved);
    }

    public List<AllergyResponse> getAllergies(Long patientId, AllergyStatus statusFilter) {

        getPatientOrThrow(patientId);

        List<PatientAllergy> allergies = statusFilter != null
                ? patientAllergyRepository.findByPatientIdAndStatus(patientId, statusFilter)
                : patientAllergyRepository.findByPatientId(patientId);

        return allergies.stream()
                .map(this::toResponse)
                .toList();
    }

    public AllergyResponse updateAllergy(Long patientId, Long allergyId, AllergyRequest request) {

        PatientAllergy allergy = getAllergyOrThrow(patientId, allergyId);

        allergy.setAllergen(request.getAllergen());
        allergy.setReaction(request.getReaction());
        allergy.setSeverity(request.getSeverity());

        if (request.getStatus() != null) {
            allergy.setStatus(request.getStatus());
        }
        if (request.getRecordedAt() != null) {
            allergy.setRecordedAt(request.getRecordedAt());
        }

        PatientAllergy updated = patientAllergyRepository.save(allergy);

        return toResponse(updated);
    }

    public AllergyResponse changeAllergyStatus(Long patientId, Long allergyId, AllergyStatus status) {

        PatientAllergy allergy = getAllergyOrThrow(patientId, allergyId);

        allergy.setStatus(status);

        PatientAllergy updated = patientAllergyRepository.save(allergy);

        return toResponse(updated);
    }

    private PatientProfile getPatientOrThrow(Long patientId) {
        return patientProfileRepository.findById(patientId)
                .orElseThrow(() -> new PatientNotFoundException("Patient not found"));
    }

    private PatientAllergy getAllergyOrThrow(Long patientId, Long allergyId) {
        getPatientOrThrow(patientId);
        return patientAllergyRepository.findByIdAndPatientId(allergyId, patientId)
                .orElseThrow(() -> new AllergyNotFoundException("Allergy not found"));
    }

    private AllergyResponse toResponse(PatientAllergy allergy) {
        AllergyResponse response = new AllergyResponse();
        response.setId(allergy.getId());
        response.setPatientId(allergy.getPatient().getId());
        response.setAllergen(allergy.getAllergen());
        response.setReaction(allergy.getReaction());
        response.setSeverity(allergy.getSeverity());
        response.setStatus(allergy.getStatus());
        response.setRecordedAt(allergy.getRecordedAt());
        response.setCreatedAt(allergy.getCreatedAt());
        response.setUpdatedAt(allergy.getUpdatedAt());
        return response;
    }
}