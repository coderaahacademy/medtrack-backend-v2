package com.coderaah.medtrack.visit.exception;

public class InvalidVisitStateException extends RuntimeException {
    public InvalidVisitStateException(String message) {
        super(message);
    }
}