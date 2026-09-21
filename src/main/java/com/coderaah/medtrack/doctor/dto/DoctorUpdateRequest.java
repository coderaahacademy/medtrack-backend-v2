package com.coderaah.medtrack.doctor.dto;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class DoctorUpdateRequest {

    @NotBlank
    @Size(max = 100)
    private String licenseNumber;

    @Size(max = 50)
    private String professionalPhone;

    @Size(max = 50)
    private String timeZone;

    public String getLicenseNumber(){
        return licenseNumber;
    }

    public void setLicenseNumber(String licenseNumber){
        this.licenseNumber = licenseNumber;
    }

    public String getProfessionalPhone(){
        return professionalPhone;
    }

    public void setProfessionalPhone(String professionalPhone){
        this.professionalPhone = professionalPhone;
    }

    public String getTimeZone(){
        return timeZone;
    }
    public void setTimeZone(String timeZone){
        this.timeZone = timeZone;
    }
}
