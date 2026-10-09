package com.coderaah.medtrack.visit.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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

import com.coderaah.medtrack.appointment.domain.Appointment;
import com.coderaah.medtrack.appointment.domain.AppointmentStatus;
import com.coderaah.medtrack.appointment.exception.AppointmentNotFoundException;
import com.coderaah.medtrack.appointment.repository.AppointmentRepository;
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
import com.coderaah.medtrack.visit.exception.InvalidAppointmentForVisitException;
import com.coderaah.medtrack.visit.exception.InvalidVisitStatusException;
import com.coderaah.medtrack.visit.exception.VisitAlreadyExistsException;
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
import org.springframework.dao.DataIntegrityViolationException;

@ExtendWith(MockitoExtension.class)
class VisitServiceTest {

    @Mock
    private VisitRepository visitRepository;

    @Mock
    private PatientProfileRepository patientProfileRepository;

    @Mock
    private DoctorProfileRepository doctorProfileRepository;

    @Mock
    private AppointmentRepository appointmentRepository;

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

    private Appointment appointment(AppointmentStatus status, PatientProfile p, DoctorProfile d) {
        Appointment appointment = mock(Appointment.class);
        lenient().when(appointment.getId()).thenReturn(5L);
        lenient().when(appointment.getStatus()).thenReturn(status);
        lenient().when(appointment.getPatient()).thenReturn(p);
        lenient().when(appointment.getDoctor()).thenReturn(d);
        return appointment;
    }


    private void stubAppointment(AppointmentStatus status, PatientProfile p, DoctorProfile d) {
        Appointment appt = appointment(status, p, d);
        when(appointmentRepository.findById(5L)).thenReturn(Optional.of(appt));
    }

