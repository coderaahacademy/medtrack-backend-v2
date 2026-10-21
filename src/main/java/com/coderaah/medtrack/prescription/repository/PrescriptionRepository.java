package com.coderaah.medtrack.prescription.repository;

import com.coderaah.medtrack.prescription.domain.Prescription;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PrescriptionRepository extends JpaRepository<Prescription, Long> {

    List<Prescription> findByVisit_Id(Long visitId);

    List<Prescription> findByVisit_Patient_Id(Long patientId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Prescription p WHERE p.id = :id")
    Optional<Prescription> findByIdForUpdate(@Param("id") Long id);
}