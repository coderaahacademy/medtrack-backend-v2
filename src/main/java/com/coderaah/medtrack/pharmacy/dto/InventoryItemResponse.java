package com.coderaah.medtrack.pharmacy.dto;

public class InventoryItemResponse {

    private Long id;
    private Long pharmacyId;
    private Long medicationId;
    private String medicationName;
    private int quantityOnHand;
    private int quantityReserved;
    private int availableQuantity;
    private Integer reorderLevel;
    private boolean lowStock;

    public Long getId() {
        return id;
    }

    public Long getPharmacyId() {
        return pharmacyId;
    }

    public Long getMedicationId() {
        return medicationId;
    }

    public String getMedicationName() {
        return medicationName;
    }

    public int getQuantityOnHand() {
        return quantityOnHand;
    }

    public int getQuantityReserved() {
        return quantityReserved;
    }

    public int getAvailableQuantity() {
        return availableQuantity;
    }

    public Integer getReorderLevel() {
        return reorderLevel;
    }

    public boolean isLowStock() {
        return lowStock;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setPharmacyId(Long pharmacyId) {
        this.pharmacyId = pharmacyId;
    }

    public void setMedicationId(Long medicationId) {
        this.medicationId = medicationId;
    }

    public void setMedicationName(String medicationName) {
        this.medicationName = medicationName;
    }

    public void setQuantityOnHand(int quantityOnHand) {
        this.quantityOnHand = quantityOnHand;
    }

    public void setQuantityReserved(int quantityReserved) {
        this.quantityReserved = quantityReserved;
    }

    public void setAvailableQuantity(int availableQuantity) {
        this.availableQuantity = availableQuantity;
    }

    public void setReorderLevel(Integer reorderLevel) {
        this.reorderLevel = reorderLevel;
    }

    public void setLowStock(boolean lowStock) {
        this.lowStock = lowStock;
    }
}