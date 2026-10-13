package com.coderaah.medtrack.pharmacy.controller;

import com.coderaah.medtrack.pharmacy.dto.PharmacyRequest;
import com.coderaah.medtrack.pharmacy.dto.PharmacyResponse;
import com.coderaah.medtrack.pharmacy.dto.PharmacyStatusRequest;
import com.coderaah.medtrack.pharmacy.service.PharmacyService;
import jakarta.validation.Valid;
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

import java.util.List;

@RestController
@RequestMapping("/api/pharmacies")
public class PharmacyController {

    private final PharmacyService pharmacyService;

    public PharmacyController(PharmacyService pharmacyService) {
        this.pharmacyService = pharmacyService;
    }

    @PostMapping
    public ResponseEntity<PharmacyResponse> createPharmacy(
            @Valid @RequestBody PharmacyRequest request) {

        PharmacyResponse createdPharmacy = pharmacyService.createPharmacy(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(createdPharmacy);
    }

    @GetMapping
    public ResponseEntity<List<PharmacyResponse>> listPharmacies() {
        return ResponseEntity.ok(pharmacyService.listPharmacies());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PharmacyResponse> getPharmacyById(@PathVariable Long id) {
        return ResponseEntity.ok(pharmacyService.getPharmacyById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PharmacyResponse> updatePharmacy(
            @PathVariable Long id,
            @Valid @RequestBody PharmacyRequest request) {

        return ResponseEntity.ok(pharmacyService.updatePharmacy(id, request));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<PharmacyResponse> updatePharmacyStatus(
            @PathVariable Long id,
            @Valid @RequestBody PharmacyStatusRequest request) {

        return ResponseEntity.ok(pharmacyService.updatePharmacyStatus(id, request.getActive()));
    }
}