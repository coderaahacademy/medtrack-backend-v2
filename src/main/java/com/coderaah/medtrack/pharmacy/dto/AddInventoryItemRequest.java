package com.coderaah.medtrack.pharmacy.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public class AddInventoryItemRequest {

    @NotNull
    private Long medicationId;

    @NotNull
    @PositiveOrZero
    private Integer initialQuantity;

    @PositiveOrZero
    private Integer reorderLevel;

    public Long getMedicationId() {
        return medicationId;
    }

    public Integer getInitialQuantity() {
        return initialQuantity;
    }

    public Integer getReorderLevel() {
        return reorderLevel;
    }

    public void setMedicationId(Long medicationId) {
        this.medicationId = medicationId;
    }

    public void setInitialQuantity(Integer initialQuantity) {
        this.initialQuantity = initialQuantity;
    }

    public void setReorderLevel(Integer reorderLevel) {
        this.reorderLevel = reorderLevel;
    }
}