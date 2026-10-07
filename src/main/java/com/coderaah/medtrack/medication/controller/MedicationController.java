package com.coderaah.medtrack.medication.controller;


import com.coderaah.medtrack.medication.dto.MedicationRequest;
import com.coderaah.medtrack.medication.dto.MedicationResponse;
import com.coderaah.medtrack.medication.service.MedicationService;
import com.coderaah.medtrack.medication.dto.MedicationStatusRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/medications")
public class MedicationController {

    private final MedicationService medicationService;

    public MedicationController(MedicationService medicationService) {
        this.medicationService = medicationService;
    }

    @PostMapping
    public ResponseEntity<MedicationResponse> createMedication(@Valid @RequestBody MedicationRequest newMedication) {
        MedicationResponse createdMedication = medicationService.createMedication(newMedication);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(createdMedication);
    }

    @GetMapping("/{id}")
    public ResponseEntity<MedicationResponse> getMedicationById(@PathVariable Long id) {
        MedicationResponse medication = medicationService.getMedicationById(id);
        return ResponseEntity.ok(medication);
    }
    @GetMapping
    public ResponseEntity<List<MedicationResponse>> listMedications(
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) String search) {

        List<MedicationResponse> medications = medicationService.listMedications(active, search);
        return ResponseEntity.ok(medications);
    }
    @PutMapping("/{id}")
    public ResponseEntity<MedicationResponse> updateMedication(@PathVariable Long id, @Valid @RequestBody MedicationRequest medicationUpdate) {
        MedicationResponse updatedMedication = medicationService.updateMedication(id, medicationUpdate);
        return ResponseEntity.ok(updatedMedication);
    }
    @PatchMapping("/{id}/status")
    public ResponseEntity<MedicationResponse> updateStatus(@PathVariable Long id, @Valid @RequestBody MedicationStatusRequest statusUpdate) {
        MedicationResponse updatedMedication = medicationService.updateStatus(id, statusUpdate.getActive());
        return ResponseEntity.ok(updatedMedication);
    }


}
