package com.coderaah.medtrack.doctor.dto;


public class DoctorResponse {
    Long id;
    Long personId;
    String firstName;
    String lastName;
    String licenseNumber;
    String professionalPhone;
    String timeZone;
    boolean active;

    // Getters

    public Long getId() {
        return id;
    }

    public Long getPersonId() {
        return personId;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
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

    public boolean isActive() {
        return active;
    }

    // Setters


    public void setId(Long id) {
        this.id = id;
    }

    public void setPersonId(Long personId) {
        this.personId = personId;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
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

    public void setActive(boolean active) {
        this.active = active;
    }
}
