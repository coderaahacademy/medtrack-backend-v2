package com.coderaah.medtrack.doctor.controller;

import com.coderaah.medtrack.doctor.dto.CreatePatientDoctorRelationshipRequest;
import com.coderaah.medtrack.doctor.dto.PatientDoctorRelationshipResponse;
import com.coderaah.medtrack.doctor.service.PatientDoctorRelationshipService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class PatientDoctorRelationshipController {

    private final PatientDoctorRelationshipService relationshipService;

    public PatientDoctorRelationshipController(PatientDoctorRelationshipService relationshipService) {
        this.relationshipService = relationshipService;
    }

    @PostMapping("/patients/{patientId}/doctors")
    public ResponseEntity<PatientDoctorRelationshipResponse> assign(@PathVariable Long patientId,
                                                                    @Valid @RequestBody CreatePatientDoctorRelationshipRequest request) {
        PatientDoctorRelationshipResponse response =
                PatientDoctorRelationshipResponse.from(relationshipService.assign(patientId, request));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/patients/{patientId}/doctors")
    public List<PatientDoctorRelationshipResponse> findDoctorsForPatient(@PathVariable Long patientId) {
        return relationshipService.findActiveDoctorsForPatient(patientId).stream()
                .map(PatientDoctorRelationshipResponse::from)
                .toList();
    }

    @GetMapping("/doctors/{doctorId}/patients")
    public List<PatientDoctorRelationshipResponse> findPatientsForDoctor(@PathVariable Long doctorId) {
        return relationshipService.findActivePatientsForDoctor(doctorId).stream()
                .map(PatientDoctorRelationshipResponse::from)
                .toList();
    }

    @PatchMapping("/patient-doctor-relationships/{id}/end")
    public PatientDoctorRelationshipResponse end(@PathVariable Long id) {
        return PatientDoctorRelationshipResponse.from(relationshipService.end(id));
    }
}