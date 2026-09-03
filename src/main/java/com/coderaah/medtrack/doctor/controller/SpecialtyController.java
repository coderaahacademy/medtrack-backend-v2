package com.coderaah.medtrack.doctor.controller;

import com.coderaah.medtrack.doctor.dto.CreateSpecialtyRequest;
import com.coderaah.medtrack.doctor.dto.SpecialtyResponse;
import com.coderaah.medtrack.doctor.service.SpecialtyService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/specialties")
public class SpecialtyController {

    private final SpecialtyService specialtyService;

    public SpecialtyController(SpecialtyService specialtyService) {
        this.specialtyService = specialtyService;
    }

    @PostMapping
    public ResponseEntity<SpecialtyResponse> create(@Valid @RequestBody CreateSpecialtyRequest request) {
        SpecialtyResponse response = SpecialtyResponse.from(specialtyService.create(request));
        return ResponseEntity.created(URI.create("/api/specialties/" + response.id())).body(response);
    }

    @GetMapping
    public List<SpecialtyResponse> findAll() {
        return specialtyService.findAll().stream().map(SpecialtyResponse::from).toList();
    }
}