package com.coderaah.medtrack.prescription.service;

import com.coderaah.medtrack.identity.domain.UserAccount;
import com.coderaah.medtrack.identity.exception.UserAccountNotFoundException;
import com.coderaah.medtrack.identity.repository.UserAccountRepository;
import com.coderaah.medtrack.prescription.domain.Prescription;
import com.coderaah.medtrack.prescription.domain.PrescriptionStatus;
import com.coderaah.medtrack.prescription.domain.PrescriptionStatusHistory;
import com.coderaah.medtrack.prescription.exception.InvalidPrescriptionOperationException;
import com.coderaah.medtrack.prescription.repository.PrescriptionRepository;
import com.coderaah.medtrack.prescription.repository.PrescriptionStatusHistoryRepository;
import java.time.LocalDate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class PrescriptionStatusTransitionService {

    private final PrescriptionRepository prescriptionRepository;
    private final PrescriptionStatusHistoryRepository historyRepository;
    private final UserAccountRepository userAccountRepository;

    public PrescriptionStatusTransitionService(
            PrescriptionRepository prescriptionRepository,
            PrescriptionStatusHistoryRepository historyRepository,
            UserAccountRepository userAccountRepository) {
        this.prescriptionRepository = prescriptionRepository;
        this.historyRepository = historyRepository;
        this.userAccountRepository = userAccountRepository;
    }

    public UserAccount findActor(Long actorUserId) {
        if (actorUserId == null) {
            throw new InvalidPrescriptionOperationException(
                    "actorUserId is required to record the operation.");
        }

        return userAccountRepository.findById(actorUserId)
                .orElseThrow(() -> new UserAccountNotFoundException(
                        "User account not found: " + actorUserId));
    }

    public boolean isExpired(Prescription prescription) {
        if (prescription.getExpiresAt() == null) {
            return false;
        }

        PrescriptionStatus status = prescription.getStatus();
        if (status == PrescriptionStatus.DRAFT
                || status == PrescriptionStatus.CANCELLED
                || status == PrescriptionStatus.FULFILLED) {
            return false;
        }

        return prescription.getExpiresAt().isBefore(LocalDate.now());
    }


    public Prescription changeStatus(
            Prescription prescription,
            PrescriptionStatus newStatus,
            UserAccount actor,
            String reason) {

        PrescriptionStatus oldStatus = prescription.getStatus();

        prescription.setStatus(newStatus);
        Prescription saved = prescriptionRepository.save(prescription);

        recordStatusChange(saved, oldStatus, newStatus, actor, reason);

        return saved;
    }

    public void recordStatusChange(
            Prescription prescription,
            PrescriptionStatus oldStatus,
            PrescriptionStatus newStatus,
            UserAccount actor,
            String reason) {

        PrescriptionStatusHistory history = new PrescriptionStatusHistory(
                prescription, oldStatus, newStatus, actor, reason);

        historyRepository.save(history);
    }
}