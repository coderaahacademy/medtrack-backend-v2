package com.coderaah.medtrack.pharmacy.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public class ReorderLevelRequest {

    @NotNull
    @PositiveOrZero
    private Integer reorderLevel;

    public Integer getReorderLevel() {
        return reorderLevel;
    }

    public void setReorderLevel(Integer reorderLevel) {
        this.reorderLevel = reorderLevel;
    }
}