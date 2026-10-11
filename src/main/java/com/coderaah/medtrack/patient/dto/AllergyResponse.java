package com.coderaah.medtrack.patient.dto;

import com.coderaah.medtrack.patient.domain.AllergySeverity;
import com.coderaah.medtrack.patient.domain.AllergyStatus;

import java.time.LocalDateTime;

public class AllergyResponse {
    private Long id;
    private Long patientId;
    private String allergen;
    private String reaction;
    private AllergySeverity severity;
    private AllergyStatus status;
    private LocalDateTime recordedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Long getId(){
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
    public Long getPatientId(){
        return patientId;
    }
    public void setPatientId(Long patientId){
        this.patientId=patientId;
    }

    public String getAllergen() {
        return allergen;
    }

    public void setAllergen(String allergen) {
        this.allergen = allergen;
    }
    public String getReaction(){
        return reaction;
    }
    public void setReaction(String reaction){
        this.reaction=reaction;
    }
    public AllergySeverity getSeverity(){
        return severity;
    }
    public void setSeverity(AllergySeverity severity){
        this.severity=severity;
    }
    public AllergyStatus getStatus(){
        return status;
    }

    public void setStatus(AllergyStatus status) {
        this.status = status;
    }
    public LocalDateTime getRecordedAt(){
        return recordedAt;
    }
    public void setRecordedAt(LocalDateTime recordedAt) {
        this.recordedAt = recordedAt;
    }
    public LocalDateTime getCreatedAt(){
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
