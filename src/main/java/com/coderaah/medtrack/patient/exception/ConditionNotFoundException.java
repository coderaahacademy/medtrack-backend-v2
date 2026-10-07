package com.coderaah.medtrack.patient.exception;

    public class ConditionNotFoundException extends RuntimeException {
        public ConditionNotFoundException (String message){
            super(message);
        }
    }
