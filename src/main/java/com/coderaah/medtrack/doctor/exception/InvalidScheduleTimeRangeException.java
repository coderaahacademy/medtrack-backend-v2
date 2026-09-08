package com.coderaah.medtrack.doctor.exception;

public class InvalidScheduleTimeRangeException extends RuntimeException {

    public InvalidScheduleTimeRangeException(String message) {
        super(message);
    }
}