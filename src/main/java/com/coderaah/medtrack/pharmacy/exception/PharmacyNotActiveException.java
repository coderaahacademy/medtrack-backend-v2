package com.coderaah.medtrack.pharmacy.exception;

public class PharmacyNotActiveException extends RuntimeException {
    public PharmacyNotActiveException(String message) {
        super(message);
    }
}
