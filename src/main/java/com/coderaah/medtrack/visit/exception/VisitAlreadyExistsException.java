package com.coderaah.medtrack.visit.exception;

public class VisitAlreadyExistsException extends RuntimeException {
    public VisitAlreadyExistsException(String message) {
        super(message);
    }
}