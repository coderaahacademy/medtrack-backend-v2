package com.coderaah.medtrack.appointment.exception;


public class CancellationReasonRequiredException extends RuntimeException {
    public CancellationReasonRequiredException(String message) {
        super(message);
    }
}
