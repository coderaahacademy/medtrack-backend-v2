package com.coderaah.medtrack.visit.exception;

public class DuplicateVisitForAppointmentException extends RuntimeException {
    public DuplicateVisitForAppointmentException(Long appointmentId) {
        super("A visit already exists for appointment id: " + appointmentId);
    }
}