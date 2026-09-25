package com.coderaah.medtrack.medication.dto;

import com.coderaah.medtrack.medication.domain.DosageForm;

public class MedicationResponse {
    private Long id;
    private String genericName;
    private String brandName;
    private String strength;
    private String strengthUnit;
    private DosageForm dosageForm;
    private String manufacturer;
    private boolean active;

    public Long getId() {
        return id;
    }

    public String getGenericName() {
        return genericName;
    }

    public String getBrandName() {
        return brandName;
    }

    public String getStrength() {
        return strength;
    }

    public String getStrengthUnit() {
        return strengthUnit;
    }

    public DosageForm getDosageForm() {
        return dosageForm;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public boolean isActive() {
        return active;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setGenericName(String genericName) {
        this.genericName = genericName;
    }

    public void setBrandName(String brandName) {
        this.brandName = brandName;
    }

    public void setStrength(String strength) {
        this.strength = strength;
    }

    public void setStrengthUnit(String strengthUnit) {
        this.strengthUnit = strengthUnit;
    }

    public void setDosageForm(DosageForm dosageForm) {
        this.dosageForm = dosageForm;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
