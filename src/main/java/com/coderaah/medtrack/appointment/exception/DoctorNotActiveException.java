package com.coderaah.medtrack.appointment.exception;


public class DoctorNotActiveException extends RuntimeException {
    public DoctorNotActiveException(String message) {
        super(message);
    }
}