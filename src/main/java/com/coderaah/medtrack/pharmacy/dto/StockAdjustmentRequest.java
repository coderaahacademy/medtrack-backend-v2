package com.coderaah.medtrack.pharmacy.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class StockAdjustmentRequest {

    @NotNull
    private Integer quantityChange;

    @NotBlank
    @Size(max = 255)
    private String reason;

    public Integer getQuantityChange() {
        return quantityChange;
    }

    public String getReason() {
        return reason;
    }

    public void setQuantityChange(Integer quantityChange) {
        this.quantityChange = quantityChange;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}