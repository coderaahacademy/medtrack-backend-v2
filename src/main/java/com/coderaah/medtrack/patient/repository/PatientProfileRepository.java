package com.coderaah.medtrack.patient.repository;

import com.coderaah.medtrack.patient.domain.PatientProfile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PatientProfileRepository extends JpaRepository<PatientProfile,Long> {

    boolean existsByMedicalRecordNumber(String medicalRecordNumber);

    boolean existsByMedicalRecordNumberAndIdNot(String medicalRecordNumber,Long id);
}
