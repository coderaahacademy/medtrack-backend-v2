package com.coderaah.medtrack.pharmacy.exception;

public class PrescriptionSubmissionNotFoundException extends RuntimeException {
    public PrescriptionSubmissionNotFoundException(String message) {
        super(message);
    }
}