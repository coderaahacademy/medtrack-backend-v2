package com.coderaah.medtrack.prescription.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CancelPrescriptionRequest {

    @NotBlank
    @Size(max = 1000)
    private String cancellationReason;

    public String getCancellationReason() {
        return cancellationReason;
    }

    public void setCancellationReason(String cancellationReason) {
        this.cancellationReason = cancellationReason;
    }
}