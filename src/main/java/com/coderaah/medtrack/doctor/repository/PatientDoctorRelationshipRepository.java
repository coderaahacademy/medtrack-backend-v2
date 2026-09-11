package com.coderaah.medtrack.doctor.repository;

import com.coderaah.medtrack.doctor.domain.DoctorRelationshipType;
import com.coderaah.medtrack.doctor.domain.PatientDoctorRelationship;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PatientDoctorRelationshipRepository extends JpaRepository<PatientDoctorRelationship, Long> {

    List<PatientDoctorRelationship> findByPatient_IdAndActiveTrue(Long patientId);

    List<PatientDoctorRelationship> findByDoctor_IdAndActiveTrue(Long doctorId);

    Optional<PatientDoctorRelationship> findByPatient_IdAndRelationshipTypeAndActiveTrue(
            Long patientId, DoctorRelationshipType relationshipType);

}
