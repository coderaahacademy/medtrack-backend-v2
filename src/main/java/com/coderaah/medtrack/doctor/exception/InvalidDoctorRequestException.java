package com.coderaah.medtrack.doctor.exception;

public class InvalidDoctorRequestException extends RuntimeException {
    public InvalidDoctorRequestException(String message) {
        super(message);
    }
}
