package com.coderaah.medtrack.visit.dto;

import jakarta.validation.constraints.Size;

public class UpdateClinicalNotesRequest {

    @Size(max = 5000)
    private String symptoms;

    @Size(max = 5000)
    private String diagnosis;

    @Size(max = 10000)
    private String clinicalNotes;

    public String getSymptoms() { return symptoms; }
    public void setSymptoms(String symptoms) { this.symptoms = symptoms; }

    public String getDiagnosis() { return diagnosis; }
    public void setDiagnosis(String diagnosis) { this.diagnosis = diagnosis; }

    public String getClinicalNotes() { return clinicalNotes; }
    public void setClinicalNotes(String clinicalNotes) { this.clinicalNotes = clinicalNotes; }
}