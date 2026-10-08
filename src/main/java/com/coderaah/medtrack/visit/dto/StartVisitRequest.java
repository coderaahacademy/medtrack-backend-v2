package com.coderaah.medtrack.visit.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class StartVisitRequest {

    @NotNull
    private Long patientId;

    @NotNull
    private Long doctorId;

    @Size(max = 5000)
    private String symptoms;

    public Long getPatientId() { return patientId; }
    public void setPatientId(Long patientId) { this.patientId = patientId; }

    public Long getDoctorId() { return doctorId; }
    public void setDoctorId(Long doctorId) { this.doctorId = doctorId; }

    public String getSymptoms() { return symptoms; }
    public void setSymptoms(String symptoms) { this.symptoms = symptoms; }
}