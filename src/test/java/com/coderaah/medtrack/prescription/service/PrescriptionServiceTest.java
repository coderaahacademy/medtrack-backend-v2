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
import com.coderaah.medtrack.prescription.dto.PrescriptionResponse;
import com.coderaah.medtrack.prescription.exception.InvalidPrescriptionOperationException;
import com.coderaah.medtrack.prescription.repository.PrescriptionItemRepository;
import com.coderaah.medtrack.prescription.repository.PrescriptionRepository;
import com.coderaah.medtrack.prescription.repository.PrescriptionStatusHistoryRepository;
import com.coderaah.medtrack.visit.domain.Visit;
import com.coderaah.medtrack.visit.exception.VisitNotFoundException;
import com.coderaah.medtrack.visit.repository.VisitRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.BeforeEach;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PrescriptionServiceTest {

    @Mock
    private PrescriptionRepository prescriptionRepository;

    @Mock
    private PrescriptionItemRepository itemRepository;

    @Mock
    private PrescriptionStatusHistoryRepository historyRepository;

    @Mock
    private VisitRepository visitRepository;

    @Mock
    private MedicationRepository medicationRepository;

    @Mock
    private UserAccountRepository userAccountRepository;

    private PrescriptionService prescriptionService;

    @BeforeEach
    void setUp() {
        PrescriptionStatusTransitionService transitionService =
                new PrescriptionStatusTransitionService(
                        prescriptionRepository,
                        historyRepository,
                        userAccountRepository);

        prescriptionService = new PrescriptionService(
                prescriptionRepository,
                itemRepository,
                historyRepository,
                visitRepository,
                medicationRepository,
                transitionService);
    }

    @Test
    void addItem_rejectsInactiveMedication() {
        Prescription prescription = mock(Prescription.class);
        Medication medication = mock(Medication.class);

        when(prescriptionRepository.findById(1L))
                .thenReturn(Optional.of(prescription));
        when(prescription.getStatus()).thenReturn(PrescriptionStatus.DRAFT);
        when(medicationRepository.findById(2L))
                .thenReturn(Optional.of(medication));
        when(medication.isActive()).thenReturn(false);

        AddPrescriptionItemRequest request = new AddPrescriptionItemRequest();
        request.setMedicationId(2L);

        assertThatThrownBy(() -> prescriptionService.addItem(1L, request))
                .isInstanceOf(InvalidPrescriptionOperationException.class);

        verify(itemRepository, never()).save(any());
    }

    @Test
    void issue_withItems_changesStatusAndRecordsHistory() {
        Prescription prescription = mock(
                Prescription.class, RETURNS_DEEP_STUBS);
        PrescriptionItem item = mock(PrescriptionItem.class);
        Medication medication = mock(Medication.class);
        UserAccount actor = mock(UserAccount.class);

        when(prescriptionRepository.findById(1L))
                .thenReturn(Optional.of(prescription));
        when(prescription.getId()).thenReturn(1L);
        when(prescription.getStatus()).thenReturn(PrescriptionStatus.DRAFT);
        when(prescription.getExpiresAt()).thenReturn(null);
        when(prescription.getVisit().getId()).thenReturn(4L);
        when(prescription.getVisit().getPatient().getId()).thenReturn(7L);

        when(itemRepository.findByPrescription_Id(1L))
                .thenReturn(List.of(item));
        when(item.getMedication()).thenReturn(medication);
        when(medication.getId()).thenReturn(2L);
        when(medication.getGenericName()).thenReturn("Test medication");
        when(item.getId()).thenReturn(3L);

        when(userAccountRepository.findById(9L))
                .thenReturn(Optional.of(actor));
        when(prescriptionRepository.save(prescription))
                .thenAnswer(invocation -> invocation.getArgument(0));

        prescriptionService.issue(1L, 9L);

        verify(prescription).setIssuedAt(any());
        verify(prescription).setStatus(PrescriptionStatus.ISSUED);
        verify(prescriptionRepository).save(prescription);

        ArgumentCaptor<PrescriptionStatusHistory> captor =
                ArgumentCaptor.forClass(PrescriptionStatusHistory.class);
        verify(historyRepository).save(captor.capture());

        PrescriptionStatusHistory savedHistory = captor.getValue();

        assertThat(savedHistory.getOldStatus())
                .isEqualTo(PrescriptionStatus.DRAFT);
        assertThat(savedHistory.getNewStatus())
                .isEqualTo(PrescriptionStatus.ISSUED);
        assertThat(savedHistory.getChangedByUser()).isSameAs(actor);
    }

    @Test
    void issue_rejectsPrescriptionWithoutItems() {
        Prescription prescription = mock(Prescription.class);

        when(prescriptionRepository.findById(1L))
                .thenReturn(Optional.of(prescription));
        when(prescription.getStatus()).thenReturn(PrescriptionStatus.DRAFT);
        when(itemRepository.findByPrescription_Id(1L))
                .thenReturn(List.of());

        assertThatThrownBy(() -> prescriptionService.issue(1L, 9L))
                .isInstanceOf(InvalidPrescriptionOperationException.class);

        verify(prescriptionRepository, never()).save(any());
        verify(historyRepository, never()).save(any());
    }

    @Test
    void cancel_issuedPrescription_changesStatusAndRecordsHistory() {
        Prescription prescription = mock(
                Prescription.class, RETURNS_DEEP_STUBS);
        UserAccount actor = mock(UserAccount.class);

        when(prescriptionRepository.findById(1L))
                .thenReturn(Optional.of(prescription));
        when(prescription.getStatus()).thenReturn(PrescriptionStatus.ISSUED);
        when(prescription.getExpiresAt()).thenReturn(null);
        when(prescription.getId()).thenReturn(1L);
        when(prescription.getVisit().getId()).thenReturn(4L);
        when(prescription.getVisit().getPatient().getId()).thenReturn(7L);
        when(itemRepository.findByPrescription_Id(1L))
                .thenReturn(List.of());
        when(userAccountRepository.findById(9L))
                .thenReturn(Optional.of(actor));
        when(prescriptionRepository.save(prescription))
                .thenAnswer(invocation -> invocation.getArgument(0));

        CancelPrescriptionRequest request = new CancelPrescriptionRequest();
        request.setCancellationReason("Patient requested cancellation");

        prescriptionService.cancel(1L, request, 9L);

        verify(prescription).setStatus(PrescriptionStatus.CANCELLED);
        verify(prescription).setCancelledAt(any());
        verify(prescription).setCancellationReason(
                "Patient requested cancellation");
        verify(prescriptionRepository).save(prescription);

        ArgumentCaptor<PrescriptionStatusHistory> captor =
                ArgumentCaptor.forClass(PrescriptionStatusHistory.class);
        verify(historyRepository).save(captor.capture());

        PrescriptionStatusHistory savedHistory = captor.getValue();

        assertThat(savedHistory.getOldStatus())
                .isEqualTo(PrescriptionStatus.ISSUED);
        assertThat(savedHistory.getNewStatus())
                .isEqualTo(PrescriptionStatus.CANCELLED);
        assertThat(savedHistory.getReason())
                .isEqualTo("Patient requested cancellation");
        assertThat(savedHistory.getChangedByUser()).isSameAs(actor);
    }

    @Test
    void cancel_rejectsFulfilledPrescription() {
        Prescription prescription = mock(Prescription.class);

        when(prescriptionRepository.findById(1L))
                .thenReturn(Optional.of(prescription));
        when(prescription.getStatus()).thenReturn(PrescriptionStatus.FULFILLED);

        CancelPrescriptionRequest request = new CancelPrescriptionRequest();
        request.setCancellationReason("Cancellation test");

        assertThatThrownBy(() ->
                prescriptionService.cancel(1L, request, 9L))
                .isInstanceOf(InvalidPrescriptionOperationException.class);

        verify(prescriptionRepository, never()).save(any());
        verify(historyRepository, never()).save(any());
    }

    @Test
    void createPrescription_startsAsDraftAndRecordsInitialHistory() {
        Visit visit = mock(Visit.class, RETURNS_DEEP_STUBS);
        UserAccount actor = mock(UserAccount.class);

        when(visitRepository.findById(4L))
                .thenReturn(Optional.of(visit));
        when(visit.getId()).thenReturn(4L);
        when(visit.getPatient().getId()).thenReturn(7L);
        when(userAccountRepository.findById(9L))
                .thenReturn(Optional.of(actor));
        when(prescriptionRepository.save(any(Prescription.class)))
                .thenAnswer(invocation -> {
                    Prescription saved = invocation.getArgument(0);
                    saved.setId(10L);
                    return saved;
                });
        when(itemRepository.findByPrescription_Id(10L))
                .thenReturn(List.of());

        CreatePrescriptionRequest request = new CreatePrescriptionRequest();

        PrescriptionResponse response =
                prescriptionService.createPrescription(4L, request, 9L);

        ArgumentCaptor<Prescription> prescriptionCaptor =
                ArgumentCaptor.forClass(Prescription.class);
        ArgumentCaptor<PrescriptionStatusHistory> historyCaptor =
                ArgumentCaptor.forClass(PrescriptionStatusHistory.class);

        verify(prescriptionRepository).save(prescriptionCaptor.capture());
        verify(historyRepository).save(historyCaptor.capture());

        Prescription savedPrescription = prescriptionCaptor.getValue();
        PrescriptionStatusHistory savedHistory = historyCaptor.getValue();

        assertThat(savedPrescription.getStatus())
                .isEqualTo(PrescriptionStatus.DRAFT);
        assertThat(savedPrescription.getIssuedAt()).isNull();

        assertThat(savedHistory.getOldStatus()).isNull();
        assertThat(savedHistory.getNewStatus())
                .isEqualTo(PrescriptionStatus.DRAFT);
        assertThat(savedHistory.getChangedByUser()).isSameAs(actor);

        assertThat(response.getStatus()).isEqualTo(PrescriptionStatus.DRAFT);
        assertThat(response.getVisitId()).isEqualTo(4L);
        assertThat(response.getPatientId()).isEqualTo(7L);
    }

    @Test
    void updateItem_rejectsIssuedPrescription() {
        Prescription prescription = mock(Prescription.class);

        when(prescriptionRepository.findById(1L))
                .thenReturn(Optional.of(prescription));
        when(prescription.getStatus()).thenReturn(PrescriptionStatus.ISSUED);

        AddPrescriptionItemRequest request = new AddPrescriptionItemRequest();
        request.setMedicationId(2L);
        request.setDosage("500 mg");
        request.setFrequency("Twice daily");
        request.setQuantityPrescribed(10);

        assertThatThrownBy(() ->
                prescriptionService.updateItem(1L, 3L, request))
                .isInstanceOf(InvalidPrescriptionOperationException.class);

        verify(itemRepository, never()).save(any());
    }

    @Test
    void issue_rejectsExpirationDateBeforeIssueDate() {
        Prescription prescription = mock(Prescription.class);

        when(prescriptionRepository.findById(1L))
                .thenReturn(Optional.of(prescription));
        when(prescription.getStatus()).thenReturn(PrescriptionStatus.DRAFT);
        when(prescription.getExpiresAt())
                .thenReturn(LocalDate.now().minusDays(1));
        when(itemRepository.findByPrescription_Id(1L))
                .thenReturn(List.of(mock(PrescriptionItem.class)));

        assertThatThrownBy(() -> prescriptionService.issue(1L, 9L))
                .isInstanceOf(InvalidPrescriptionOperationException.class);

        verify(prescriptionRepository, never()).save(any());
        verify(historyRepository, never()).save(any());
        verify(userAccountRepository, never()).findById(9L);
    }

    @Test
    void removeItem_rejectsIssuedPrescription() {
        Prescription prescription = mock(Prescription.class);

        when(prescriptionRepository.findById(1L))
                .thenReturn(Optional.of(prescription));
        when(prescription.getStatus()).thenReturn(PrescriptionStatus.ISSUED);

        assertThatThrownBy(() -> prescriptionService.removeItem(1L, 3L))
                .isInstanceOf(InvalidPrescriptionOperationException.class);

        verify(itemRepository, never())
                .findByIdAndPrescription_Id(3L, 1L);
        verify(itemRepository, never()).delete(any());
    }

    @Test
    void createPrescription_rejectsUnknownVisit() {
        when(visitRepository.findById(4L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> prescriptionService.createPrescription(
                4L, new CreatePrescriptionRequest(), 9L))
                .isInstanceOf(VisitNotFoundException.class);

        verify(prescriptionRepository, never()).save(any());
        verify(historyRepository, never()).save(any());
        verify(userAccountRepository, never()).findById(9L);
    }

    @Test
    void createPrescription_rejectsUnknownActor() {
        Visit visit = mock(Visit.class);

        when(visitRepository.findById(4L)).thenReturn(Optional.of(visit));
        when(userAccountRepository.findById(9L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> prescriptionService.createPrescription(
                4L, new CreatePrescriptionRequest(), 9L))
                .isInstanceOf(UserAccountNotFoundException.class);

        verify(prescriptionRepository, never()).save(any());
        verify(historyRepository, never()).save(any());
    }

    @Test
    void addItem_rejectsNonDraftPrescription() {
        Prescription prescription = mock(Prescription.class);

        when(prescriptionRepository.findById(1L))
                .thenReturn(Optional.of(prescription));
        when(prescription.getStatus()).thenReturn(PrescriptionStatus.ISSUED);

        AddPrescriptionItemRequest request = new AddPrescriptionItemRequest();
        request.setMedicationId(2L);

        assertThatThrownBy(() -> prescriptionService.addItem(1L, request))
                .isInstanceOf(InvalidPrescriptionOperationException.class);

        verify(medicationRepository, never()).findById(2L);
        verify(itemRepository, never()).save(any());
    }

    @Test
    void addItem_rejectsUnknownMedication() {
        Prescription prescription = mock(Prescription.class);

        when(prescriptionRepository.findById(1L))
                .thenReturn(Optional.of(prescription));
        when(prescription.getStatus()).thenReturn(PrescriptionStatus.DRAFT);
        when(medicationRepository.findById(2L)).thenReturn(Optional.empty());

        AddPrescriptionItemRequest request = new AddPrescriptionItemRequest();
        request.setMedicationId(2L);

        assertThatThrownBy(() -> prescriptionService.addItem(1L, request))
                .isInstanceOf(MedicationNotFoundException.class);

        verify(itemRepository, never()).save(any());
    }

    @Test
    void addItem_savesItemWithRequestedDetails() {
        Prescription prescription = mock(Prescription.class);
        Medication medication = mock(Medication.class);

        when(prescriptionRepository.findById(1L))
                .thenReturn(Optional.of(prescription));
        when(prescription.getStatus()).thenReturn(PrescriptionStatus.DRAFT);
        when(medicationRepository.findById(2L))
                .thenReturn(Optional.of(medication));
        when(medication.isActive()).thenReturn(true);
        when(medication.getId()).thenReturn(2L);
        when(medication.getGenericName()).thenReturn("Test medication");
        when(itemRepository.save(any(PrescriptionItem.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        AddPrescriptionItemRequest request = new AddPrescriptionItemRequest();
        request.setMedicationId(2L);
        request.setDosage("500 mg");
        request.setFrequency("Twice daily");
        request.setQuantityPrescribed(10);
        request.setDurationDays(5);
        request.setRoute("Oral");
        request.setInstructions("Take after meals");

        prescriptionService.addItem(1L, request);

        ArgumentCaptor<PrescriptionItem> captor =
                ArgumentCaptor.forClass(PrescriptionItem.class);
        verify(itemRepository).save(captor.capture());

        PrescriptionItem saved = captor.getValue();

        assertThat(saved.getMedication()).isSameAs(medication);
        assertThat(saved.getDosage()).isEqualTo("500 mg");
        assertThat(saved.getFrequency()).isEqualTo("Twice daily");
        assertThat(saved.getQuantityPrescribed()).isEqualTo(10);
        assertThat(saved.getDurationDays()).isEqualTo(5);
        assertThat(saved.getRoute()).isEqualTo("Oral");
        assertThat(saved.getInstructions()).isEqualTo("Take after meals");
    }

    @Test
    void updateItem_updatesExistingItem() {
        Prescription prescription = mock(Prescription.class);
        PrescriptionItem item = mock(PrescriptionItem.class);
        Medication medication = mock(Medication.class);

        when(prescriptionRepository.findById(1L))
                .thenReturn(Optional.of(prescription));
        when(prescription.getStatus()).thenReturn(PrescriptionStatus.DRAFT);
        when(itemRepository.findByIdAndPrescription_Id(3L, 1L))
                .thenReturn(Optional.of(item));
        when(medicationRepository.findById(2L))
                .thenReturn(Optional.of(medication));
        when(medication.isActive()).thenReturn(true);
        when(medication.getId()).thenReturn(2L);
        when(medication.getGenericName()).thenReturn("Test medication");
        when(item.getMedication()).thenReturn(medication);
        when(itemRepository.save(item)).thenReturn(item);

        AddPrescriptionItemRequest request = new AddPrescriptionItemRequest();
        request.setMedicationId(2L);
        request.setDosage("250 mg");
        request.setFrequency("Once daily");
        request.setQuantityPrescribed(5);
        request.setDurationDays(5);
        request.setRoute("Oral");
        request.setInstructions("After meals");

        prescriptionService.updateItem(1L, 3L, request);

        verify(item).setMedication(medication);
        verify(item).setDosage("250 mg");
        verify(item).setFrequency("Once daily");
        verify(item).setQuantityPrescribed(5);
        verify(item).setDurationDays(5);
        verify(item).setRoute("Oral");
        verify(item).setInstructions("After meals");
        verify(itemRepository).save(item);
    }

    @Test
    void updateItem_rejectsItemFromAnotherPrescription() {
        Prescription prescription = mock(Prescription.class);

        when(prescriptionRepository.findById(1L))
                .thenReturn(Optional.of(prescription));
        when(prescription.getStatus()).thenReturn(PrescriptionStatus.DRAFT);
        when(itemRepository.findByIdAndPrescription_Id(3L, 1L))
                .thenReturn(Optional.empty());

        AddPrescriptionItemRequest request = new AddPrescriptionItemRequest();
        request.setMedicationId(2L);

        assertThatThrownBy(() ->
                prescriptionService.updateItem(1L, 3L, request))
                .isInstanceOf(
                        com.coderaah.medtrack.prescription.exception
                                .PrescriptionItemNotFoundException.class);

        verify(medicationRepository, never()).findById(2L);
        verify(itemRepository, never()).save(any());
    }

    @Test
    void removeItem_deletesItemFromDraftPrescription() {
        Prescription prescription = mock(Prescription.class);
        PrescriptionItem item = mock(PrescriptionItem.class);

        when(prescriptionRepository.findById(1L))
                .thenReturn(Optional.of(prescription));
        when(prescription.getStatus()).thenReturn(PrescriptionStatus.DRAFT);
        when(itemRepository.findByIdAndPrescription_Id(3L, 1L))
                .thenReturn(Optional.of(item));

        prescriptionService.removeItem(1L, 3L);

        verify(itemRepository).delete(item);
    }

    @Test
    void cancel_draftPrescription_recordsCancellation() {
        Prescription prescription = mock(
                Prescription.class, RETURNS_DEEP_STUBS);
        UserAccount actor = mock(UserAccount.class);

        when(prescriptionRepository.findById(1L))
                .thenReturn(Optional.of(prescription));
        when(prescription.getStatus()).thenReturn(PrescriptionStatus.DRAFT);
        when(prescription.getId()).thenReturn(1L);
        when(prescription.getVisit().getId()).thenReturn(4L);
        when(prescription.getVisit().getPatient().getId()).thenReturn(7L);
        when(itemRepository.findByPrescription_Id(1L))
                .thenReturn(List.of());
        when(userAccountRepository.findById(9L))
                .thenReturn(Optional.of(actor));
        when(prescriptionRepository.save(prescription))
                .thenAnswer(invocation -> invocation.getArgument(0));

        CancelPrescriptionRequest request = new CancelPrescriptionRequest();
        request.setCancellationReason("Created in error");

        prescriptionService.cancel(1L, request, 9L);

        verify(prescription).setStatus(PrescriptionStatus.CANCELLED);
        verify(prescription).setCancelledAt(any());
        verify(prescription).setCancellationReason("Created in error");

        ArgumentCaptor<PrescriptionStatusHistory> captor =
                ArgumentCaptor.forClass(PrescriptionStatusHistory.class);
        verify(historyRepository).save(captor.capture());

        PrescriptionStatusHistory savedHistory = captor.getValue();

        assertThat(savedHistory.getOldStatus())
                .isEqualTo(PrescriptionStatus.DRAFT);
        assertThat(savedHistory.getNewStatus())
                .isEqualTo(PrescriptionStatus.CANCELLED);
        assertThat(savedHistory.getReason()).isEqualTo("Created in error");
        assertThat(savedHistory.getChangedByUser()).isSameAs(actor);
    }

    @Test
    void getHistory_returnsHistoryInChronologicalOrder() {
        Prescription prescription = mock(Prescription.class);
        PrescriptionStatusHistory first = mock(PrescriptionStatusHistory.class);
        PrescriptionStatusHistory second = mock(PrescriptionStatusHistory.class);
        UserAccount actor = mock(UserAccount.class);

        when(prescriptionRepository.findById(1L))
                .thenReturn(Optional.of(prescription));
        when(historyRepository.findByPrescription_IdOrderByChangedAtAsc(1L))
                .thenReturn(List.of(first, second));

        when(first.getId()).thenReturn(11L);
        when(first.getChangedByUser()).thenReturn(actor);
        when(first.getNewStatus()).thenReturn(PrescriptionStatus.DRAFT);

        when(second.getId()).thenReturn(12L);
        when(second.getChangedByUser()).thenReturn(actor);
        when(second.getNewStatus()).thenReturn(PrescriptionStatus.ISSUED);

        when(actor.getId()).thenReturn(9L);

        var result = prescriptionService.getHistory(1L);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getId()).isEqualTo(11L);
        assertThat(result.get(0).getNewStatus()).isEqualTo(PrescriptionStatus.DRAFT);
        assertThat(result.get(0).getChangedByUserId()).isEqualTo(9L);
        assertThat(result.get(1).getId()).isEqualTo(12L);
        assertThat(result.get(1).getNewStatus()).isEqualTo(PrescriptionStatus.ISSUED);
        assertThat(result.get(1).getChangedByUserId()).isEqualTo(9L);
    }

    @Test
    void getPrescriptionsForVisit_rejectsUnknownVisit() {
        when(visitRepository.existsById(4L)).thenReturn(false);

        assertThatThrownBy(() -> prescriptionService.getPrescriptionsForVisit(4L))
                .isInstanceOf(VisitNotFoundException.class);

        verify(prescriptionRepository, never()).findByVisit_Id(4L);
    }

    @Test
    void getPrescription_returnsExpiredStatus_whenExpirationDatePassed() {
        Prescription prescription = mock(Prescription.class, RETURNS_DEEP_STUBS);

        when(prescriptionRepository.findById(1L))
                .thenReturn(Optional.of(prescription));
        when(prescription.getId()).thenReturn(1L);
        when(prescription.getStatus()).thenReturn(PrescriptionStatus.ISSUED);
        when(prescription.getExpiresAt()).thenReturn(LocalDate.now().minusDays(1));
        when(prescription.getVisit().getId()).thenReturn(4L);
        when(prescription.getVisit().getPatient().getId()).thenReturn(7L);
        when(itemRepository.findByPrescription_Id(1L)).thenReturn(List.of());

        PrescriptionResponse response = prescriptionService.getPrescription(1L);

        assertThat(response.getStatus()).isEqualTo(PrescriptionStatus.EXPIRED);
    }

    @Test
    void updateItem_rejectsInactiveMedication() {
        Prescription prescription = mock(Prescription.class);
        PrescriptionItem item = mock(PrescriptionItem.class);
        Medication inactiveMedication = mock(Medication.class);

        when(prescriptionRepository.findById(1L))
                .thenReturn(Optional.of(prescription));
        when(prescription.getStatus()).thenReturn(PrescriptionStatus.DRAFT);

        when(itemRepository.findByIdAndPrescription_Id(10L, 1L))
                .thenReturn(Optional.of(item));

        when(medicationRepository.findById(2L))
                .thenReturn(Optional.of(inactiveMedication));
        when(inactiveMedication.isActive()).thenReturn(false);

        AddPrescriptionItemRequest request = new AddPrescriptionItemRequest();
        request.setMedicationId(2L);

        assertThatThrownBy(() ->
                prescriptionService.updateItem(1L, 10L, request))
                .isInstanceOf(InvalidPrescriptionOperationException.class);

        verify(itemRepository, never()).save(any());
        verify(item, never()).setMedication(any());
    }


    @Test
    void issue_rejectsMissingActor() {
        Prescription prescription = mock(Prescription.class);
        PrescriptionItem item = mock(PrescriptionItem.class);

        when(prescriptionRepository.findById(1L))
                .thenReturn(Optional.of(prescription));
        when(prescription.getStatus()).thenReturn(PrescriptionStatus.DRAFT);
        when(prescription.getExpiresAt()).thenReturn(null);
        when(itemRepository.findByPrescription_Id(1L))
                .thenReturn(List.of(item));
        when(userAccountRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> prescriptionService.issue(1L, 999L))
                .isInstanceOf(UserAccountNotFoundException.class);

        verify(prescription, never()).setStatus(any());
        verify(prescription, never()).setIssuedAt(any());

        verify(prescriptionRepository, never()).save(any());
        verify(historyRepository, never()).save(any());
    }

    @Test
    void cancel_rejectsExpiredPrescription() {
        Prescription prescription = mock(Prescription.class);

        when(prescriptionRepository.findById(1L))
                .thenReturn(Optional.of(prescription));
        when(prescription.getStatus()).thenReturn(PrescriptionStatus.ISSUED);
        when(prescription.getExpiresAt()).thenReturn(LocalDate.now().minusDays(1));

        CancelPrescriptionRequest request = new CancelPrescriptionRequest();
        request.setCancellationReason("Try cancel expired");

        assertThatThrownBy(() -> prescriptionService.cancel(1L, request, 9L))
                .isInstanceOf(InvalidPrescriptionOperationException.class);

        verify(prescriptionRepository, never()).save(any());
        verify(historyRepository, never()).save(any());
    }

}
