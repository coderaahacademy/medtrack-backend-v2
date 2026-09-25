package com.coderaah.medtrack.patient.controller;


import com.coderaah.medtrack.patient.domain.PatientConditionStatus;
import com.coderaah.medtrack.patient.dto.ConditionRequest;
import com.coderaah.medtrack.patient.dto.ConditionResponse;
import com.coderaah.medtrack.patient.dto.ConditionStatusUpdateRequest;
import com.coderaah.medtrack.patient.service.PatientConditionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@RestController
@RequestMapping("api/patients/{patientId}/conditions")

public class PatientConditionController {
    private final PatientConditionService patientConditionService;

    public PatientConditionController(PatientConditionService patientConditionService){
        this.patientConditionService=patientConditionService;

    }
    @PostMapping
    public ResponseEntity<ConditionResponse> createCondition(@PathVariable Long patientId,
                                                             @Valid @RequestBody ConditionRequest request) {
        ConditionResponse created = patientConditionService.createCondition(patientId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public ResponseEntity<List<ConditionResponse>> getConditions(@PathVariable Long patientId,
                                                                 @RequestParam(required = false) String status) {
        PatientConditionStatus statusFilter = parseStatus(status);
        List<ConditionResponse> conditions = patientConditionService.getConditions(patientId, statusFilter);
        return ResponseEntity.ok(conditions);
    }

    @PutMapping("/{conditionId}")
    public ResponseEntity<ConditionResponse> updateCondition(@PathVariable Long patientId,@PathVariable Long conditionId,@Valid @RequestBody ConditionRequest request)
    {
        ConditionResponse updated = patientConditionService.updateCondition(patientId, conditionId, request);
        return ResponseEntity.ok(updated);
    }

    @PatchMapping("/{conditionId}/status")
    public ResponseEntity<ConditionResponse> changeConditionStatus(@PathVariable Long patientId,@PathVariable Long conditionId,@Valid @RequestBody ConditionStatusUpdateRequest request)
    {
        ConditionResponse updated = patientConditionService.changeConditionStatus(patientId, conditionId, request.getStatus());
        return ResponseEntity.ok(updated);
    }

    private PatientConditionStatus parseStatus(String status) {
        if (status == null) {
            return null;
        }
        try {
            return PatientConditionStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("Invalid condition status: " + status);
        }
    }
}
