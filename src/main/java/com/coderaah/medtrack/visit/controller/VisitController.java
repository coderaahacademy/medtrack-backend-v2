package com.coderaah.medtrack.visit.controller;

import com.coderaah.medtrack.visit.dto.StartVisitRequest;
import com.coderaah.medtrack.visit.dto.UpdateClinicalNotesRequest;
import com.coderaah.medtrack.visit.dto.VisitResponse;
import com.coderaah.medtrack.visit.service.VisitService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class VisitController {

    private final VisitService visitService;

    public VisitController(VisitService visitService) {
        this.visitService = visitService;
    }

    @PostMapping("/visits")
    public ResponseEntity<VisitResponse> startVisit(@Valid @RequestBody StartVisitRequest request) {
        VisitResponse created = visitService.startVisit(request);
        return ResponseEntity.created(URI.create("/api/visits/" + created.getId())).body(created);
    }

    @PostMapping("/appointments/{appointmentId}/visit")
    public ResponseEntity<VisitResponse> startVisitFromAppointment(@PathVariable Long appointmentId) {
        VisitResponse created = visitService.startVisitFromAppointment(appointmentId);
        return ResponseEntity.created(URI.create("/api/visits/" + created.getId())).body(created);
    }

    @GetMapping("/visits/{id}")
    public ResponseEntity<VisitResponse> getVisitById(@PathVariable Long id) {
        return ResponseEntity.ok(visitService.getVisitById(id));
    }

    @GetMapping("/patients/{patientId}/visits")
    public ResponseEntity<List<VisitResponse>> getVisitsByPatient(@PathVariable Long patientId) {
        return ResponseEntity.ok(visitService.getVisitsByPatient(patientId));
    }

    @GetMapping("/doctors/{doctorId}/visits")
    public ResponseEntity<List<VisitResponse>> getVisitsByDoctor(@PathVariable Long doctorId) {
        return ResponseEntity.ok(visitService.getVisitsByDoctor(doctorId));
    }

    @GetMapping("/appointments/{appointmentId}/visit")
    public ResponseEntity<VisitResponse> getVisitByAppointment(@PathVariable Long appointmentId) {
        return ResponseEntity.ok(visitService.getVisitByAppointment(appointmentId));
    }

    @PutMapping("/visits/{id}/clinical-notes")
    public ResponseEntity<VisitResponse> updateClinicalNotes(
            @PathVariable Long id, @Valid @RequestBody UpdateClinicalNotesRequest request) {
        return ResponseEntity.ok(visitService.updateClinicalNotes(id, request));
    }

    @PatchMapping("/visits/{id}/complete")
    public ResponseEntity<VisitResponse> completeVisit(@PathVariable Long id) {
        return ResponseEntity.ok(visitService.completeVisit(id));
    }

    @PatchMapping("/visits/{id}/cancel")
    public ResponseEntity<VisitResponse> cancelVisit(@PathVariable Long id) {
        return ResponseEntity.ok(visitService.cancelVisit(id));
    }
}