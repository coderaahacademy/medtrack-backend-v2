package com.coderaah.medtrack.pharmacy.exception;

public class InvalidSubmissionTransitionException extends RuntimeException {
    public InvalidSubmissionTransitionException(String message) {
        super(message);
    }
}