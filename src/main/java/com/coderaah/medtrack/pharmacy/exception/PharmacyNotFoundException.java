package com.coderaah.medtrack.pharmacy.exception;

public class PharmacyNotFoundException extends RuntimeException {
    public PharmacyNotFoundException(String message) {
        super(message);
    }
}