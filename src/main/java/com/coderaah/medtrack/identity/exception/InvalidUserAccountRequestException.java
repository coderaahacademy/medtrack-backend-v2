package com.coderaah.medtrack.identity.exception;

public class InvalidUserAccountRequestException extends RuntimeException {

    public InvalidUserAccountRequestException(String message) {
        super(message);
    }
}