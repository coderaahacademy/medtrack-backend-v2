package com.coderaah.medtrack.pharmacy.service;

import com.coderaah.medtrack.identity.domain.UserAccount;
import com.coderaah.medtrack.pharmacy.domain.Pharmacy;
import com.coderaah.medtrack.pharmacy.domain.PrescriptionSubmission;
import com.coderaah.medtrack.pharmacy.domain.PrescriptionSubmissionStatus;
import com.coderaah.medtrack.pharmacy.dto.CancelSubmissionRequest;
import com.coderaah.medtrack.pharmacy.dto.PrescriptionSubmissionResponse;
import com.coderaah.medtrack.pharmacy.dto.RejectSubmissionRequest;
import com.coderaah.medtrack.pharmacy.dto.SubmitPrescriptionRequest;
import com.coderaah.medtrack.pharmacy.exception.ActiveSubmissionAlreadyExistsException;
import com.coderaah.medtrack.pharmacy.exception.InvalidSubmissionTransitionException;
import com.coderaah.medtrack.pharmacy.exception.PharmacyNotActiveException;
import com.coderaah.medtrack.pharmacy.exception.PharmacyNotFoundException;
import com.coderaah.medtrack.pharmacy.exception.PrescriptionNotSubmittableException;
import com.coderaah.medtrack.pharmacy.exception.PrescriptionSubmissionNotFoundException;
import com.coderaah.medtrack.pharmacy.repository.PharmacyRepository;
import com.coderaah.medtrack.pharmacy.repository.PrescriptionSubmissionRepository;
import com.coderaah.medtrack.prescription.domain.Prescription;
import com.coderaah.medtrack.prescription.domain.PrescriptionStatus;
import com.coderaah.medtrack.prescription.exception.PrescriptionNotFoundException;
import com.coderaah.medtrack.prescription.repository.PrescriptionRepository;
import com.coderaah.medtrack.prescription.service.PrescriptionStatusTransitionService;
import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class PrescriptionSubmissionService {

    private static final Set<PrescriptionSubmissionStatus> ACTIVE_STATUSES =
            EnumSet.of(PrescriptionSubmissionStatus.SUBMITTED,
                    PrescriptionSubmissionStatus.ACCEPTED);

    private final PrescriptionSubmissionRepository submissionRepository;
    private final PrescriptionRepository prescriptionRepository;
    private final PharmacyRepository pharmacyRepository;
    private final PrescriptionStatusTransitionService transitionService;

    public PrescriptionSubmissionService(
            PrescriptionSubmissionRepository submissionRepository,
            PrescriptionRepository prescriptionRepository,
            PharmacyRepository pharmacyRepository,
            PrescriptionStatusTransitionService transitionService) {
        this.submissionRepository = submissionRepository;
        this.prescriptionRepository = prescriptionRepository;
        this.pharmacyRepository = pharmacyRepository;
        this.transitionService = transitionService;
    }

    public PrescriptionSubmissionResponse submit(
            Long prescriptionId,
            SubmitPrescriptionRequest request,
            Long actorUserId) {

        // Row lock: two parallel submits for the same prescription run one after the other
        Prescription prescription = prescriptionRepository.findByIdForUpdate(prescriptionId)
                .orElseThrow(() -> new PrescriptionNotFoundException(
                        "Prescription not found: " + prescriptionId));

        Pharmacy pharmacy = pharmacyRepository.findById(request.getPharmacyId())
                .orElseThrow(() -> new PharmacyNotFoundException(
                        "Pharmacy with id " + request.getPharmacyId() + " not found"));

        if (!pharmacy.isActive()) {
            throw new PharmacyNotActiveException(
                    "Pharmacy " + pharmacy.getId() + " is not active");
        }

        if (transitionService.isExpired(prescription)) {
            throw new PrescriptionNotSubmittableException(
                    "An expired prescription cannot be submitted.");
        }

        if (prescription.getStatus() != PrescriptionStatus.ISSUED) {
            throw new PrescriptionNotSubmittableException(
                    "Only an ISSUED prescription can be submitted, but the status is "
                            + prescription.getStatus());
        }

        if (submissionRepository.existsByPrescription_IdAndStatusIn(
                prescriptionId, ACTIVE_STATUSES)) {
            throw new ActiveSubmissionAlreadyExistsException(
                    "Prescription " + prescriptionId + " already has an active submission");
        }

        UserAccount actor = transitionService.findActor(actorUserId);

        PrescriptionSubmission submission = new PrescriptionSubmission(
                prescription, pharmacy,
                PrescriptionSubmissionStatus.SUBMITTED, LocalDateTime.now());
        submission = submissionRepository.save(submission);

        transitionService.changeStatus(
                prescription, PrescriptionStatus.SUBMITTED, actor,
                "Submitted to pharmacy " + pharmacy.getId());

        return toResponse(submission);
    }

    @Transactional(readOnly = true)
    public PrescriptionSubmissionResponse getSubmission(Long id) {
        return toResponse(findSubmission(id));
    }

    @Transactional(readOnly = true)
    public List<PrescriptionSubmissionResponse> getSubmissionsForPrescription(Long prescriptionId) {
        if (!prescriptionRepository.existsById(prescriptionId)) {
            throw new PrescriptionNotFoundException(
                    "Prescription not found: " + prescriptionId);
        }
        return submissionRepository
                .findByPrescription_IdOrderBySubmittedAtDesc(prescriptionId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PrescriptionSubmissionResponse> getSubmissionsForPharmacy(Long pharmacyId) {
        if (!pharmacyRepository.existsById(pharmacyId)) {
            throw new PharmacyNotFoundException(
                    "Pharmacy with id " + pharmacyId + " not found");
        }
        return submissionRepository
                .findByPharmacy_IdOrderBySubmittedAtDesc(pharmacyId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public PrescriptionSubmissionResponse accept(Long id) {
        PrescriptionSubmission submission = findSubmission(id);
        requireSubmitted(submission, "accept");

        if (transitionService.isExpired(submission.getPrescription())) {
            throw new InvalidSubmissionTransitionException(
                    "An expired prescription cannot be accepted.");
        }

        submission.setStatus(PrescriptionSubmissionStatus.ACCEPTED);
        submission.setRespondedAt(LocalDateTime.now());


        return toResponse(submissionRepository.save(submission));
    }

    public PrescriptionSubmissionResponse reject(
            Long id, RejectSubmissionRequest request, Long actorUserId) {

        PrescriptionSubmission submission = findSubmission(id);
        requireSubmitted(submission, "reject");

        String reason = request.getRejectionReason().trim();
        if (reason.isEmpty()) {
            throw new InvalidSubmissionTransitionException("A rejection reason is required.");
        }

        UserAccount actor = transitionService.findActor(actorUserId);

        submission.setStatus(PrescriptionSubmissionStatus.REJECTED);
        submission.setRespondedAt(LocalDateTime.now());
        submission.setRejectionReason(reason);
        submission = submissionRepository.save(submission);

        returnPrescriptionToIssued(submission, actor, "Submission rejected: " + reason);

        return toResponse(submission);
    }

    public PrescriptionSubmissionResponse cancel(
            Long id, CancelSubmissionRequest request, Long actorUserId) {

        PrescriptionSubmission submission = findSubmission(id);
        requireSubmitted(submission, "cancel");

        UserAccount actor = transitionService.findActor(actorUserId);

        submission.setStatus(PrescriptionSubmissionStatus.CANCELLED);
        submission = submissionRepository.save(submission);

        String reason = (request == null || request.getReason() == null
                || request.getReason().isBlank())
                ? "Submission cancelled"
                : "Submission cancelled: " + request.getReason().trim();

        returnPrescriptionToIssued(submission, actor, reason);

        return toResponse(submission);
    }

    // ---------- Helper ----------

    private void returnPrescriptionToIssued(
            PrescriptionSubmission submission, UserAccount actor, String reason) {

        Prescription prescription = submission.getPrescription();
        if (prescription.getStatus() == PrescriptionStatus.SUBMITTED) {
            transitionService.changeStatus(
                    prescription, PrescriptionStatus.ISSUED, actor, reason);
        }
    }

    private void requireSubmitted(PrescriptionSubmission submission, String action) {
        if (submission.getStatus() != PrescriptionSubmissionStatus.SUBMITTED) {
            throw new InvalidSubmissionTransitionException(
                    "Cannot " + action + " a submission with status " + submission.getStatus());
        }
    }

    private PrescriptionSubmission findSubmission(Long id) {
        return submissionRepository.findById(id)
                .orElseThrow(() -> new PrescriptionSubmissionNotFoundException(
                        "Prescription submission not found: " + id));
    }

    private PrescriptionSubmissionResponse toResponse(PrescriptionSubmission submission) {
        PrescriptionSubmissionResponse response = new PrescriptionSubmissionResponse();
        response.setId(submission.getId());
        response.setPrescriptionId(submission.getPrescription().getId());
        response.setPharmacyId(submission.getPharmacy().getId());
        response.setStatus(submission.getStatus());
        response.setSubmittedAt(submission.getSubmittedAt());
        response.setRespondedAt(submission.getRespondedAt());
        response.setRejectionReason(submission.getRejectionReason());
        return response;
    }
}