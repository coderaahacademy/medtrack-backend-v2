package com.coderaah.medtrack.pharmacy.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "inventory_movements")
public class InventoryMovement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inventory_id", nullable = false)
    private PharmacyInventory inventory;

    @Enumerated(EnumType.STRING)
    @Column(name = "movement_type", nullable = false, length = 50)
    private InventoryMovementType movementType;

    @Column(name = "on_hand_delta", nullable = false)
    private Integer onHandDelta;

    @Column(name = "reserved_delta", nullable = false)
    private Integer reservedDelta;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fulfillment_item_id")
    private PrescriptionFulfillmentItem fulfillmentItem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "performed_by_membership_id")
    private PharmacyStaffMembership performedByMembership;

    @Column(name = "reason", length = 255)
    private String reason;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public InventoryMovement() {
    }

    public InventoryMovement(PharmacyInventory inventory, InventoryMovementType movementType, Integer onHandDelta, Integer reservedDelta) {
        this.inventory = inventory;
        this.movementType = movementType;
        this.onHandDelta = onHandDelta;
        this.reservedDelta = reservedDelta;
    }

    public InventoryMovement(PharmacyInventory inventory, InventoryMovementType movementType, Integer onHandDelta, Integer reservedDelta, PrescriptionFulfillmentItem fulfillmentItem, PharmacyStaffMembership performedByMembership, String reason) {
        this.inventory = inventory;
        this.movementType = movementType;
        this.onHandDelta = onHandDelta;
        this.reservedDelta = reservedDelta;
        this.fulfillmentItem = fulfillmentItem;
        this.performedByMembership = performedByMembership;
        this.reason = reason;
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public PharmacyInventory getInventory() {
        return inventory;
    }

    public void setInventory(PharmacyInventory inventory) {
        this.inventory = inventory;
    }

    public InventoryMovementType getMovementType() {
        return movementType;
    }

    public void setMovementType(InventoryMovementType movementType) {
        this.movementType = movementType;
    }

    public Integer getOnHandDelta() {
        return onHandDelta;
    }

    public void setOnHandDelta(Integer onHandDelta) {
        this.onHandDelta = onHandDelta;
    }

    public Integer getReservedDelta() {
        return reservedDelta;
    }

    public void setReservedDelta(Integer reservedDelta) {
        this.reservedDelta = reservedDelta;
    }

    public PrescriptionFulfillmentItem getFulfillmentItem() {
        return fulfillmentItem;
    }

    public void setFulfillmentItem(PrescriptionFulfillmentItem fulfillmentItem) {
        this.fulfillmentItem = fulfillmentItem;
    }

    public PharmacyStaffMembership getPerformedByMembership() {
        return performedByMembership;
    }

    public void setPerformedByMembership(PharmacyStaffMembership performedByMembership) {
        this.performedByMembership = performedByMembership;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof InventoryMovement that)) return false;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
