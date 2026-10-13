package com.coderaah.medtrack.patient.dto;

import com.coderaah.medtrack.patient.domain.AllergySeverity;
import com.coderaah.medtrack.patient.domain.AllergyStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public class AllergyRequest {
    @NotBlank
    @Size(max = 255)
    private String allergen;

    @Size(max = 255)
    private String reaction;

    private AllergySeverity severity;
    private AllergyStatus status;
    private LocalDateTime recordedAt;

    public String getAllergen(){
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

    public void setSeverity(AllergySeverity severity) {
        this.severity = severity;
    }

    public AllergyStatus getStatus(){
        return status;
    }

    public void setStatus (AllergyStatus status){
        this.status=status;
    }
    public LocalDateTime getRecordedAt(){
        return recordedAt;
    }
    public void setRecordedAt(LocalDateTime recordedAt){
        this.recordedAt=recordedAt;
    }
}
