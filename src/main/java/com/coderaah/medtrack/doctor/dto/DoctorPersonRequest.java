package com.coderaah.medtrack.doctor.dto;

import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;

public class DoctorPersonRequest {

    @NotBlank
    String firstName;

    @NotBlank
    String lastName;

    LocalDate birthDate;
    String phone;

    // Getters

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

    // Setters

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
}
