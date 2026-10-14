package com.coderaah.medtrack.pharmacy.exception;

public class DuplicateInventoryItemException extends RuntimeException {
    public DuplicateInventoryItemException(String message) {
        super(message);
    }
}
