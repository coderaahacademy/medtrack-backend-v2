package com.coderaah.medtrack.prescription.repository;

import com.coderaah.medtrack.prescription.domain.Prescription;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PrescriptionRepository extends JpaRepository<Prescription, Long> {

    List<Prescription> findByVisit_Id(Long visitId);

    List<Prescription> findByVisit_Patient_Id(Long patientId);
}