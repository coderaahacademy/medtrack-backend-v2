package com.coderaah.medtrack.pharmacy.dto;

import com.coderaah.medtrack.pharmacy.domain.PrescriptionSubmissionStatus;
import java.time.LocalDateTime;

public class PrescriptionSubmissionResponse {

    private Long id;
    private Long prescriptionId;
    private Long pharmacyId;
    private PrescriptionSubmissionStatus status;
    private LocalDateTime submittedAt;
    private LocalDateTime respondedAt;
    private String rejectionReason;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getPrescriptionId() { return prescriptionId; }
    public void setPrescriptionId(Long prescriptionId) { this.prescriptionId = prescriptionId; }

    public Long getPharmacyId() { return pharmacyId; }
    public void setPharmacyId(Long pharmacyId) { this.pharmacyId = pharmacyId; }

    public PrescriptionSubmissionStatus getStatus() { return status; }
    public void setStatus(PrescriptionSubmissionStatus status) { this.status = status; }

    public LocalDateTime getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(LocalDateTime submittedAt) { this.submittedAt = submittedAt; }

    public LocalDateTime getRespondedAt() { return respondedAt; }
    public void setRespondedAt(LocalDateTime respondedAt) { this.respondedAt = respondedAt; }

    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }
}