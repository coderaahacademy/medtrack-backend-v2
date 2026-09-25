package com.coderaah.medtrack.medication.exception;

public class MedicationNotFoundException  extends RuntimeException{
    public MedicationNotFoundException(String message){
        super(message);
    }
}
