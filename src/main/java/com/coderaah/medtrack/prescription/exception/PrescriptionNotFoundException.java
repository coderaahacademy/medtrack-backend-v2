package com.coderaah.medtrack.prescription.exception;

public class PrescriptionNotFoundException extends RuntimeException {

    public PrescriptionNotFoundException(String message) {
        super(message);
    }
}
