package com.coderaah.medtrack.prescription.repository;

import com.coderaah.medtrack.prescription.domain.PrescriptionStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PrescriptionStatusHistoryRepository
        extends JpaRepository<PrescriptionStatusHistory, Long> {

    List<PrescriptionStatusHistory> findByPrescription_IdOrderByChangedAtAsc(Long prescriptionId);
}