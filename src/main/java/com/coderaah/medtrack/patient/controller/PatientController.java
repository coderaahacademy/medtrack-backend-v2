package com.coderaah.medtrack.patient.controller;


import com.coderaah.medtrack.patient.dto.PatientRequest;
import com.coderaah.medtrack.patient.dto.PatientResponse;
import com.coderaah.medtrack.patient.service.PatientService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/patients")
public class PatientController {

    private final PatientService patientService;

    public PatientController(PatientService patientService){
        this.patientService=patientService;
    }

    @PostMapping
    public ResponseEntity<PatientResponse> createPatient(@Valid @RequestBody PatientRequest newPatient){
        PatientResponse createdPatient = patientService.createPatient(newPatient);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(createdPatient);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PatientResponse> getPatientById(@PathVariable Long id){
        PatientResponse patient = patientService.getPatientById(id);

        return ResponseEntity.ok(patient);
    }

    @GetMapping
    public ResponseEntity<List<PatientResponse>> getAllPatients(){

        List<PatientResponse> patients = patientService.getAllPatients();

        return ResponseEntity.ok(patients);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PatientResponse> updatePatient(@PathVariable Long id, @Valid @RequestBody PatientRequest patientUpdate){
        PatientResponse updatedPatient =patientService.updatePatient(id, patientUpdate);

        return ResponseEntity.ok(updatedPatient);
    }

}
