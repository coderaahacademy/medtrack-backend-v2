package com.coderaah.medtrack.patient.controller;

import com.coderaah.medtrack.patient.domain.AllergyStatus;
import com.coderaah.medtrack.patient.dto.AllergyRequest;
import com.coderaah.medtrack.patient.dto.AllergyResponse;
import com.coderaah.medtrack.patient.dto.AllergyStatusUpdateRequest;
import com.coderaah.medtrack.patient.service.PatientAllergyService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/patients/{patientId}/allergies")
public class PatientAllergyController {

    private final PatientAllergyService patientAllergyService;

    public PatientAllergyController(PatientAllergyService patientAllergyService) {
        this.patientAllergyService = patientAllergyService;
    }

    @PostMapping
    public ResponseEntity<AllergyResponse> createAllergy(@PathVariable Long patientId,
                                                         @Valid @RequestBody AllergyRequest request) {
        AllergyResponse created = patientAllergyService.createAllergy(patientId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public ResponseEntity<List<AllergyResponse>> getAllergies(@PathVariable Long patientId,
                                                              @RequestParam(required = false) AllergyStatus status) {
        List<AllergyResponse> allergies = patientAllergyService.getAllergies(patientId, status);
        return ResponseEntity.ok(allergies);
    }

    @PutMapping("/{allergyId}")
    public ResponseEntity<AllergyResponse> updateAllergy(@PathVariable Long patientId,
                                                         @PathVariable Long allergyId,
                                                         @Valid @RequestBody AllergyRequest request) {
        AllergyResponse updated = patientAllergyService.updateAllergy(patientId, allergyId, request);
        return ResponseEntity.ok(updated);
    }

    @PatchMapping("/{allergyId}/status")
    public ResponseEntity<AllergyResponse> changeAllergyStatus(@PathVariable Long patientId,
                                                               @PathVariable Long allergyId,
                                                               @Valid @RequestBody AllergyStatusUpdateRequest request) {
        AllergyResponse updated = patientAllergyService.changeAllergyStatus(patientId, allergyId, request.getStatus());
        return ResponseEntity.ok(updated);
    }
}