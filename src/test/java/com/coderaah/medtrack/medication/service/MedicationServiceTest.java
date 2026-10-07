package com.coderaah.medtrack.medication.service;

import com.coderaah.medtrack.medication.domain.DosageForm;
import com.coderaah.medtrack.medication.domain.Medication;
import com.coderaah.medtrack.medication.dto.MedicationRequest;
import com.coderaah.medtrack.medication.dto.MedicationResponse;
import com.coderaah.medtrack.medication.exception.MedicationNotFoundException;
import com.coderaah.medtrack.medication.repository.MedicationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class MedicationServiceTest {

    private MedicationRepository medicationRepository;
    private MedicationService medicationService;

    @BeforeEach
    void setUp() {
        medicationRepository = mock(MedicationRepository.class);
        medicationService = new MedicationService(medicationRepository);
    }

    @Test
    void createMedication_returnsCreatedMedication() {

        MedicationRequest request = new MedicationRequest();
        request.setGenericName("Amoxicillin");
        request.setBrandName("Amoxil");
        request.setStrength("500");
        request.setStrengthUnit("mg");
        request.setDosageForm(DosageForm.CAPSULE);
        request.setManufacturer("Pfizer");

        when(medicationRepository.save(any(Medication.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        MedicationResponse result = medicationService.createMedication(request);

        assertEquals("Amoxicillin", result.getGenericName());
        assertEquals("Amoxil", result.getBrandName());
        assertEquals("500", result.getStrength());
        assertEquals("mg", result.getStrengthUnit());
        assertEquals(DosageForm.CAPSULE, result.getDosageForm());
        assertEquals("Pfizer", result.getManufacturer());
        assertTrue(result.isActive());

        verify(medicationRepository).save(any(Medication.class));
    }

    @Test
    void getMedicationById_returnsMedication() {

        Medication medication = new Medication(
                "Ibuprofen", "Advil", "200", "mg", DosageForm.TABLET, "Bayer"
        );

        when(medicationRepository.findById(1L))
                .thenReturn(Optional.of(medication));

        MedicationResponse result = medicationService.getMedicationById(1L);

        assertEquals("Ibuprofen", result.getGenericName());
        assertEquals("Advil", result.getBrandName());
    }

    @Test
    void getMedicationById_throwsException_whenNotFound() {

        when(medicationRepository.findById(999L))
                .thenReturn(Optional.empty());

        MedicationNotFoundException exception = assertThrows(
                MedicationNotFoundException.class,
                () -> medicationService.getMedicationById(999L)
        );

        assertEquals("Medication not found", exception.getMessage());
    }

    @Test
    void listMedications_returnsAll_whenNoFiltersGiven() {

        Medication medication1 = new Medication("Ibuprofen", "200", "mg", DosageForm.TABLET);
        Medication medication2 = new Medication("Paracetamol", "500", "mg", DosageForm.TABLET);

        when(medicationRepository.findAll())
                .thenReturn(List.of(medication1, medication2));

        List<MedicationResponse> result = medicationService.listMedications(null, null);

        assertEquals(2, result.size());
        assertEquals("Ibuprofen", result.get(0).getGenericName());
        assertEquals("Paracetamol", result.get(1).getGenericName());
    }

    @Test
    void listMedications_filtersByActive() {

        Medication activeMedication = new Medication("Ibuprofen", "200", "mg", DosageForm.TABLET);

        when(medicationRepository.findByActive(true))
                .thenReturn(List.of(activeMedication));

        List<MedicationResponse> result = medicationService.listMedications(true, null);

        assertEquals(1, result.size());
        assertTrue(result.get(0).isActive());

        verify(medicationRepository).findByActive(true);
        verify(medicationRepository, never()).findAll();
    }

    @Test
    void listMedications_filtersBySearch() {

        Medication medication = new Medication("Amoxicillin", "500", "mg", DosageForm.CAPSULE);

        when(medicationRepository.findByGenericNameContainingIgnoreCaseOrBrandNameContainingIgnoreCase("amox", "amox"))
                .thenReturn(List.of(medication));

        List<MedicationResponse> result = medicationService.listMedications(null, "amox");

        assertEquals(1, result.size());
        assertEquals("Amoxicillin", result.get(0).getGenericName());
    }

    @Test
    void updateMedication_returnsUpdatedMedication() {

        Medication medication = new Medication("Ibuprofen", "200", "mg", DosageForm.TABLET);

        MedicationRequest updateRequest = new MedicationRequest();
        updateRequest.setGenericName("Ibuprofen");
        updateRequest.setBrandName("Nurofen");
        updateRequest.setStrength("400");
        updateRequest.setStrengthUnit("mg");
        updateRequest.setDosageForm(DosageForm.TABLET);
        updateRequest.setManufacturer("Reckitt");

        when(medicationRepository.findById(1L))
                .thenReturn(Optional.of(medication));

        when(medicationRepository.save(any(Medication.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        MedicationResponse result = medicationService.updateMedication(1L, updateRequest);

        assertEquals("Nurofen", result.getBrandName());
        assertEquals("400", result.getStrength());
        assertEquals("Reckitt", result.getManufacturer());

        verify(medicationRepository).save(any(Medication.class));
    }

    @Test
    void updateMedication_throwsException_whenNotFound() {

        MedicationRequest updateRequest = new MedicationRequest();
        updateRequest.setGenericName("Ibuprofen");
        updateRequest.setStrength("200");
        updateRequest.setStrengthUnit("mg");
        updateRequest.setDosageForm(DosageForm.TABLET);

        when(medicationRepository.findById(999L))
                .thenReturn(Optional.empty());

        MedicationNotFoundException exception = assertThrows(
                MedicationNotFoundException.class,
                () -> medicationService.updateMedication(999L, updateRequest)
        );

        assertEquals("Medication not found", exception.getMessage());
    }

    @Test
    void updateStatus_deactivatesMedication() {

        Medication medication = new Medication("Ibuprofen", "200", "mg", DosageForm.TABLET);

        when(medicationRepository.findById(1L))
                .thenReturn(Optional.of(medication));

        when(medicationRepository.save(any(Medication.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        MedicationResponse result = medicationService.updateStatus(1L, false);

        assertFalse(result.isActive());

        verify(medicationRepository).save(any(Medication.class));
    }

    @Test
    void updateStatus_activatesMedication() {

        Medication medication = new Medication("Ibuprofen", "200", "mg", DosageForm.TABLET);
        medication.setActive(false);

        when(medicationRepository.findById(1L))
                .thenReturn(Optional.of(medication));

        when(medicationRepository.save(any(Medication.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        MedicationResponse result = medicationService.updateStatus(1L, true);

        assertTrue(result.isActive());
    }

    @Test
    void updateStatus_throwsException_whenNotFound() {

        when(medicationRepository.findById(999L))
                .thenReturn(Optional.empty());

        MedicationNotFoundException exception = assertThrows(
                MedicationNotFoundException.class,
                () -> medicationService.updateStatus(999L, false)
        );

        assertEquals("Medication not found", exception.getMessage());
    }
}
