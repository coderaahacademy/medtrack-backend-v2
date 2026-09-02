package com.coderaah.medtrack.patient.dto;

import com.coderaah.medtrack.patient.domain.BloodType;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;

public class PatientRequest {


    @NotBlank
    private String firstName;

    @NotBlank
    private String lastName;

    private LocalDate birthDate;

    private String phone;

    @NotBlank
    private String medicalRecordNumber;

    private BloodType bloodType;

    private String insuranceProvider;

    private String insuranceNumber;

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public String getPhone() {
        return phone;
    }

    public String getMedicalRecordNumber() {
        return medicalRecordNumber;
    }

    public BloodType getBloodType() {
        return bloodType;
    }

    public String getInsuranceProvider() {
        return insuranceProvider;
    }

    public String getInsuranceNumber() {
        return insuranceNumber;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setMedicalRecordNumber(String medicalRecordNumber) {
        this.medicalRecordNumber = medicalRecordNumber;
    }

    public void setBloodType(BloodType bloodType) {
        this.bloodType = bloodType;
    }

    public void setInsuranceProvider(String insuranceProvider) {
        this.insuranceProvider = insuranceProvider;
    }

    public void setInsuranceNumber(String insuranceNumber) {
        this.insuranceNumber = insuranceNumber;
    }
}
