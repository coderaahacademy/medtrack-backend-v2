package com.coderaah.medtrack.doctor.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.coderaah.medtrack.common.exception.ConflictException;
import com.coderaah.medtrack.common.exception.ResourceNotFoundException;
import com.coderaah.medtrack.doctor.domain.DoctorProfile;
import com.coderaah.medtrack.doctor.domain.DoctorRelationshipType;
import com.coderaah.medtrack.doctor.domain.PatientDoctorRelationship;
import com.coderaah.medtrack.doctor.dto.CreatePatientDoctorRelationshipRequest;
import com.coderaah.medtrack.doctor.repository.PatientDoctorRelationshipRepository;
import com.coderaah.medtrack.patient.domain.PatientProfile;
import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PatientDoctorRelationshipServiceTest {

    private final PatientDoctorRelationshipRepository relationshipRepository = mock(PatientDoctorRelationshipRepository.class);
    private final EntityManager entityManager = mock(EntityManager.class);

    private PatientDoctorRelationshipService service;

    @BeforeEach
    void setUp() {
        service = new PatientDoctorRelationshipService(relationshipRepository, entityManager);
    }

    @Test
    void assign_rejectsSecondActiveFamilyDoctor() {
        when(entityManager.find(PatientProfile.class, 1L)).thenReturn(patientWithId(1L));
        when(entityManager.find(DoctorProfile.class, 2L)).thenReturn(doctorWithId(2L));
        when(relationshipRepository.findByPatient_IdAndRelationshipTypeAndActiveTrue(1L, DoctorRelationshipType.FAMILY_DOCTOR))
                .thenReturn(Optional.of(new PatientDoctorRelationship()));

        CreatePatientDoctorRelationshipRequest request =
                new CreatePatientDoctorRelationshipRequest(2L, DoctorRelationshipType.FAMILY_DOCTOR);

        assertThatThrownBy(() -> service.assign(1L, request)).isInstanceOf(ConflictException.class);
    }

    @Test
    void assign_allowsFamilyDoctorWhenNoneActive() {
        PatientProfile patient = patientWithId(1L);
        DoctorProfile doctor = doctorWithId(2L);
        when(entityManager.find(PatientProfile.class, 1L)).thenReturn(patient);
        when(entityManager.find(DoctorProfile.class, 2L)).thenReturn(doctor);
        when(relationshipRepository.findByPatient_IdAndRelationshipTypeAndActiveTrue(1L, DoctorRelationshipType.FAMILY_DOCTOR))
                .thenReturn(Optional.empty());
        when(relationshipRepository.save(any(PatientDoctorRelationship.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PatientDoctorRelationship result =
                service.assign(1L, new CreatePatientDoctorRelationshipRequest(2L, DoctorRelationshipType.FAMILY_DOCTOR));

        assertThat(result.getPatient()).isEqualTo(patient);
        assertThat(result.getDoctor()).isEqualTo(doctor);
        assertThat(result.isActive()).isTrue();
    }

    @Test
    void assign_allowsMultipleActiveSpecialistsForSamePatient() {
        when(entityManager.find(PatientProfile.class, 1L)).thenReturn(patientWithId(1L));
        when(entityManager.find(DoctorProfile.class, 2L)).thenReturn(doctorWithId(2L));
        when(relationshipRepository.save(any(PatientDoctorRelationship.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PatientDoctorRelationship result =
                service.assign(1L, new CreatePatientDoctorRelationshipRequest(2L, DoctorRelationshipType.SPECIALIST));

        assertThat(result.getRelationshipType()).isEqualTo(DoctorRelationshipType.SPECIALIST);
    }

    @Test
    void assign_throwsWhenPatientMissing() {
        when(entityManager.find(PatientProfile.class, 1L)).thenReturn(null);

        CreatePatientDoctorRelationshipRequest request =
                new CreatePatientDoctorRelationshipRequest(2L, DoctorRelationshipType.SPECIALIST);

        assertThatThrownBy(() -> service.assign(1L, request)).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void end_deactivatesAndPreservesHistory() {
        PatientDoctorRelationship relationship =
                new PatientDoctorRelationship(patientWithId(1L), doctorWithId(2L), DoctorRelationshipType.FAMILY_DOCTOR, LocalDateTime.now());
        when(relationshipRepository.findById(10L)).thenReturn(Optional.of(relationship));
        when(relationshipRepository.save(any(PatientDoctorRelationship.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PatientDoctorRelationship result = service.end(10L);

        assertThat(result.isActive()).isFalse();
        assertThat(result.getEndedAt()).isNotNull();
        assertThat(result.getRelationshipType()).isEqualTo(DoctorRelationshipType.FAMILY_DOCTOR);
    }

    @Test
    void end_rejectsEndingAlreadyEndedRelationship() {
        PatientDoctorRelationship relationship =
                new PatientDoctorRelationship(patientWithId(1L), doctorWithId(2L), DoctorRelationshipType.FAMILY_DOCTOR, LocalDateTime.now());
        relationship.setActive(false);
        relationship.setEndedAt(LocalDateTime.now());
        when(relationshipRepository.findById(10L)).thenReturn(Optional.of(relationship));

        assertThatThrownBy(() -> service.end(10L)).isInstanceOf(ConflictException.class);
    }

    @Test
    void end_throwsWhenRelationshipMissing() {
        when(relationshipRepository.findById(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.end(10L)).isInstanceOf(ResourceNotFoundException.class);
    }

    private PatientProfile patientWithId(Long id) {
        PatientProfile patient = new PatientProfile();
        patient.setId(id);
        return patient;
    }

    private DoctorProfile doctorWithId(Long id) {
        DoctorProfile doctor = new DoctorProfile();
        doctor.setId(id);
        return doctor;
    }
}