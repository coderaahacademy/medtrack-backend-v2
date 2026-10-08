package com.coderaah.medtrack.visit.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.coderaah.medtrack.doctor.domain.DoctorProfile;
import com.coderaah.medtrack.doctor.exception.DoctorNotFoundException;
import com.coderaah.medtrack.doctor.repository.DoctorProfileRepository;
import com.coderaah.medtrack.patient.domain.PatientProfile;
import com.coderaah.medtrack.patient.exception.PatientNotFoundException;
import com.coderaah.medtrack.patient.repository.PatientProfileRepository;
import com.coderaah.medtrack.visit.domain.Visit;
import com.coderaah.medtrack.visit.domain.VisitStatus;
import com.coderaah.medtrack.visit.dto.StartVisitRequest;
import com.coderaah.medtrack.visit.dto.UpdateClinicalNotesRequest;
import com.coderaah.medtrack.visit.dto.VisitResponse;
import com.coderaah.medtrack.visit.exception.InvalidVisitStatusException;
import com.coderaah.medtrack.visit.exception.VisitNotFoundException;
import com.coderaah.medtrack.visit.repository.VisitRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class VisitServiceTest {

    @Mock
    private VisitRepository visitRepository;

    @Mock
    private PatientProfileRepository patientProfileRepository;

    @Mock
    private DoctorProfileRepository doctorProfileRepository;

    @InjectMocks
    private VisitService visitService;

    private PatientProfile patient;
    private DoctorProfile doctor;

    @BeforeEach
    void setUp() {
        patient = mock(PatientProfile.class);
        doctor = mock(DoctorProfile.class);
        lenient().when(patient.getId()).thenReturn(1L);
        lenient().when(doctor.getId()).thenReturn(2L);
    }

    private Visit visitWithStatus(VisitStatus status) {
        Visit visit = new Visit(patient, doctor, LocalDateTime.now().minusMinutes(10), status);
        visit.setId(10L);
        return visit;
    }

    private UpdateClinicalNotesRequest notesRequest() {
        UpdateClinicalNotesRequest request = new UpdateClinicalNotesRequest();
        request.setSymptoms("Fever");
        request.setDiagnosis("Flu");
        request.setClinicalNotes("Rest and fluids");
        return request;
    }

    // ---------- startVisit ----------

    @Test
    void startVisit_validPatientAndDoctor_createsInProgressVisit() {
        StartVisitRequest request = new StartVisitRequest();
        request.setPatientId(1L);
        request.setDoctorId(2L);
        request.setSymptoms("Headache");

        when(patientProfileRepository.findById(1L)).thenReturn(Optional.of(patient));
        when(doctorProfileRepository.findById(2L)).thenReturn(Optional.of(doctor));
        when(visitRepository.save(any(Visit.class))).then(returnsFirstArg());

        VisitResponse response = visitService.startVisit(request);

        ArgumentCaptor<Visit> captor = ArgumentCaptor.forClass(Visit.class);
        verify(visitRepository).save(captor.capture());
        assertEquals(VisitStatus.IN_PROGRESS, captor.getValue().getStatus());
        assertNotNull(captor.getValue().getStartedAt());

        assertEquals(VisitStatus.IN_PROGRESS, response.getStatus());
        assertEquals(1L, response.getPatientId());
        assertEquals(2L, response.getDoctorId());
        assertEquals("Headache", response.getSymptoms());
        assertNull(response.getAppointmentId());
        assertNull(response.getEndedAt());
    }

    @Test
    void startVisit_patientNotFound_throwsAndSavesNothing() {
        StartVisitRequest request = new StartVisitRequest();
        request.setPatientId(1L);
        request.setDoctorId(2L);

        when(patientProfileRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(PatientNotFoundException.class, () -> visitService.startVisit(request));
        verify(visitRepository, never()).save(any());
    }

    @Test
    void startVisit_doctorNotFound_throwsAndSavesNothing() {
        StartVisitRequest request = new StartVisitRequest();
        request.setPatientId(1L);
        request.setDoctorId(2L);

        when(patientProfileRepository.findById(1L)).thenReturn(Optional.of(patient));
        when(doctorProfileRepository.findById(2L)).thenReturn(Optional.empty());

        assertThrows(DoctorNotFoundException.class, () -> visitService.startVisit(request));
        verify(visitRepository, never()).save(any());
    }

    // ---------- read ----------

    @Test
    void getVisitById_existing_returnsVisit() {
        when(visitRepository.findById(10L)).thenReturn(Optional.of(visitWithStatus(VisitStatus.IN_PROGRESS)));

        VisitResponse response = visitService.getVisitById(10L);

        assertEquals(10L, response.getId());
        assertEquals(VisitStatus.IN_PROGRESS, response.getStatus());
    }

    @Test
    void getVisitById_unknown_throwsVisitNotFound() {
        when(visitRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(VisitNotFoundException.class, () -> visitService.getVisitById(99L));
    }

    @Test
    void getVisitsByPatient_existingPatient_returnsVisits() {
        when(patientProfileRepository.existsById(1L)).thenReturn(true);
        when(visitRepository.findByPatient_IdOrderByStartedAtDesc(1L))
                .thenReturn(List.of(visitWithStatus(VisitStatus.COMPLETED), visitWithStatus(VisitStatus.IN_PROGRESS)));

        List<VisitResponse> result = visitService.getVisitsByPatient(1L);

        assertEquals(2, result.size());
    }

    @Test
    void getVisitsByPatient_unknownPatient_throwsPatientNotFound() {
        when(patientProfileRepository.existsById(1L)).thenReturn(false);

        assertThrows(PatientNotFoundException.class, () -> visitService.getVisitsByPatient(1L));
        verify(visitRepository, never()).findByPatient_IdOrderByStartedAtDesc(any());
    }

    @Test
    void getVisitsByDoctor_existingDoctor_returnsVisits() {
        when(doctorProfileRepository.existsById(2L)).thenReturn(true);
        when(visitRepository.findByDoctor_IdOrderByStartedAtDesc(2L))
                .thenReturn(List.of(visitWithStatus(VisitStatus.COMPLETED)));

        List<VisitResponse> result = visitService.getVisitsByDoctor(2L);

        assertEquals(1, result.size());
    }

    @Test
    void getVisitsByDoctor_unknownDoctor_throwsDoctorNotFound() {
        when(doctorProfileRepository.existsById(2L)).thenReturn(false);

        assertThrows(DoctorNotFoundException.class, () -> visitService.getVisitsByDoctor(2L));
        verify(visitRepository, never()).findByDoctor_IdOrderByStartedAtDesc(any());
    }

    // ---------- updateClinicalNotes ----------

    @Test
    void updateClinicalNotes_inProgress_updatesFields() {
        when(visitRepository.findById(10L)).thenReturn(Optional.of(visitWithStatus(VisitStatus.IN_PROGRESS)));
        when(visitRepository.save(any(Visit.class))).then(returnsFirstArg());

        VisitResponse response = visitService.updateClinicalNotes(10L, notesRequest());

        assertEquals("Fever", response.getSymptoms());
        assertEquals("Flu", response.getDiagnosis());
        assertEquals("Rest and fluids", response.getClinicalNotes());
    }

    @ParameterizedTest
    @EnumSource(value = VisitStatus.class, names = {"COMPLETED", "CANCELLED"})
    void updateClinicalNotes_notInProgress_isRejected(VisitStatus status) {
        when(visitRepository.findById(10L)).thenReturn(Optional.of(visitWithStatus(status)));

        assertThrows(InvalidVisitStatusException.class,
                () -> visitService.updateClinicalNotes(10L, notesRequest()));
        verify(visitRepository, never()).save(any());
    }

    @Test
    void updateClinicalNotes_unknownVisit_throwsVisitNotFound() {
        when(visitRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(VisitNotFoundException.class,
                () -> visitService.updateClinicalNotes(99L, notesRequest()));
    }

    // ---------- completeVisit ----------

    @Test
    void completeVisit_inProgress_setsCompletedAndEndedAt() {
        Visit visit = visitWithStatus(VisitStatus.IN_PROGRESS);
        when(visitRepository.findById(10L)).thenReturn(Optional.of(visit));
        when(visitRepository.save(any(Visit.class))).then(returnsFirstArg());

        VisitResponse response = visitService.completeVisit(10L);

        assertEquals(VisitStatus.COMPLETED, response.getStatus());
        assertNotNull(response.getEndedAt());
        assertEquals(false, response.getEndedAt().isBefore(response.getStartedAt()));
    }

    @ParameterizedTest
    @EnumSource(value = VisitStatus.class, names = {"COMPLETED", "CANCELLED"})
    void completeVisit_notInProgress_isRejected(VisitStatus status) {
        when(visitRepository.findById(10L)).thenReturn(Optional.of(visitWithStatus(status)));

        assertThrows(InvalidVisitStatusException.class, () -> visitService.completeVisit(10L));
        verify(visitRepository, never()).save(any());
    }

    @Test
    void completeVisit_startedAtInFuture_isRejected() {
        Visit visit = visitWithStatus(VisitStatus.IN_PROGRESS);
        visit.setStartedAt(LocalDateTime.now().plusHours(1));
        when(visitRepository.findById(10L)).thenReturn(Optional.of(visit));

        assertThrows(InvalidVisitStatusException.class, () -> visitService.completeVisit(10L));
        verify(visitRepository, never()).save(any());
    }

    // ---------- cancelVisit ----------

    @Test
    void cancelVisit_inProgress_setsCancelled() {
        when(visitRepository.findById(10L)).thenReturn(Optional.of(visitWithStatus(VisitStatus.IN_PROGRESS)));
        when(visitRepository.save(any(Visit.class))).then(returnsFirstArg());

        VisitResponse response = visitService.cancelVisit(10L);

        assertEquals(VisitStatus.CANCELLED, response.getStatus());
    }

    @ParameterizedTest
    @EnumSource(value = VisitStatus.class, names = {"COMPLETED", "CANCELLED"})
    void cancelVisit_notInProgress_isRejected(VisitStatus status) {
        when(visitRepository.findById(10L)).thenReturn(Optional.of(visitWithStatus(status)));

        assertThrows(InvalidVisitStatusException.class, () -> visitService.cancelVisit(10L));
        verify(visitRepository, never()).save(any());
    }
}