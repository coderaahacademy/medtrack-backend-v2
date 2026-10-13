package com.coderaah.medtrack.prescription.controller;

import com.coderaah.medtrack.prescription.dto.AddPrescriptionItemRequest;
import com.coderaah.medtrack.prescription.dto.CancelPrescriptionRequest;
import com.coderaah.medtrack.prescription.dto.CreatePrescriptionRequest;
import com.coderaah.medtrack.prescription.dto.PrescriptionItemResponse;
import com.coderaah.medtrack.prescription.dto.PrescriptionResponse;
import com.coderaah.medtrack.prescription.dto.PrescriptionStatusHistoryResponse;
import com.coderaah.medtrack.prescription.service.PrescriptionService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class PrescriptionController {

    private final PrescriptionService prescriptionService;

    public PrescriptionController(PrescriptionService prescriptionService) {
        this.prescriptionService = prescriptionService;
    }

    @PostMapping("/visits/{visitId}/prescriptions")
    public ResponseEntity<PrescriptionResponse> createPrescription(
            @PathVariable Long visitId,
            @Valid @RequestBody CreatePrescriptionRequest request,
            @RequestParam Long actorUserId) {

        PrescriptionResponse response = prescriptionService.createPrescription(
                visitId, request, actorUserId);

        return ResponseEntity
                .created(URI.create("/api/prescriptions/" + response.getId()))
                .body(response);
    }

    @PostMapping("/prescriptions/{id}/items")
    public ResponseEntity<PrescriptionItemResponse> addItem(
            @PathVariable("id") Long prescriptionId,
            @Valid @RequestBody AddPrescriptionItemRequest request) {

        PrescriptionItemResponse response =
                prescriptionService.addItem(prescriptionId, request);

        return ResponseEntity.status(201).body(response);
    }

    @PutMapping("/prescriptions/{id}/items/{itemId}")
    public ResponseEntity<PrescriptionItemResponse> updateItem(
            @PathVariable("id") Long prescriptionId,
            @PathVariable Long itemId,
            @Valid @RequestBody AddPrescriptionItemRequest request) {

        return ResponseEntity.ok(
                prescriptionService.updateItem(prescriptionId, itemId, request));
    }

    @DeleteMapping("/prescriptions/{id}/items/{itemId}")
    public ResponseEntity<Void> removeItem(
            @PathVariable("id") Long prescriptionId,
            @PathVariable Long itemId) {

        prescriptionService.removeItem(prescriptionId, itemId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/prescriptions/{id}/issue")
    public ResponseEntity<PrescriptionResponse> issue(
            @PathVariable("id") Long prescriptionId,
            @RequestParam Long actorUserId) {

        return ResponseEntity.ok(
                prescriptionService.issue(prescriptionId, actorUserId));
    }

    @PatchMapping("/prescriptions/{id}/cancel")
    public ResponseEntity<PrescriptionResponse> cancel(
            @PathVariable("id") Long prescriptionId,
            @RequestParam Long actorUserId,
            @Valid @RequestBody CancelPrescriptionRequest request) {

        return ResponseEntity.ok(
                prescriptionService.cancel(prescriptionId, request, actorUserId));
    }

    @GetMapping("/prescriptions/{id}")
    public ResponseEntity<PrescriptionResponse> getPrescription(
            @PathVariable("id") Long prescriptionId) {

        return ResponseEntity.ok(
                prescriptionService.getPrescription(prescriptionId));
    }

    @GetMapping("/visits/{visitId}/prescriptions")
    public ResponseEntity<List<PrescriptionResponse>> getPrescriptionsForVisit(
            @PathVariable Long visitId) {

        return ResponseEntity.ok(
                prescriptionService.getPrescriptionsForVisit(visitId));
    }

    @GetMapping("/patients/{patientId}/prescriptions")
    public ResponseEntity<List<PrescriptionResponse>> getPrescriptionsForPatient(
            @PathVariable Long patientId) {

        return ResponseEntity.ok(
                prescriptionService.getPrescriptionsForPatient(patientId));
    }

    @GetMapping("/prescriptions/{id}/history")
    public ResponseEntity<List<PrescriptionStatusHistoryResponse>> getHistory(
            @PathVariable("id") Long prescriptionId) {

        return ResponseEntity.ok(
                prescriptionService.getHistory(prescriptionId));
    }
}
