package com.coderaah.medtrack.doctor.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;


public class DoctorRequest {
    Long personId;

    @Valid
    DoctorPersonRequest person;

    @NotBlank
    String licenseNumber;

    String professionalPhone;
    String timeZone;

    // Getters

    public Long getPersonId() {
        return personId;
    }

    public DoctorPersonRequest getPerson() {
        return person;
    }

    public String getLicenseNumber() {
        return licenseNumber;
    }

    public String getProfessionalPhone() {
        return professionalPhone;
    }

    public String getTimeZone() {
        return timeZone;
    }

    // Setters

    public void setPersonId(Long personId) {
        this.personId = personId;
    }

    public void setPerson(DoctorPersonRequest person) {
        this.person = person;
    }

    public void setLicenseNumber(String licenseNumber) {
        this.licenseNumber = licenseNumber;
    }

    public void setProfessionalPhone(String professionalPhone) {
        this.professionalPhone = professionalPhone;
    }

    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }
}
