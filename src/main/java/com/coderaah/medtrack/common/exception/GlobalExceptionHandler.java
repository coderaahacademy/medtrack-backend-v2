package com.coderaah.medtrack.common.exception;

import com.coderaah.medtrack.patient.exception.AllergyNotFoundException;
import com.coderaah.medtrack.patient.exception.ConditionNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;

public class GlobalExceptionHandler {
    @ExceptionHandler(AllergyNotFoundException.class)
            public ResponseEntity<String> handleAllergyNotFound(AllergyNotFoundException exception){
             return ResponseEntity.status(HttpStatus.NOT_FOUND).body(exception.getMessage());
    }

    @ExceptionHandler(ConditionNotFoundException.class)
    public ResponseEntity<String> handleConditionNotFound(ConditionNotFoundException exception){
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(exception.getMessage());
    }

}
