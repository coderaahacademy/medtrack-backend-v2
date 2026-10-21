package com.coderaah.medtrack.pharmacy.repository;

import com.coderaah.medtrack.pharmacy.domain.PrescriptionSubmission;
import com.coderaah.medtrack.pharmacy.domain.PrescriptionSubmissionStatus;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PrescriptionSubmissionRepository
        extends JpaRepository<PrescriptionSubmission, Long> {

    List<PrescriptionSubmission> findByPrescription_IdOrderBySubmittedAtDesc(Long prescriptionId);

    List<PrescriptionSubmission> findByPharmacy_IdOrderBySubmittedAtDesc(Long pharmacyId);

    boolean existsByPrescription_IdAndStatusIn(
            Long prescriptionId, Collection<PrescriptionSubmissionStatus> statuses);
}