package com.coderaah.medtrack.doctor.controller;

import com.coderaah.medtrack.doctor.dto.DoctorRequest;
import com.coderaah.medtrack.doctor.dto.DoctorResponse;
import com.coderaah.medtrack.doctor.dto.DoctorStatusRequest;
import com.coderaah.medtrack.doctor.dto.DoctorUpdateRequest;
import com.coderaah.medtrack.doctor.service.DoctorService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/doctors")
public class DoctorController {

    private final DoctorService doctorService;

    public DoctorController(DoctorService doctorService) {
        this.doctorService = doctorService;

    }

    @GetMapping("/{id}")
    public DoctorResponse getDoctorById(@PathVariable Long id) {
        return doctorService.getDoctorResponseById(id);
    }

    @GetMapping
    public List<DoctorResponse> getDoctorList() {
        return doctorService.getAllDoctors();
    }

    @PutMapping("/{id}")
    public DoctorResponse updateDoctor(
            @PathVariable Long id,
            @Valid @RequestBody DoctorUpdateRequest doctorRequest
    ) {
        return doctorService.updateDoctor(id, doctorRequest);
    }

    @PatchMapping("/{id}/status")
    public DoctorResponse updateDoctorStatus(
            @PathVariable Long id,
            @Valid @RequestBody DoctorStatusRequest doctorRequest
    ) {
        return doctorService.updateDoctorStatus(id, doctorRequest.getActive());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DoctorResponse registerDoctor(
            @Valid @RequestBody DoctorRequest doctorRequest
    ) {
        return doctorService.registerDoctor(doctorRequest);
    }


}
