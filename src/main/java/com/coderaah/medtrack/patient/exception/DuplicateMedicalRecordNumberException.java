package com.coderaah.medtrack.patient.exception;

public class DuplicateMedicalRecordNumberException extends RuntimeException{

    public DuplicateMedicalRecordNumberException(String message){
        super(message);
    }
}