    private StartVisitRequest startRequest(Long appointmentId) {
        StartVisitRequest request = new StartVisitRequest();
        request.setPatientId(1L);
        request.setDoctorId(2L);
        request.setAppointmentId(appointmentId);
        request.setSymptoms("Headache");
        return request;
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
        when(patientProfileRepository.findById(1L)).thenReturn(Optional.of(patient));
        when(doctorProfileRepository.findById(2L)).thenReturn(Optional.of(doctor));
        when(visitRepository.saveAndFlush(any(Visit.class))).then(returnsFirstArg());

        VisitResponse response = visitService.startVisit(startRequest(null));

        ArgumentCaptor<Visit> captor = ArgumentCaptor.forClass(Visit.class);
        verify(visitRepository).saveAndFlush(captor.capture());
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
        when(patientProfileRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(PatientNotFoundException.class, () -> visitService.startVisit(startRequest(null)));
        verify(visitRepository, never()).saveAndFlush(any());
    }

    @Test
    void startVisit_doctorNotFound_throwsAndSavesNothing() {
        when(patientProfileRepository.findById(1L)).thenReturn(Optional.of(patient));
        when(doctorProfileRepository.findById(2L)).thenReturn(Optional.empty());

        assertThrows(DoctorNotFoundException.class, () -> visitService.startVisit(startRequest(null)));
        verify(visitRepository, never()).saveAndFlush(any());
    }

    // ---------- startVisit mit Appointment ----------

    @Test
    void startVisit_withMatchingAppointment_linksAppointment() {
        when(patientProfileRepository.findById(1L)).thenReturn(Optional.of(patient));
        when(doctorProfileRepository.findById(2L)).thenReturn(Optional.of(doctor));
        stubAppointment(AppointmentStatus.CONFIRMED, patient, doctor);
        when(visitRepository.existsByAppointment_Id(5L)).thenReturn(false);
        when(visitRepository.saveAndFlush(any(Visit.class))).then(returnsFirstArg());

        VisitResponse response = visitService.startVisit(startRequest(5L));

        assertEquals(5L, response.getAppointmentId());
        assertEquals(VisitStatus.IN_PROGRESS, response.getStatus());
    }

    @Test
    void startVisit_appointmentNotFound_throws() {
        when(patientProfileRepository.findById(1L)).thenReturn(Optional.of(patient));
        when(doctorProfileRepository.findById(2L)).thenReturn(Optional.of(doctor));
        when(appointmentRepository.findById(5L)).thenReturn(Optional.empty());

        assertThrows(AppointmentNotFoundException.class, () -> visitService.startVisit(startRequest(5L)));
        verify(visitRepository, never()).saveAndFlush(any());
    }

    @Test
    void startVisit_appointmentOfDifferentPatient_isRejected() {
        PatientProfile otherPatient = mock(PatientProfile.class);
        lenient().when(otherPatient.getId()).thenReturn(3L);

        when(patientProfileRepository.findById(1L)).thenReturn(Optional.of(patient));
        when(doctorProfileRepository.findById(2L)).thenReturn(Optional.of(doctor));
        stubAppointment(AppointmentStatus.SCHEDULED, otherPatient, doctor);

        assertThrows(InvalidAppointmentForVisitException.class, () -> visitService.startVisit(startRequest(5L)));
        verify(visitRepository, never()).saveAndFlush(any());
    }

    @Test
    void startVisit_appointmentOfDifferentDoctor_isRejected() {
        DoctorProfile otherDoctor = mock(DoctorProfile.class);
        lenient().when(otherDoctor.getId()).thenReturn(4L);

        when(patientProfileRepository.findById(1L)).thenReturn(Optional.of(patient));
        when(doctorProfileRepository.findById(2L)).thenReturn(Optional.of(doctor));
        stubAppointment(AppointmentStatus.SCHEDULED, patient, otherDoctor);

        assertThrows(InvalidAppointmentForVisitException.class, () -> visitService.startVisit(startRequest(5L)));
        verify(visitRepository, never()).saveAndFlush(any());
    }

    @ParameterizedTest
    @EnumSource(value = AppointmentStatus.class, names = {"CANCELLED", "NO_SHOW"})
    void startVisit_cancelledOrNoShowAppointment_isRejected(AppointmentStatus status) {
        when(patientProfileRepository.findById(1L)).thenReturn(Optional.of(patient));
        when(doctorProfileRepository.findById(2L)).thenReturn(Optional.of(doctor));
        stubAppointment(status, patient, doctor);

        assertThrows(InvalidAppointmentForVisitException.class, () -> visitService.startVisit(startRequest(5L)));
        verify(visitRepository, never()).saveAndFlush(any());
    }

    @Test
    void startVisit_appointmentAlreadyHasVisit_isRejected() {
        when(patientProfileRepository.findById(1L)).thenReturn(Optional.of(patient));
        when(doctorProfileRepository.findById(2L)).thenReturn(Optional.of(doctor));
        stubAppointment(AppointmentStatus.CONFIRMED, patient, doctor);
        when(visitRepository.existsByAppointment_Id(5L)).thenReturn(true);

        assertThrows(VisitAlreadyExistsException.class, () -> visitService.startVisit(startRequest(5L)));
        verify(visitRepository, never()).saveAndFlush(any());
    }

    @Test
    void startVisit_concurrentDuplicate_isTranslatedToVisitAlreadyExists() {
        when(patientProfileRepository.findById(1L)).thenReturn(Optional.of(patient));
        when(doctorProfileRepository.findById(2L)).thenReturn(Optional.of(doctor));
        stubAppointment(AppointmentStatus.CONFIRMED, patient, doctor);
        when(visitRepository.existsByAppointment_Id(5L)).thenReturn(false);
        when(visitRepository.saveAndFlush(any(Visit.class)))
                .thenThrow(new DataIntegrityViolationException("duplicate appointment_id"));

        assertThrows(VisitAlreadyExistsException.class, () -> visitService.startVisit(startRequest(5L)));
    }

    // ---------- startVisitFromAppointment ----------

    @Test
    void startVisitFromAppointment_validAppointment_usesPatientAndDoctorOfAppointment() {
        stubAppointment(AppointmentStatus.SCHEDULED, patient, doctor);
        when(visitRepository.existsByAppointment_Id(5L)).thenReturn(false);
        when(visitRepository.saveAndFlush(any(Visit.class))).then(returnsFirstArg());

        VisitResponse response = visitService.startVisitFromAppointment(5L);

        assertEquals(VisitStatus.IN_PROGRESS, response.getStatus());
        assertEquals(1L, response.getPatientId());
        assertEquals(2L, response.getDoctorId());
        assertEquals(5L, response.getAppointmentId());
    }

    @Test
    void startVisitFromAppointment_unknownAppointment_throws() {
        when(appointmentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(AppointmentNotFoundException.class, () -> visitService.startVisitFromAppointment(99L));
        verify(visitRepository, never()).saveAndFlush(any());
    }

    @ParameterizedTest
    @EnumSource(value = AppointmentStatus.class, names = {"CANCELLED", "NO_SHOW"})
    void startVisitFromAppointment_cancelledOrNoShow_isRejected(AppointmentStatus status) {
        stubAppointment(status, patient, doctor);

        assertThrows(InvalidAppointmentForVisitException.class, () -> visitService.startVisitFromAppointment(5L));
        verify(visitRepository, never()).saveAndFlush(any());
    }

    @Test
    void startVisitFromAppointment_alreadyHasVisit_isRejected() {
        stubAppointment(AppointmentStatus.CONFIRMED, patient, doctor);
        when(visitRepository.existsByAppointment_Id(5L)).thenReturn(true);

        assertThrows(VisitAlreadyExistsException.class, () -> visitService.startVisitFromAppointment(5L));
        verify(visitRepository, never()).saveAndFlush(any());
    }

    // ---------- getVisitByAppointment ----------

    @Test
    void getVisitByAppointment_existing_returnsVisit() {
        Visit visit = visitWithStatus(VisitStatus.IN_PROGRESS);
        visit.setAppointment(appointment(AppointmentStatus.CONFIRMED, patient, doctor));
        when(appointmentRepository.existsById(5L)).thenReturn(true);
        when(visitRepository.findByAppointment_Id(5L)).thenReturn(Optional.of(visit));

        VisitResponse response = visitService.getVisitByAppointment(5L);

        assertEquals(10L, response.getId());
        assertEquals(5L, response.getAppointmentId());
    }

    @Test
    void getVisitByAppointment_unknownAppointment_throwsAppointmentNotFound() {
        when(appointmentRepository.existsById(99L)).thenReturn(false);

        assertThrows(AppointmentNotFoundException.class, () -> visitService.getVisitByAppointment(99L));
    }

    @Test
    void getVisitByAppointment_noVisitYet_throwsVisitNotFound() {
        when(appointmentRepository.existsById(5L)).thenReturn(true);
        when(visitRepository.findByAppointment_Id(5L)).thenReturn(Optional.empty());

        assertThrows(VisitNotFoundException.class, () -> visitService.getVisitByAppointment(5L));
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
        assertFalse(response.getEndedAt().isBefore(response.getStartedAt()));
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