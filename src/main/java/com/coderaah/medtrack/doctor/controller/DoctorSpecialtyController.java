package com.coderaah.medtrack.doctor.controller;

import com.coderaah.medtrack.doctor.dto.AssignDoctorSpecialtyRequest;
import com.coderaah.medtrack.doctor.dto.DoctorSpecialtyResponse;
import com.coderaah.medtrack.doctor.service.DoctorSpecialtyService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/doctors/{doctorId}/specialties")
public class DoctorSpecialtyController {

    private final DoctorSpecialtyService doctorSpecialtyService;

    public DoctorSpecialtyController(DoctorSpecialtyService doctorSpecialtyService) {
        this.doctorSpecialtyService = doctorSpecialtyService;
    }

    @PostMapping
    public ResponseEntity<DoctorSpecialtyResponse> assign(@PathVariable Long doctorId,
                                                          @Valid @RequestBody AssignDoctorSpecialtyRequest request) {
        DoctorSpecialtyResponse response = DoctorSpecialtyResponse.from(doctorSpecialtyService.assign(doctorId, request));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public List<DoctorSpecialtyResponse> findByDoctor(@PathVariable Long doctorId) {
        return doctorSpecialtyService.findByDoctor(doctorId).stream().map(DoctorSpecialtyResponse::from).toList();
    }

    @DeleteMapping("/{specialtyId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void remove(@PathVariable Long doctorId, @PathVariable Long specialtyId) {
        doctorSpecialtyService.remove(doctorId, specialtyId);
    }
}