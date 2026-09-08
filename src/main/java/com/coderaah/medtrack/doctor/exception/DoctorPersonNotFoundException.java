package com.coderaah.medtrack.doctor.exception;

public class DoctorPersonNotFoundException extends RuntimeException {
    public DoctorPersonNotFoundException(String message) {
        super(message);
    }
}
