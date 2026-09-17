package com.coderaah.medtrack.patient.dto;

import com.coderaah.medtrack.patient.domain.PatientConditionStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public class ConditionRequest {
    @NotBlank
    @Size (max =  255)
    private String conditionName;

    @Size (max =100)
    private String conditionCode;
    private LocalDateTime diagnosedAt;
    private PatientConditionStatus status;
    private String notes;

    public String getConditionName(){
        return conditionName;
    }
    public void setConditionName(String conditionName){
        this.conditionName=conditionName;
    }

    public String getConditionCode() {
        return conditionCode;
    }

    public void setConditionCode(String conditionCode) {
        this.conditionCode = conditionCode;
    }
    public LocalDateTime getDiagnosedAt(){
        return diagnosedAt;
    }
    public void setDiagnosedAt(LocalDateTime diagnosedAt){
        this.diagnosedAt=diagnosedAt;
    }

    public PatientConditionStatus getStatus() {
        return status;
    }

    public void setStatus(PatientConditionStatus status) {
        this.status = status;
    }
    public String getNotes(){
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
