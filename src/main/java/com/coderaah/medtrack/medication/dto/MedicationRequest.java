package com.coderaah.medtrack.medication.dto;

import com.coderaah.medtrack.medication.domain.DosageForm;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class MedicationRequest {


    @NotBlank
    @Size(max = 255)
    private String genericName;

    @Size(max = 255)
    private String brandName;

    @NotBlank
    @Size(max = 50)
    private String strength;

    @NotBlank
    @Size(max = 50)
    private String strengthUnit;

    @NotNull
    private DosageForm dosageForm;

    @Size(max = 255)
    private String manufacturer;

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
}
