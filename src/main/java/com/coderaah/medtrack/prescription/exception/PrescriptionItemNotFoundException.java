package com.coderaah.medtrack.prescription.exception;

public class PrescriptionItemNotFoundException extends RuntimeException {

    public PrescriptionItemNotFoundException(String message) {
        super(message);
    }
}
