package com.coderaah.medtrack.prescription.dto;

import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public class CreatePrescriptionRequest {

    private LocalDate expiresAt;

    @Size(max = 2000)
    private String notes;

    public LocalDate getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDate expiresAt) {
        this.expiresAt = expiresAt;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}