package com.coderaah.medtrack.pharmacy.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class RejectSubmissionRequest {

    @NotBlank
    @Size(max = 255)
    private String rejectionReason;

    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }
}