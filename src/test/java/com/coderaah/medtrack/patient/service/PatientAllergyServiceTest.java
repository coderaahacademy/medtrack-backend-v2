package com.coderaah.medtrack.patient.service;


import com.coderaah.medtrack.patient.domain.AllergySeverity;
import com.coderaah.medtrack.patient.domain.AllergyStatus;
import com.coderaah.medtrack.patient.domain.PatientAllergy;
import com.coderaah.medtrack.patient.domain.PatientProfile;
import com.coderaah.medtrack.patient.dto.AllergyRequest;
import com.coderaah.medtrack.patient.dto.AllergyResponse;
import com.coderaah.medtrack.patient.exception.AllergyNotFoundException;
import com.coderaah.medtrack.patient.exception.PatientNotFoundException;
import com.coderaah.medtrack.patient.repository.PatientAllergyRepository;
import com.coderaah.medtrack.patient.repository.PatientProfileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class PatientAllergyServiceTest {

    private PatientAllergyRepository patientAllergyRepository;
    private PatientProfileRepository patientProfileRepository;
    private PatientAllergyService patientAllergyService;

    @BeforeEach
    void setUp() {
        patientAllergyRepository = mock(PatientAllergyRepository.class);
        patientProfileRepository = mock(PatientProfileRepository.class);

        patientAllergyService = new PatientAllergyService(patientAllergyRepository, patientProfileRepository);
    }

    @Test
    void createAllergy_returnsCreatedAllergy() {

        PatientProfile patient = new PatientProfile(null, "MRN-001");
        patient.setId(1L);
        AllergyRequest request = new AllergyRequest();
        request.setAllergen("Penicillin");
        request.setReaction("Rash");
        request.setSeverity(AllergySeverity.MODERATE);
        request.setStatus(AllergyStatus.ACTIVE);

        when(patientProfileRepository.findById(1L))
                .thenReturn(Optional.of(patient));

        when(patientAllergyRepository.save(any(PatientAllergy.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        AllergyResponse result = patientAllergyService.createAllergy(1L, request);

        assertEquals("Penicillin", result.getAllergen());
        assertEquals("Rash", result.getReaction());
        assertEquals(AllergySeverity.MODERATE, result.getSeverity());
        assertEquals(AllergyStatus.ACTIVE, result.getStatus());

        verify(patientAllergyRepository).save(any(PatientAllergy.class));
    }

    @Test
    void createAllergy_throwsException_whenPatientNotFound() {

        AllergyRequest request = new AllergyRequest();
        request.setAllergen("Penicillin");

        when(patientProfileRepository.findById(999L))
                .thenReturn(Optional.empty());

        PatientNotFoundException exception = assertThrows(
                PatientNotFoundException.class,
                () -> patientAllergyService.createAllergy(999L, request)
        );

        assertEquals("Patient not found", exception.getMessage());
    }

    @Test
    void getAllergies_returnsAllergiesForPatient() {

        PatientProfile patient = new PatientProfile(null, "MRN-001");
        patient.setId(1L);

        PatientAllergy allergy1 = new PatientAllergy(patient, "Penicillin", AllergySeverity.MODERATE, AllergyStatus.ACTIVE);
        PatientAllergy allergy2 = new PatientAllergy(patient, "Peanuts", AllergySeverity.SEVERE, AllergyStatus.ACTIVE);

        when(patientProfileRepository.findById(1L))
                .thenReturn(Optional.of(patient));

        when(patientAllergyRepository.findByPatientId(1L))
                .thenReturn(List.of(allergy1, allergy2));

        List<AllergyResponse> result = patientAllergyService.getAllergies(1L, null);

        assertEquals(2, result.size());
        assertEquals("Penicillin", result.get(0).getAllergen());
        assertEquals("Peanuts", result.get(1).getAllergen());
    }

    @Test
    void getAllergies_returnsOnlyActiveAllergies_whenStatusFilterProvided() {

        PatientProfile patient = new PatientProfile(null, "MRN-001");
        patient.setId(1L);

        PatientAllergy activeAllergy = new PatientAllergy(patient, "Penicillin", AllergySeverity.MODERATE, AllergyStatus.ACTIVE);

        when(patientProfileRepository.findById(1L))
                .thenReturn(Optional.of(patient));

        when(patientAllergyRepository.findByPatientIdAndStatus(1L, AllergyStatus.ACTIVE))
                .thenReturn(List.of(activeAllergy));

        List<AllergyResponse> result = patientAllergyService.getAllergies(1L, AllergyStatus.ACTIVE);

        assertEquals(1, result.size());
        assertEquals(AllergyStatus.ACTIVE, result.get(0).getStatus());
    }

    @Test
    void getAllergies_throwsException_whenPatientNotFound() {

        when(patientProfileRepository.findById(999L))
                .thenReturn(Optional.empty());

        PatientNotFoundException exception = assertThrows(
                PatientNotFoundException.class,
                () -> patientAllergyService.getAllergies(999L, null)
        );

        assertEquals("Patient not found", exception.getMessage());
    }

    @Test
    void updateAllergy_returnsUpdatedAllergy() {

        PatientProfile patient = new PatientProfile(null, "MRN-001");
        patient.setId(1L);
        PatientAllergy allergy = new PatientAllergy(patient, "Penicillin", AllergySeverity.MILD, AllergyStatus.ACTIVE);

        AllergyRequest request = new AllergyRequest();
        request.setAllergen("Penicillin");
        request.setReaction("Severe rash");
        request.setSeverity(AllergySeverity.SEVERE);

        when(patientProfileRepository.findById(1L))
                .thenReturn(Optional.of(patient));

        when(patientAllergyRepository.findByIdAndPatientId(10L, 1L))
                .thenReturn(Optional.of(allergy));

        when(patientAllergyRepository.save(any(PatientAllergy.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        AllergyResponse result = patientAllergyService.updateAllergy(1L, 10L, request);

        assertEquals("Severe rash", result.getReaction());
        assertEquals(AllergySeverity.SEVERE, result.getSeverity());
    }

    @Test
    void updateAllergy_throwsException_whenAllergyDoesNotBelongToPatient() {

        PatientProfile patient = new PatientProfile(null, "MRN-001");
        patient.setId(1L);
        AllergyRequest request = new AllergyRequest();
        request.setAllergen("Penicillin");

        when(patientProfileRepository.findById(1L))
                .thenReturn(Optional.of(patient));

        when(patientAllergyRepository.findByIdAndPatientId(10L, 1L))
                .thenReturn(Optional.empty());

        AllergyNotFoundException exception = assertThrows(
                AllergyNotFoundException.class,
                () -> patientAllergyService.updateAllergy(1L, 10L, request)
        );

        assertEquals("Allergy not found", exception.getMessage());
    }

    @Test
    void changeAllergyStatus_updatesStatusWithoutDeletingRecord() {

        PatientProfile patient = new PatientProfile(null, "MRN-001");
        patient.setId(1L);
        PatientAllergy allergy = new PatientAllergy(patient, "Penicillin", AllergySeverity.MODERATE, AllergyStatus.ACTIVE);

        when(patientProfileRepository.findById(1L))
                .thenReturn(Optional.of(patient));

        when(patientAllergyRepository.findByIdAndPatientId(10L, 1L))
                .thenReturn(Optional.of(allergy));

        when(patientAllergyRepository.save(any(PatientAllergy.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        AllergyResponse result = patientAllergyService.changeAllergyStatus(1L, 10L, AllergyStatus.RESOLVED);

        assertEquals(AllergyStatus.RESOLVED, result.getStatus());

        verify(patientAllergyRepository).save(any(PatientAllergy.class));
        verify(patientAllergyRepository, never()).deleteById(any());
    }

    @Test
    void changeAllergyStatus_throwsException_whenAllergyNotFound() {

        PatientProfile patient = new PatientProfile(null, "MRN-001");
        patient.setId(1L);

        when(patientProfileRepository.findById(1L))
                .thenReturn(Optional.of(patient));

        when(patientAllergyRepository.findByIdAndPatientId(999L, 1L))
                .thenReturn(Optional.empty());

        AllergyNotFoundException exception = assertThrows(
                AllergyNotFoundException.class,
                () -> patientAllergyService.changeAllergyStatus(1L, 999L, AllergyStatus.RESOLVED)
        );

        assertEquals("Allergy not found", exception.getMessage());
    }
}
