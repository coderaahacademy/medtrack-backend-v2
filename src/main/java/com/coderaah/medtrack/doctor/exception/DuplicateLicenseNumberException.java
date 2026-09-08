package com.coderaah.medtrack.doctor.exception;

public class DuplicateLicenseNumberException extends RuntimeException {
    public DuplicateLicenseNumberException(String message) {
        super(message);
    }
}
