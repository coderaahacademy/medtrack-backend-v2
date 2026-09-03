package com.coderaah.medtrack.visit.controller;

import com.coderaah.medtrack.visit.dto.CompleteVisitRequest;
import com.coderaah.medtrack.visit.dto.StartVisitFromAppointmentRequest;
import com.coderaah.medtrack.visit.dto.StartVisitRequest;
import com.coderaah.medtrack.visit.dto.UpdateVisitClinicalInfoRequest;
import com.coderaah.medtrack.visit.dto.VisitResponse;
import com.coderaah.medtrack.visit.service.VisitService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class VisitController {

    private final VisitService visitService;

    public VisitController(VisitService visitService) {
        this.visitService = visitService;
    }

    @PostMapping("/visits")
    public ResponseEntity<VisitResponse> startVisit(@Valid @RequestBody StartVisitRequest request) {
        VisitResponse createdVisit = visitService.startVisit(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdVisit);
    }

    @PostMapping("/appointments/{appointmentId}/visit")
    public ResponseEntity<VisitResponse> startVisitFromAppointment(
            @PathVariable Long appointmentId,
            @Valid @RequestBody(required = false) StartVisitFromAppointmentRequest request
    ) {
        VisitResponse createdVisit = visitService.startVisitFromAppointment(appointmentId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdVisit);
    }

    @GetMapping("/visits/{id}")
    public ResponseEntity<VisitResponse> getVisitById(@PathVariable Long id) {
        return ResponseEntity.ok(visitService.getVisitById(id));
    }

    @GetMapping("/patients/{patientId}/visits")
    public ResponseEntity<List<VisitResponse>> getVisitsForPatient(@PathVariable Long patientId) {
        return ResponseEntity.ok(visitService.getVisitsForPatient(patientId));
    }

    @GetMapping("/doctors/{doctorId}/visits")
    public ResponseEntity<List<VisitResponse>> getVisitsForDoctor(@PathVariable Long doctorId) {
        return ResponseEntity.ok(visitService.getVisitsForDoctor(doctorId));
    }

    @GetMapping("/appointments/{appointmentId}/visit")
    public ResponseEntity<VisitResponse> getVisitByAppointment(@PathVariable Long appointmentId) {
        return ResponseEntity.ok(visitService.getVisitByAppointment(appointmentId));
    }

    @PutMapping("/visits/{id}/clinical-notes")
    public ResponseEntity<VisitResponse> updateClinicalInformation(
            @PathVariable Long id,
            @Valid @RequestBody UpdateVisitClinicalInfoRequest request
    ) {
        return ResponseEntity.ok(visitService.updateClinicalInformation(id, request));
    }

    @PatchMapping("/visits/{id}/complete")
    public ResponseEntity<VisitResponse> completeVisit(
            @PathVariable Long id,
            @RequestBody(required = false) CompleteVisitRequest request
    ) {
        return ResponseEntity.ok(visitService.completeVisit(id, request));
    }

    @PatchMapping("/visits/{id}/cancel")
    public ResponseEntity<VisitResponse> cancelVisit(@PathVariable Long id) {
        return ResponseEntity.ok(visitService.cancelVisit(id));
    }
}
