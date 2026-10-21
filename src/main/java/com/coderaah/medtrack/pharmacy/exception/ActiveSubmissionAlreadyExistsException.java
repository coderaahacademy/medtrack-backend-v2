package com.coderaah.medtrack.pharmacy.exception;

public class ActiveSubmissionAlreadyExistsException extends RuntimeException {
    public ActiveSubmissionAlreadyExistsException(String message) {
        super(message);
    }
}