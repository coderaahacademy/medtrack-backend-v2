package com.coderaah.medtrack.prescription.repository;

import com.coderaah.medtrack.prescription.domain.PrescriptionItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PrescriptionItemRepository extends JpaRepository<PrescriptionItem, Long> {

    List<PrescriptionItem> findByPrescription_Id(Long prescriptionId);

    Optional<PrescriptionItem> findByIdAndPrescription_Id(Long itemId, Long prescriptionId);
}