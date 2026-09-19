package com.coderaah.medtrack.patient.repository;

import com.coderaah.medtrack.patient.domain.PatientCondition;
import com.coderaah.medtrack.patient.domain.PatientConditionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PatientConditionRepository extends JpaRepository<PatientCondition, Long> {
    List<PatientCondition> findByPatientId(Long patientId);
    List<PatientCondition> findByPatientIdAndStatus(Long patientId, PatientConditionStatus status);
    Optional<PatientCondition> findByIdAndPatientI(Long Id,Long patientId);

}
