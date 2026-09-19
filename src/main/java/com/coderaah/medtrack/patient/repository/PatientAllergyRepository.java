package com.coderaah.medtrack.patient.repository;

import com.coderaah.medtrack.patient.domain.AllergyStatus;
import com.coderaah.medtrack.patient.domain.PatientAllergy;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PatientAllergyRepository extends JpaRepository<PatientAllergy,Long> {

    List<PatientAllergy> findByPatientId(Long patientId);
    List<PatientAllergy> findByPatientIdAndStatus (Long patientId , AllergyStatus status);
    Optional<PatientAllergy> findByIdAndPatientId(Long Id , Long patientId);

}
