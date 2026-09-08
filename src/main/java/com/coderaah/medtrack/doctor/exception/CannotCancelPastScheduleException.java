package com.coderaah.medtrack.doctor.exception;

public class CannotCancelPastScheduleException extends RuntimeException {

    public CannotCancelPastScheduleException(String message) {
        super(message);
    }
}