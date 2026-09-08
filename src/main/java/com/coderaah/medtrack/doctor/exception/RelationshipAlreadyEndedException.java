package com.coderaah.medtrack.doctor.exception;

public class RelationshipAlreadyEndedException extends RuntimeException {
    public RelationshipAlreadyEndedException(String message) {
        super(message);
    }
}
