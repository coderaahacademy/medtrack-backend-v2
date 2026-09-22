package com.coderaah.medtrack.medication.repository;

import com.coderaah.medtrack.medication.domain.Medication;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MedicationRepository extends JpaRepository<Medication, Long> {
    List<Medication> findByActive(boolean active);

    List<Medication> findByGenericNameContainingIgnoreCaseOrBrandNameContainingIgnoreCase(String genericName, String brandName);
}