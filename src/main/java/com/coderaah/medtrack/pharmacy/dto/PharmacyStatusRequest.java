package com.coderaah.medtrack.pharmacy.dto;

import jakarta.validation.constraints.NotNull;

public class PharmacyStatusRequest {

    @NotNull
    private Boolean active;

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }
}