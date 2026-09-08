package com.coderaah.medtrack.common.exception;

import com.coderaah.medtrack.doctor.exception.ActiveFamilyDoctorAlreadyExistsException;
import com.coderaah.medtrack.doctor.exception.AvailabilityRuleNotFoundException;
import com.coderaah.medtrack.doctor.exception.CannotCancelPastScheduleException;
import com.coderaah.medtrack.doctor.exception.DoctorNotFoundException;
import com.coderaah.medtrack.doctor.exception.DoctorPersonNotFoundException;
import com.coderaah.medtrack.doctor.exception.DoctorSpecialtyNotFoundException;
import com.coderaah.medtrack.doctor.exception.DuplicateDoctorPersonException;
import com.coderaah.medtrack.doctor.exception.DuplicateDoctorSpecialtyException;
import com.coderaah.medtrack.doctor.exception.DuplicateLicenseNumberException;
import com.coderaah.medtrack.doctor.exception.DuplicateSpecialtyCodeException;
import com.coderaah.medtrack.doctor.exception.InvalidDoctorRequestException;
import com.coderaah.medtrack.doctor.exception.InvalidScheduleTimeRangeException;
import com.coderaah.medtrack.doctor.exception.PatientDoctorRelationshipNotFoundException;
import com.coderaah.medtrack.doctor.exception.RelationshipAlreadyEndedException;
import com.coderaah.medtrack.doctor.exception.ScheduleExceptionNotFoundException;
import com.coderaah.medtrack.doctor.exception.SpecialtyNotFoundException;
import com.coderaah.medtrack.patient.exception.DuplicateMedicalRecordNumberException;
import com.coderaah.medtrack.patient.exception.PatientNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AvailabilityRuleNotFoundException.class)
    public ResponseEntity<String> handleAvailabilityRuleNotFound(
            AvailabilityRuleNotFoundException exception) {

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(exception.getMessage());
    }

    @ExceptionHandler(ScheduleExceptionNotFoundException.class)
    public ResponseEntity<String> handleScheduleExceptionNotFound(
            ScheduleExceptionNotFoundException exception) {

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(exception.getMessage());
    }

    @ExceptionHandler(InvalidScheduleTimeRangeException.class)
    public ResponseEntity<String> handleInvalidScheduleTimeRange(
            InvalidScheduleTimeRangeException exception) {

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(exception.getMessage());
    }

    @ExceptionHandler(CannotCancelPastScheduleException.class)
    public ResponseEntity<String> handleCannotCancelPastSchedule(
            CannotCancelPastScheduleException exception) {

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(exception.getMessage());
    }

    @ExceptionHandler(DoctorNotFoundException.class)
    public ResponseEntity<String> handleDoctorNotFound(
            DoctorNotFoundException exception) {

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(exception.getMessage());
    }

    @ExceptionHandler(DoctorPersonNotFoundException.class)
    public ResponseEntity<String> handleDoctorPersonNotFound(
            DoctorPersonNotFoundException exception) {

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(exception.getMessage());
    }

    @ExceptionHandler(DuplicateDoctorPersonException.class)
    public ResponseEntity<String> handleDuplicateDoctorPerson(
            DuplicateDoctorPersonException exception) {

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(exception.getMessage());
    }

    @ExceptionHandler(DuplicateLicenseNumberException.class)
    public ResponseEntity<String> handleDuplicateLicenseNumber(
            DuplicateLicenseNumberException exception) {

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(exception.getMessage());
    }

    @ExceptionHandler(InvalidDoctorRequestException.class)
    public ResponseEntity<String> handleInvalidDoctorRequest(
            InvalidDoctorRequestException exception) {

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(exception.getMessage());
    }

    @ExceptionHandler(PatientNotFoundException.class)
    public ResponseEntity<String> handlePatientNotFound(
            PatientNotFoundException exception) {

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(exception.getMessage());
    }

    @ExceptionHandler(DuplicateMedicalRecordNumberException.class)
    public ResponseEntity<String> handleDuplicateMedicalRecordNumber(
            DuplicateMedicalRecordNumberException exception) {

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(exception.getMessage());
    }

    @ExceptionHandler(SpecialtyNotFoundException.class)
    public ResponseEntity<String> handleSpecialtyNotFound(
            SpecialtyNotFoundException exception) {

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(exception.getMessage());
    }

    @ExceptionHandler(DuplicateSpecialtyCodeException.class)
    public ResponseEntity<String> handleDuplicateSpecialtyCode(
            DuplicateSpecialtyCodeException exception) {

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(exception.getMessage());
    }

    @ExceptionHandler(DuplicateDoctorSpecialtyException.class)
    public ResponseEntity<String> handleDuplicateDoctorSpecialty(
            DuplicateDoctorSpecialtyException exception) {

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(exception.getMessage());
    }

    @ExceptionHandler(DoctorSpecialtyNotFoundException.class)
    public ResponseEntity<String> handleDoctorSpecialtyNotFound(
            DoctorSpecialtyNotFoundException exception) {

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(exception.getMessage());
    }

    @ExceptionHandler(PatientDoctorRelationshipNotFoundException.class)
    public ResponseEntity<String> handlePatientDoctorRelationshipNotFound(
            PatientDoctorRelationshipNotFoundException exception) {

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(exception.getMessage());
    }

    @ExceptionHandler(ActiveFamilyDoctorAlreadyExistsException.class)
    public ResponseEntity<String> handleActiveFamilyDoctorAlreadyExists(
            ActiveFamilyDoctorAlreadyExistsException exception) {

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(exception.getMessage());
    }

    @ExceptionHandler(RelationshipAlreadyEndedException.class)
    public ResponseEntity<String> handleRelationshipAlreadyEnded(
            RelationshipAlreadyEndedException exception) {

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(exception.getMessage());
    }
}