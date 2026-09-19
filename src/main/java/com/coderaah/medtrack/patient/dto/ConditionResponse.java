package com.coderaah.medtrack.patient.dto;

import com.coderaah.medtrack.patient.domain.PatientConditionStatus;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public class ConditionResponse {
        private Long id;
        private Long patientId;
        private String conditionName;
        private String conditionCode;
        private LocalDateTime diagnosedAt;
        private PatientConditionStatus status;
        private String notes;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;


        public Long getId(){
            return id;
        }
        public void setId(Long id){
            this.id=id;
        }

        public Long getPatientId() {
            return patientId;
        }

        public void setPatientId(Long patientId) {
            this.patientId = patientId;
        }
        public String getConditionName(){
            return conditionName;
        }
        public void setConditionName(String conditionName){
            this.conditionName=conditionName;
        }

        public String getConditionCode(){
            return conditionCode;
        }
        public void setConditionCode(String conditionCode){
            this.conditionCode=conditionCode;
        }
        public LocalDateTime getDiagnosedAt(){
            return diagnosedAt;
        }

        public void setDiagnosedAt(LocalDateTime diagnosedAt) {
        this.diagnosedAt = diagnosedAt;
        }

        public PatientConditionStatus getStatus() {
        return status;
        }
        public void setStatus(PatientConditionStatus status){
            this.status=status;
        }
        public String getNotes(){
            return notes;
        }
        public void setNotes(String notes){
            this.notes=notes;
        }
         public LocalDateTime getCreatedAt() {
             return createdAt;
        }

        public void setCreatedAt(LocalDateTime createdAt) {
           this.createdAt = createdAt;
        }

         public LocalDateTime getUpdatedAt() {
             return updatedAt;
        }

        public void setUpdatedAt(LocalDateTime updatedAt) {
             this.updatedAt = updatedAt;
        }
}


