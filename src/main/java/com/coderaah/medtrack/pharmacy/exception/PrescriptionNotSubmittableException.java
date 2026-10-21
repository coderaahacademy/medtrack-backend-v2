package com.coderaah.medtrack.pharmacy.exception;

public class PrescriptionNotSubmittableException extends RuntimeException {
    public PrescriptionNotSubmittableException(String message) {
        super(message);
    }
}