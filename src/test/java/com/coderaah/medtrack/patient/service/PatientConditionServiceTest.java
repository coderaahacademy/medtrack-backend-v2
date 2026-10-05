package com.coderaah.medtrack.patient.service;


import com.coderaah.medtrack.patient.domain.PatientCondition;
import com.coderaah.medtrack.patient.domain.PatientConditionStatus;
import com.coderaah.medtrack.patient.domain.PatientProfile;
import com.coderaah.medtrack.patient.dto.ConditionRequest;
import com.coderaah.medtrack.patient.dto.ConditionResponse;
import com.coderaah.medtrack.patient.exception.ConditionNotFoundException;
import com.coderaah.medtrack.patient.exception.PatientNotFoundException;
import com.coderaah.medtrack.patient.repository.PatientConditionRepository;
import com.coderaah.medtrack.patient.repository.PatientProfileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class PatientConditionServiceTest {

    private PatientConditionRepository patientConditionRepository;
    private PatientProfileRepository patientProfileRepository;
    private PatientConditionService patientConditionService;

    @BeforeEach
    void setUp() {
        patientConditionRepository = mock(PatientConditionRepository.class);
        patientProfileRepository = mock(PatientProfileRepository.class);

        patientConditionService = new PatientConditionService(patientConditionRepository, patientProfileRepository);
    }

    @Test
    void createCondition_returnsCreatedCondition() {

        PatientProfile patient = new PatientProfile(null, "MRN-001");

        ConditionRequest request = new ConditionRequest();
        request.setConditionName("Diabetes");
        request.setConditionCode("E11");
        request.setStatus(PatientConditionStatus.ACTIVE);

        when(patientProfileRepository.findById(1L))
                .thenReturn(Optional.of(patient));

        when(patientConditionRepository.save(any(PatientCondition.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ConditionResponse result = patientConditionService.createCondition(1L, request);

        assertEquals("Diabetes", result.getConditionName());
        assertEquals("E11", result.getConditionCode());
        assertEquals(PatientConditionStatus.ACTIVE, result.getStatus());

        verify(patientConditionRepository).save(any(PatientCondition.class));
    }

    @Test
    void createCondition_throwsException_whenPatientNotFound() {

        ConditionRequest request = new ConditionRequest();
        request.setConditionName("Diabetes");

        when(patientProfileRepository.findById(999L))
                .thenReturn(Optional.empty());

        PatientNotFoundException exception = assertThrows(
                PatientNotFoundException.class,
                () -> patientConditionService.createCondition(999L, request)
        );

        assertEquals("Patient not found", exception.getMessage());
    }

    @Test
    void getConditions_returnsConditionsForPatient() {

        PatientProfile patient = new PatientProfile(null, "MRN-001");

        PatientCondition condition1 = new PatientCondition(patient, "Diabetes", PatientConditionStatus.ACTIVE);
        PatientCondition condition2 = new PatientCondition(patient, "Asthma", PatientConditionStatus.ACTIVE);

        when(patientProfileRepository.findById(1L))
                .thenReturn(Optional.of(patient));

        when(patientConditionRepository.findByPatientId(1L))
                .thenReturn(List.of(condition1, condition2));

        List<ConditionResponse> result = patientConditionService.getConditions(1L, null);

        assertEquals(2, result.size());
        assertEquals("Diabetes", result.get(0).getConditionName());
        assertEquals("Asthma", result.get(1).getConditionName());
    }

    @Test
    void getConditions_returnsOnlyActiveConditions_whenStatusFilterProvided() {

        PatientProfile patient = new PatientProfile(null, "MRN-001");

        PatientCondition activeCondition = new PatientCondition(patient, "Diabetes", PatientConditionStatus.ACTIVE);

        when(patientProfileRepository.findById(1L))
                .thenReturn(Optional.of(patient));

        when(patientConditionRepository.findByPatientIdAndStatus(1L, PatientConditionStatus.ACTIVE))
                .thenReturn(List.of(activeCondition));

        List<ConditionResponse> result = patientConditionService.getConditions(1L, PatientConditionStatus.ACTIVE);

        assertEquals(1, result.size());
        assertEquals(PatientConditionStatus.ACTIVE, result.get(0).getStatus());
    }

    @Test
    void getConditions_throwsException_whenPatientNotFound() {

        when(patientProfileRepository.findById(999L))
                .thenReturn(Optional.empty());

        PatientNotFoundException exception = assertThrows(
                PatientNotFoundException.class,
                () -> patientConditionService.getConditions(999L, null)
        );

        assertEquals("Patient not found", exception.getMessage());
    }

    @Test
    void updateCondition_returnsUpdatedCondition() {

        PatientProfile patient = new PatientProfile(null, "MRN-001");
        PatientCondition condition = new PatientCondition(patient, "Diabetes", PatientConditionStatus.ACTIVE);

        ConditionRequest request = new ConditionRequest();
        request.setConditionName("Diabetes");
        request.setNotes("Type 2, controlled with medication");

        when(patientProfileRepository.findById(1L))
                .thenReturn(Optional.of(patient));

        when(patientConditionRepository.findByIdAndPatientId(10L, 1L))
                .thenReturn(Optional.of(condition));

        when(patientConditionRepository.save(any(PatientCondition.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ConditionResponse result = patientConditionService.updateCondition(1L, 10L, request);

        assertEquals("Type 2, controlled with medication", result.getNotes());
    }

    @Test
    void updateCondition_throwsException_whenConditionDoesNotBelongToPatient() {

        PatientProfile patient = new PatientProfile(null, "MRN-001");

        ConditionRequest request = new ConditionRequest();
        request.setConditionName("Diabetes");

        when(patientProfileRepository.findById(1L))
                .thenReturn(Optional.of(patient));

        when(patientConditionRepository.findByIdAndPatientId(10L, 1L))
                .thenReturn(Optional.empty());

        ConditionNotFoundException exception = assertThrows(
                ConditionNotFoundException.class,
                () -> patientConditionService.updateCondition(1L, 10L, request)
        );

        assertEquals("Condition not found", exception.getMessage());
    }

    @Test
    void changeConditionStatus_updatesStatusWithoutDeletingRecord() {

        PatientProfile patient = new PatientProfile(null, "MRN-001");
        PatientCondition condition = new PatientCondition(patient, "Diabetes", PatientConditionStatus.ACTIVE);

        when(patientProfileRepository.findById(1L))
                .thenReturn(Optional.of(patient));

        when(patientConditionRepository.findByIdAndPatientId(10L, 1L))
                .thenReturn(Optional.of(condition));

        when(patientConditionRepository.save(any(PatientCondition.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ConditionResponse result = patientConditionService.changeConditionStatus(1L, 10L, PatientConditionStatus.RESOLVED);

        assertEquals(PatientConditionStatus.RESOLVED, result.getStatus());

        verify(patientConditionRepository).save(any(PatientCondition.class));
        verify(patientConditionRepository, never()).deleteById(any());
    }
    @Test
    void changeConditionStatus_throwsException_whenConditionNotFound(){
        PatientProfile patient=new PatientProfile(null,"MRN-001");
        when(patientProfileRepository.findById(1L))
                .thenReturn(Optional.of(patient));

        when(patientConditionRepository.findByIdAndPatientId(999L, 1L))
                .thenReturn(Optional.empty());

        ConditionNotFoundException exception = assertThrows(
                ConditionNotFoundException.class,
                () -> patientConditionService.changeConditionStatus(1L, 999L, PatientConditionStatus.RESOLVED));

        assertEquals("Condition not found", exception.getMessage());
    }
    }

