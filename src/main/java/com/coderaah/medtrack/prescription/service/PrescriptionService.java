package com.coderaah.medtrack.prescription.service;

import com.coderaah.medtrack.identity.domain.UserAccount;
import com.coderaah.medtrack.identity.exception.UserAccountNotFoundException;
import com.coderaah.medtrack.identity.repository.UserAccountRepository;
import com.coderaah.medtrack.medication.domain.Medication;
import com.coderaah.medtrack.medication.exception.MedicationNotFoundException;
import com.coderaah.medtrack.medication.repository.MedicationRepository;
import com.coderaah.medtrack.prescription.domain.Prescription;
import com.coderaah.medtrack.prescription.domain.PrescriptionItem;
import com.coderaah.medtrack.prescription.domain.PrescriptionStatus;
import com.coderaah.medtrack.prescription.domain.PrescriptionStatusHistory;
import com.coderaah.medtrack.prescription.dto.AddPrescriptionItemRequest;
import com.coderaah.medtrack.prescription.dto.CancelPrescriptionRequest;
import com.coderaah.medtrack.prescription.dto.CreatePrescriptionRequest;
import com.coderaah.medtrack.prescription.dto.PrescriptionItemResponse;
import com.coderaah.medtrack.prescription.dto.PrescriptionResponse;
import com.coderaah.medtrack.prescription.dto.PrescriptionStatusHistoryResponse;
import com.coderaah.medtrack.prescription.exception.InvalidPrescriptionOperationException;
import com.coderaah.medtrack.prescription.exception.PrescriptionItemNotFoundException;
import com.coderaah.medtrack.prescription.exception.PrescriptionNotFoundException;
import com.coderaah.medtrack.prescription.repository.PrescriptionItemRepository;
import com.coderaah.medtrack.prescription.repository.PrescriptionRepository;
import com.coderaah.medtrack.prescription.repository.PrescriptionStatusHistoryRepository;
import com.coderaah.medtrack.visit.domain.Visit;
import com.coderaah.medtrack.visit.exception.VisitNotFoundException;
import com.coderaah.medtrack.visit.repository.VisitRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class PrescriptionService {

    private final PrescriptionRepository prescriptionRepository;
    private final PrescriptionItemRepository itemRepository;
    private final PrescriptionStatusHistoryRepository historyRepository;
    private final VisitRepository visitRepository;
    private final MedicationRepository medicationRepository;
    private final UserAccountRepository userAccountRepository;

    public PrescriptionService(
            PrescriptionRepository prescriptionRepository,
            PrescriptionItemRepository itemRepository,
            PrescriptionStatusHistoryRepository historyRepository,
            VisitRepository visitRepository,
            MedicationRepository medicationRepository,
            UserAccountRepository userAccountRepository) {
        this.prescriptionRepository = prescriptionRepository;
        this.itemRepository = itemRepository;
        this.historyRepository = historyRepository;
        this.visitRepository = visitRepository;
        this.medicationRepository = medicationRepository;
        this.userAccountRepository = userAccountRepository;
    }

    public PrescriptionResponse createPrescription(
            Long visitId,
            CreatePrescriptionRequest request,
            Long actorUserId) {

        Visit visit = visitRepository.findById(visitId)
                .orElseThrow(() -> new VisitNotFoundException("Visit not found: " + visitId));

        UserAccount actor = findActor(actorUserId);

        Prescription prescription = new Prescription(
                visit,
                PrescriptionStatus.DRAFT,
                null,
                request.getExpiresAt(),
                request.getNotes());

        prescription = prescriptionRepository.save(prescription);
        recordStatusChange(prescription, null, PrescriptionStatus.DRAFT, actor, null);

        return toResponse(prescription);
    }

    public PrescriptionItemResponse addItem(
            Long prescriptionId,
            AddPrescriptionItemRequest request) {

        Prescription prescription = findPrescription(prescriptionId);
        ensureDraft(prescription);

        Medication medication = findActiveMedication(request.getMedicationId());

        PrescriptionItem item = new PrescriptionItem(
                prescription,
                medication,
                request.getDosage(),
                request.getFrequency(),
                request.getRoute(),
                request.getDurationDays(),
                request.getQuantityPrescribed(),
                request.getInstructions());

        return toItemResponse(itemRepository.save(item));
    }

    public PrescriptionItemResponse updateItem(
            Long prescriptionId,
            Long itemId,
            AddPrescriptionItemRequest request) {

        Prescription prescription = findPrescription(prescriptionId);
        ensureDraft(prescription);

        PrescriptionItem item = itemRepository
                .findByIdAndPrescription_Id(itemId, prescriptionId)
                .orElseThrow(() -> new PrescriptionItemNotFoundException(
                        "Prescription item not found: " + itemId));

        Medication medication = findActiveMedication(request.getMedicationId());

        item.setMedication(medication);
        item.setDosage(request.getDosage());
        item.setFrequency(request.getFrequency());
        item.setRoute(request.getRoute());
        item.setDurationDays(request.getDurationDays());
        item.setQuantityPrescribed(request.getQuantityPrescribed());
        item.setInstructions(request.getInstructions());

        return toItemResponse(itemRepository.save(item));
    }

    public void removeItem(Long prescriptionId, Long itemId) {
        Prescription prescription = findPrescription(prescriptionId);
        ensureDraft(prescription);

        PrescriptionItem item = itemRepository
                .findByIdAndPrescription_Id(itemId, prescriptionId)
                .orElseThrow(() -> new PrescriptionItemNotFoundException(
                        "Prescription item not found: " + itemId));

        itemRepository.delete(item);
    }

    public PrescriptionResponse issue(Long prescriptionId, Long actorUserId) {
        Prescription prescription = findPrescription(prescriptionId);
        ensureDraft(prescription);

        if (itemRepository.findByPrescription_Id(prescriptionId).isEmpty()) {
            throw new InvalidPrescriptionOperationException(
                    "A prescription must contain at least one item before it can be issued.");
        }

        LocalDateTime issuedAt = LocalDateTime.now();
        LocalDate issueDate = issuedAt.toLocalDate();

        if (prescription.getExpiresAt() != null
                && prescription.getExpiresAt().isBefore(issueDate)) {
            throw new InvalidPrescriptionOperationException(
                    "The expiration date cannot be before the issue date.");
        }

        UserAccount actor = findActor(actorUserId);
        PrescriptionStatus oldStatus = prescription.getStatus();

        prescription.setIssuedAt(issuedAt);
        prescription.setStatus(PrescriptionStatus.ISSUED);
        prescription = prescriptionRepository.save(prescription);

        recordStatusChange(
                prescription, oldStatus, PrescriptionStatus.ISSUED, actor, null);

        return toResponse(prescription);
    }

    public PrescriptionResponse cancel(
            Long prescriptionId,
            CancelPrescriptionRequest request,
            Long actorUserId) {

        Prescription prescription = findPrescription(prescriptionId);
        PrescriptionStatus oldStatus = prescription.getStatus();

        if (oldStatus == PrescriptionStatus.FULFILLED) {
            throw new InvalidPrescriptionOperationException(
                    "A fulfilled prescription cannot be cancelled.");
        }

        if (oldStatus != PrescriptionStatus.DRAFT
                && oldStatus != PrescriptionStatus.ISSUED) {
            throw new InvalidPrescriptionOperationException(
                    "Only draft or issued prescriptions can be cancelled.");
        }

        String reason = request.getCancellationReason().trim();
        if (reason.length() > 255) {
            throw new InvalidPrescriptionOperationException(
                    "Cancellation reason must not exceed 255 characters.");
        }

        UserAccount actor = findActor(actorUserId);

        prescription.setStatus(PrescriptionStatus.CANCELLED);
        prescription.setCancelledAt(LocalDateTime.now());
        prescription.setCancellationReason(reason);
        prescription = prescriptionRepository.save(prescription);

        recordStatusChange(
                prescription, oldStatus, PrescriptionStatus.CANCELLED, actor, reason);

        return toResponse(prescription);
    }

    @Transactional(readOnly = true)
    public PrescriptionResponse getPrescription(Long prescriptionId) {
        return toResponse(findPrescription(prescriptionId));
    }

    @Transactional(readOnly = true)
    public List<PrescriptionResponse> getPrescriptionsForVisit(Long visitId) {
        if (!visitRepository.existsById(visitId)) {
            throw new VisitNotFoundException("Visit not found: " + visitId);
        }

        return prescriptionRepository.findByVisit_Id(visitId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PrescriptionResponse> getPrescriptionsForPatient(Long patientId) {
        return prescriptionRepository.findByVisit_Patient_Id(patientId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PrescriptionStatusHistoryResponse> getHistory(Long prescriptionId) {
        findPrescription(prescriptionId);

        return historyRepository
                .findByPrescription_IdOrderByChangedAtAsc(prescriptionId)
                .stream()
                .map(this::toHistoryResponse)
                .toList();
    }

    private Prescription findPrescription(Long prescriptionId) {
        return prescriptionRepository.findById(prescriptionId)
                .orElseThrow(() -> new PrescriptionNotFoundException(
                        "Prescription not found: " + prescriptionId));
    }

    private UserAccount findActor(Long actorUserId) {
        if (actorUserId == null) {
            throw new InvalidPrescriptionOperationException(
                    "actorUserId is required to record the operation.");
        }

        return userAccountRepository.findById(actorUserId)
                .orElseThrow(() -> new UserAccountNotFoundException(
                        "User account not found: " + actorUserId));
    }

    private Medication findActiveMedication(Long medicationId) {
        Medication medication = medicationRepository.findById(medicationId)
                .orElseThrow(() -> new MedicationNotFoundException(
                        "Medication not found: " + medicationId));

        if (!medication.isActive()) {
            throw new InvalidPrescriptionOperationException(
                    "Inactive medication cannot be added to a prescription.");
        }

        return medication;
    }

    private void ensureDraft(Prescription prescription) {
        if (prescription.getStatus() != PrescriptionStatus.DRAFT) {
            throw new InvalidPrescriptionOperationException(
                    "Prescription items can only be changed while the prescription is in DRAFT status.");
        }
    }

    private boolean isExpired(Prescription prescription) {
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

    private void recordStatusChange(
            Prescription prescription,
            PrescriptionStatus oldStatus,
            PrescriptionStatus newStatus,
            UserAccount actor,
            String reason) {

        PrescriptionStatusHistory history = new PrescriptionStatusHistory(
                prescription, oldStatus, newStatus, actor, reason);

        historyRepository.save(history);
    }

    private PrescriptionResponse toResponse(Prescription prescription) {
        PrescriptionResponse response = new PrescriptionResponse();

        response.setId(prescription.getId());
        response.setVisitId(prescription.getVisit().getId());
        response.setPatientId(prescription.getVisit().getPatient().getId());

        if (isExpired(prescription)) {
            response.setStatus(PrescriptionStatus.EXPIRED);
        } else {
            response.setStatus(prescription.getStatus());
        }

        response.setIssuedAt(prescription.getIssuedAt());
        response.setExpiresAt(prescription.getExpiresAt());
        response.setNotes(prescription.getNotes());
        response.setCancelledAt(prescription.getCancelledAt());
        response.setCancellationReason(prescription.getCancellationReason());
        response.setItems(itemRepository.findByPrescription_Id(prescription.getId())
                .stream()
                .map(this::toItemResponse)
                .toList());

        return response;
    }

    private PrescriptionItemResponse toItemResponse(PrescriptionItem item) {
        PrescriptionItemResponse response = new PrescriptionItemResponse();
        Medication medication = item.getMedication();

        response.setId(item.getId());
        response.setMedicationId(medication.getId());

        String medicationName = medication.getGenericName();
        if (medicationName == null || medicationName.isBlank()) {
            medicationName = medication.getBrandName();
        }

        response.setMedicationName(medicationName);
        response.setDosage(item.getDosage());
        response.setFrequency(item.getFrequency());
        response.setRoute(item.getRoute());
        response.setDurationDays(item.getDurationDays());
        response.setQuantityPrescribed(item.getQuantityPrescribed());
        response.setInstructions(item.getInstructions());

        return response;
    }

    private PrescriptionStatusHistoryResponse toHistoryResponse(
            PrescriptionStatusHistory history) {

        PrescriptionStatusHistoryResponse response =
                new PrescriptionStatusHistoryResponse();

        response.setId(history.getId());
        response.setOldStatus(history.getOldStatus());
        response.setNewStatus(history.getNewStatus());
        response.setChangedAt(history.getChangedAt());
        response.setReason(history.getReason());
        response.setChangedByUserId(history.getChangedByUser().getId());

        return response;
    }
}
