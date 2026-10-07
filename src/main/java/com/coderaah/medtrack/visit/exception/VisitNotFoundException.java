package com.coderaah.medtrack.visit.exception;

public class VisitNotFoundException extends RuntimeException {
    public VisitNotFoundException(Long id) {
        super("Visit not found with id: " + id);
    }
}