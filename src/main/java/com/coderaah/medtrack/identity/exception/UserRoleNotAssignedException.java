package com.coderaah.medtrack.identity.exception;

public class UserRoleNotAssignedException extends RuntimeException {

    public UserRoleNotAssignedException(String message) {
        super(message);
    }
}