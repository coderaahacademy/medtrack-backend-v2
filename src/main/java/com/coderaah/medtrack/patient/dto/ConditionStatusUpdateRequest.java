package com.coderaah.medtrack.patient.dto;

import com.coderaah.medtrack.patient.domain.PatientConditionStatus;
import jakarta.validation.constraints.NotNull;

public class ConditionStatusUpdateRequest {

    @NotNull
    private PatientConditionStatus status;

    public PatientConditionStatus getStatus() {
        return status;
    }

    public void setStatus(PatientConditionStatus status) {
        this.status = status;
    }
}
