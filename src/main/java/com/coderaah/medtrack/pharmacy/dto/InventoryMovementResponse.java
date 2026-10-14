package com.coderaah.medtrack.pharmacy.dto;

import com.coderaah.medtrack.pharmacy.domain.InventoryMovementType;

import java.time.LocalDateTime;

public class InventoryMovementResponse {

    private Long id;
    private InventoryMovementType movementType;
    private int onHandDelta;
    private int reservedDelta;
    private String reason;
    private LocalDateTime createdAt;

    public Long getId() {
        return id;
    }

    public InventoryMovementType getMovementType() {
        return movementType;
    }

    public int getOnHandDelta() {
        return onHandDelta;
    }

    public int getReservedDelta() {
        return reservedDelta;
    }

    public String getReason() {
        return reason;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setMovementType(InventoryMovementType movementType) {
        this.movementType = movementType;
    }

    public void setOnHandDelta(int onHandDelta) {
        this.onHandDelta = onHandDelta;
    }

    public void setReservedDelta(int reservedDelta) {
        this.reservedDelta = reservedDelta;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}