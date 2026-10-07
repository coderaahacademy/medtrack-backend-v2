package com.coderaah.medtrack.visit.controller;

import com.coderaah.medtrack.visit.dto.ClinicalNotesRequest;
import com.coderaah.medtrack.visit.dto.StartVisitRequest;
import com.coderaah.medtrack.visit.dto.VisitResponse;
import com.coderaah.medtrack.visit.service.VisitService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class VisitController {

    private final VisitService visitService;

    public VisitController(VisitService visitService) {
        this.visitService = visitService;
    }

    @PostMapping("/api/visits")
    public ResponseEntity<VisitResponse> startVisit(@Valid @RequestBody StartVisitRequest request) {
        VisitResponse response = visitService.startVisit(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/api/visits/{id}")
    public ResponseEntity<VisitResponse> getVisit(@PathVariable Long id) {
        return ResponseEntity.ok(visitService.getVisitById(id));
    }

    @GetMapping("/api/patients/{patientId}/visits")
    public ResponseEntity<List<VisitResponse>> getVisitsForPatient(@PathVariable Long patientId) {
        return ResponseEntity.ok(visitService.getVisitsForPatient(patientId));
    }

    @GetMapping("/api/doctors/{doctorId}/visits")
    public ResponseEntity<List<VisitResponse>> getVisitsForDoctor(@PathVariable Long doctorId) {
        return ResponseEntity.ok(visitService.getVisitsForDoctor(doctorId));
    }

    @PutMapping("/api/visits/{id}/clinical-notes")
    public ResponseEntity<VisitResponse> updateClinicalNotes(@PathVariable Long id,
                                                             @RequestBody ClinicalNotesRequest request) {
        return ResponseEntity.ok(visitService.updateClinicalNotes(id, request));
    }

    @PatchMapping("/api/visits/{id}/complete")
    public ResponseEntity<VisitResponse> completeVisit(@PathVariable Long id) {
        return ResponseEntity.ok(visitService.completeVisit(id));
    }

    @PatchMapping("/api/visits/{id}/cancel")
    public ResponseEntity<VisitResponse> cancelVisit(@PathVariable Long id) {
        return ResponseEntity.ok(visitService.cancelVisit(id));
    }
}