package com.coderaah.medtrack.appointment.exception;


public class AppointmentOverlapException extends RuntimeException {
    public AppointmentOverlapException(String message) {
        super(message);
    }
}
