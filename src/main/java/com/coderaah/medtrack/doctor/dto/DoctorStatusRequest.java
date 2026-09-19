package com.coderaah.medtrack.doctor.dto;

import jakarta.validation.constraints.NotNull;

public class DoctorStatusRequest {

    @NotNull
    private Boolean active;

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }
}