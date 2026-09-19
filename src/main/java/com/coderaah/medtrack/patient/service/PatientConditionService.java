package com.coderaah.medtrack.patient.service;

import com.coderaah.medtrack.patient.domain.PatientCondition;
import com.coderaah.medtrack.patient.domain.PatientConditionStatus;
import com.coderaah.medtrack.patient.domain.PatientProfile;
import com.coderaah.medtrack.patient.dto.ConditionRequest;
import com.coderaah.medtrack.patient.dto.ConditionResponse;
import com.coderaah.medtrack.patient.exception.ConditionNotFoundException;
import com.coderaah.medtrack.patient.exception.PatientNotFoundException;
import com.coderaah.medtrack.patient.repository.PatientConditionRepository;
import com.coderaah.medtrack.patient.repository.PatientProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class PatientConditionService {

    private final PatientConditionRepository patientConditionRepository;
    private final PatientProfileRepository patientProfileRepository;

    public PatientConditionService(PatientConditionRepository patientConditionRepository,
                                   PatientProfileRepository patientProfileRepository) {
        this.patientConditionRepository = patientConditionRepository;
        this.patientProfileRepository = patientProfileRepository;
    }

    public ConditionResponse createCondition(Long patientId, ConditionRequest request) {

        PatientProfile patient = getPatientOrThrow(patientId);

        PatientConditionStatus initialStatus = request.getStatus() != null ? request.getStatus() : PatientConditionStatus.ACTIVE;

        PatientCondition condition = new PatientCondition(
                patient,
                request.getConditionName(),
                initialStatus
        );
        condition.setConditionCode(request.getConditionCode());
        condition.setDiagnosedAt(request.getDiagnosedAt());
        condition.setNotes(request.getNotes());

        PatientCondition saved = patientConditionRepository.save(condition);

        return toResponse(saved);
    }

    public List<ConditionResponse> getConditions(Long patientId, PatientConditionStatus statusFilter) {

        getPatientOrThrow(patientId);

        List<PatientCondition> conditions = statusFilter != null
                ? patientConditionRepository.findByPatientIdAndStatus(patientId, statusFilter)
                : patientConditionRepository.findByPatientId(patientId);

        return conditions.stream()
                .map(this::toResponse)
                .toList();
    }

    public ConditionResponse updateCondition(Long patientId, Long conditionId, ConditionRequest request) {

        PatientCondition condition = getConditionOrThrow(patientId, conditionId);

        condition.setConditionName(request.getConditionName());
        condition.setConditionCode(request.getConditionCode());
        condition.setDiagnosedAt(request.getDiagnosedAt());
        condition.setNotes(request.getNotes());

        if (request.getStatus() != null) {
            condition.setStatus(request.getStatus());
        }

        PatientCondition updated = patientConditionRepository.save(condition);

        return toResponse(updated);
    }

    public ConditionResponse changeConditionStatus(Long patientId, Long conditionId, PatientConditionStatus status) {

        PatientCondition condition = getConditionOrThrow(patientId, conditionId);

        condition.setStatus(status);

        PatientCondition updated = patientConditionRepository.save(condition);

        return toResponse(updated);
    }

    private PatientProfile getPatientOrThrow(Long patientId) {
        return patientProfileRepository.findById(patientId)
                .orElseThrow(() -> new PatientNotFoundException("Patient Not Found"));
    }

    private PatientCondition getConditionOrThrow(Long patientId, Long conditionId) {
        getPatientOrThrow(patientId);
        return patientConditionRepository.findByIdAndPatientId(conditionId, patientId)
                .orElseThrow(() -> new ConditionNotFoundException("Condition not found"));
    }

    private ConditionResponse toResponse(PatientCondition condition) {
        ConditionResponse response = new ConditionResponse();
        response.setId(condition.getId());
        response.setPatientId(condition.getPatient().getId());
        response.setConditionName(condition.getConditionName());
        response.setConditionCode(condition.getConditionCode());
        response.setDiagnosedAt(condition.getDiagnosedAt());
        response.setStatus(condition.getStatus());
        response.setNotes(condition.getNotes());
        response.setCreatedAt(condition.getCreatedAt());
        response.setUpdatedAt(condition.getUpdatedAt());
        return response;
    }
}