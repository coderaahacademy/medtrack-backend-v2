package com.coderaah.medtrack.identity.exception;

public class DuplicateUserAccountException extends RuntimeException {

    public DuplicateUserAccountException(String message) {
        super(message);
    }
}