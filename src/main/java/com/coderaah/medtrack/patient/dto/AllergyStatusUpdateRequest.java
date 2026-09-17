package com.coderaah.medtrack.patient.dto;

import com.coderaah.medtrack.patient.domain.AllergyStatus;
import jakarta.validation.constraints.NotNull;

public class AllergyStatusUpdateRequest {
    @NotNull
    private AllergyStatus status;

    public AllergyStatus getStatus() {
        return status;
    }
    public void setStatus(AllergyStatus status){
        this.status=status;
    }
}
