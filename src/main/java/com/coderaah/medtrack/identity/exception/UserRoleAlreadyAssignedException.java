package com.coderaah.medtrack.identity.exception;

public class UserRoleAlreadyAssignedException extends RuntimeException {

    public UserRoleAlreadyAssignedException(String message) {
        super(message);
    }
}