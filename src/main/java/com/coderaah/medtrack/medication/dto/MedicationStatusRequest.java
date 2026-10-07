package com.coderaah.medtrack.medication.dto;

import jakarta.validation.constraints.NotNull;

public class MedicationStatusRequest {

    @NotNull
    private Boolean active;

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }
}
