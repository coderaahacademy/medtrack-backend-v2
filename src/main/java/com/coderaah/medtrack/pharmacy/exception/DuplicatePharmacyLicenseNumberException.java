package com.coderaah.medtrack.pharmacy.exception;

public class DuplicatePharmacyLicenseNumberException extends RuntimeException {
    public DuplicatePharmacyLicenseNumberException(String message) {
        super(message);
    }
}