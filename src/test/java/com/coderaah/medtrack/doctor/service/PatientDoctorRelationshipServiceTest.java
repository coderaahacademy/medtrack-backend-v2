package com.coderaah.medtrack.doctor.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.coderaah.medtrack.doctor.domain.DoctorProfile;
import com.coderaah.medtrack.doctor.domain.DoctorRelationshipType;
import com.coderaah.medtrack.doctor.domain.PatientDoctorRelationship;
import com.coderaah.medtrack.doctor.dto.CreatePatientDoctorRelationshipRequest;
import com.coderaah.medtrack.doctor.dto.PatientDoctorRelationshipResponse;
import com.coderaah.medtrack.doctor.exception.ActiveFamilyDoctorAlreadyExistsException;
import com.coderaah.medtrack.doctor.exception.DoctorNotFoundException;
import com.coderaah.medtrack.doctor.exception.PatientDoctorRelationshipNotFoundException;
import com.coderaah.medtrack.doctor.exception.RelationshipAlreadyEndedException;
import com.coderaah.medtrack.doctor.repository.DoctorProfileRepository;
import com.coderaah.medtrack.doctor.repository.PatientDoctorRelationshipRepository;
import com.coderaah.medtrack.patient.domain.PatientProfile;
import com.coderaah.medtrack.patient.exception.PatientNotFoundException;
import com.coderaah.medtrack.patient.repository.PatientProfileRepository;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PatientDoctorRelationshipServiceTest {

    private final PatientDoctorRelationshipRepository relationshipRepository = mock(PatientDoctorRelationshipRepository.class);
    private final PatientProfileRepository patientProfileRepository = mock(PatientProfileRepository.class);
    private final DoctorProfileRepository doctorProfileRepository = mock(DoctorProfileRepository.class);

    private PatientDoctorRelationshipService service;

    @BeforeEach
    void setUp() {
        service = new PatientDoctorRelationshipService(relationshipRepository, patientProfileRepository, doctorProfileRepository);
    }

    @Test
    void assign_rejectsSecondActiveFamilyDoctor() {
        when(patientProfileRepository.findById(1L)).thenReturn(Optional.of(patientWithId(1L)));
        when(doctorProfileRepository.findById(2L)).thenReturn(Optional.of(doctorWithId(2L)));
        when(relationshipRepository.findByPatient_IdAndRelationshipTypeAndActiveTrue(1L, DoctorRelationshipType.FAMILY_DOCTOR))
                .thenReturn(Optional.of(new PatientDoctorRelationship()));

        CreatePatientDoctorRelationshipRequest request =
                new CreatePatientDoctorRelationshipRequest(2L, DoctorRelationshipType.FAMILY_DOCTOR);

        assertThatThrownBy(() -> service.assign(1L, request)).isInstanceOf(ActiveFamilyDoctorAlreadyExistsException.class);
    }

    @Test
    void assign_allowsFamilyDoctorWhenNoneActive() {
        when(patientProfileRepository.findById(1L)).thenReturn(Optional.of(patientWithId(1L)));
        when(doctorProfileRepository.findById(2L)).thenReturn(Optional.of(doctorWithId(2L)));
        when(relationshipRepository.findByPatient_IdAndRelationshipTypeAndActiveTrue(1L, DoctorRelationshipType.FAMILY_DOCTOR))
                .thenReturn(Optional.empty());
        when(relationshipRepository.save(any(PatientDoctorRelationship.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PatientDoctorRelationshipResponse result =
                service.assign(1L, new CreatePatientDoctorRelationshipRequest(2L, DoctorRelationshipType.FAMILY_DOCTOR));

        assertThat(result.patientId()).isEqualTo(1L);
        assertThat(result.doctorId()).isEqualTo(2L);
        assertThat(result.active()).isTrue();
    }

    @Test
    void assign_allowsMultipleActiveSpecialistsForSamePatient() {
        when(patientProfileRepository.findById(1L)).thenReturn(Optional.of(patientWithId(1L)));
        when(doctorProfileRepository.findById(2L)).thenReturn(Optional.of(doctorWithId(2L)));
        when(relationshipRepository.save(any(PatientDoctorRelationship.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PatientDoctorRelationshipResponse result =
                service.assign(1L, new CreatePatientDoctorRelationshipRequest(2L, DoctorRelationshipType.SPECIALIST));

        assertThat(result.relationshipType()).isEqualTo(DoctorRelationshipType.SPECIALIST);
    }

    @Test
    void assign_throwsWhenPatientMissing() {
        when(patientProfileRepository.findById(1L)).thenReturn(Optional.empty());

        CreatePatientDoctorRelationshipRequest request =
                new CreatePatientDoctorRelationshipRequest(2L, DoctorRelationshipType.SPECIALIST);

        assertThatThrownBy(() -> service.assign(1L, request)).isInstanceOf(PatientNotFoundException.class);
    }

    @Test
    void end_deactivatesAndPreservesHistory() {
        PatientDoctorRelationship relationship =
                new PatientDoctorRelationship(patientWithId(1L), doctorWithId(2L), DoctorRelationshipType.FAMILY_DOCTOR, LocalDateTime.now());
        when(relationshipRepository.findById(10L)).thenReturn(Optional.of(relationship));
        when(relationshipRepository.save(any(PatientDoctorRelationship.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PatientDoctorRelationshipResponse result = service.end(10L);

        assertThat(result.active()).isFalse();
        assertThat(result.endedAt()).isNotNull();
        assertThat(result.relationshipType()).isEqualTo(DoctorRelationshipType.FAMILY_DOCTOR);
    }

    @Test
    void end_rejectsEndingAlreadyEndedRelationship() {
        PatientDoctorRelationship relationship =
                new PatientDoctorRelationship(patientWithId(1L), doctorWithId(2L), DoctorRelationshipType.FAMILY_DOCTOR, LocalDateTime.now());
        relationship.setActive(false);
        relationship.setEndedAt(LocalDateTime.now());
        when(relationshipRepository.findById(10L)).thenReturn(Optional.of(relationship));

        assertThatThrownBy(() -> service.end(10L)).isInstanceOf(RelationshipAlreadyEndedException.class);
    }

    @Test
    void end_throwsWhenRelationshipMissing() {
        when(relationshipRepository.findById(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.end(10L)).isInstanceOf(PatientDoctorRelationshipNotFoundException.class);
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