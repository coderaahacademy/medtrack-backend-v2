package com.coderaah.medtrack.pharmacy.dto;

import jakarta.validation.constraints.NotNull;

public class SubmitPrescriptionRequest {

    @NotNull
    private Long pharmacyId;

    public Long getPharmacyId() { return pharmacyId; }
    public void setPharmacyId(Long pharmacyId) { this.pharmacyId = pharmacyId; }
}