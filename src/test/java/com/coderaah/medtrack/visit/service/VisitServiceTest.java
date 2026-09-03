package com.coderaah.medtrack.visit.service;

import com.coderaah.medtrack.appointment.domain.Appointment;
import com.coderaah.medtrack.appointment.domain.AppointmentStatus;
import com.coderaah.medtrack.appointment.domain.AppointmentType;
import com.coderaah.medtrack.appointment.exception.AppointmentNotFoundException;
import com.coderaah.medtrack.appointment.repository.AppointmentRepository;
import com.coderaah.medtrack.doctor.domain.DoctorProfile;
import com.coderaah.medtrack.doctor.exception.DoctorNotFoundException;
import com.coderaah.medtrack.doctor.repository.DoctorProfileRepository;
import com.coderaah.medtrack.identity.domain.Person;
import com.coderaah.medtrack.patient.domain.PatientProfile;
import com.coderaah.medtrack.patient.exception.PatientNotFoundException;
import com.coderaah.medtrack.patient.repository.PatientProfileRepository;
import com.coderaah.medtrack.visit.domain.Visit;
import com.coderaah.medtrack.visit.domain.VisitStatus;
import com.coderaah.medtrack.visit.dto.CompleteVisitRequest;
import com.coderaah.medtrack.visit.dto.StartVisitFromAppointmentRequest;
import com.coderaah.medtrack.visit.dto.StartVisitRequest;
import com.coderaah.medtrack.visit.dto.UpdateVisitClinicalInfoRequest;
import com.coderaah.medtrack.visit.dto.VisitResponse;
import com.coderaah.medtrack.visit.exception.InvalidVisitOperationException;
import com.coderaah.medtrack.visit.exception.VisitNotFoundException;
import com.coderaah.medtrack.visit.repository.VisitRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class VisitServiceTest {

    private VisitRepository visitRepository;
    private PatientProfileRepository patientProfileRepository;
    private DoctorProfileRepository doctorProfileRepository;
    private AppointmentRepository appointmentRepository;
    private VisitService visitService;

    @BeforeEach
    void setUp() {
        visitRepository = mock(VisitRepository.class);
        patientProfileRepository = mock(PatientProfileRepository.class);
        doctorProfileRepository = mock(DoctorProfileRepository.class);
        appointmentRepository = mock(AppointmentRepository.class);

        visitService = new VisitService(
                visitRepository,
                patientProfileRepository,
                doctorProfileRepository,
                appointmentRepository
        );
    }

    @Test
    void startVisit_createsInProgressVisit() {
        PatientProfile patient = patient(10L);
        DoctorProfile doctor = doctor(20L);
        Visit savedVisit = new Visit(patient, doctor, LocalDateTime.of(2026, 1, 1, 9, 0), VisitStatus.IN_PROGRESS);
        savedVisit.setId(1L);

        StartVisitRequest request = new StartVisitRequest();
        request.setPatientId(10L);
        request.setDoctorId(20L);
        request.setSymptoms("Cough");

        when(patientProfileRepository.findById(10L)).thenReturn(Optional.of(patient));
        when(doctorProfileRepository.findById(20L)).thenReturn(Optional.of(doctor));
        when(visitRepository.save(any(Visit.class))).thenReturn(savedVisit);

        VisitResponse response = visitService.startVisit(request);

        assertEquals(1L, response.getId());
        assertEquals(VisitStatus.IN_PROGRESS, response.getStatus());
        assertEquals(10L, response.getPatientId());
        assertEquals(20L, response.getDoctorId());
    }

    @Test
    void startVisit_throwsWhenPatientNotFound() {
        StartVisitRequest request = new StartVisitRequest();
        request.setPatientId(10L);
        request.setDoctorId(20L);

        when(patientProfileRepository.findById(10L)).thenReturn(Optional.empty());

        assertThrows(PatientNotFoundException.class, () -> visitService.startVisit(request));
    }

    @Test
    void startVisit_throwsWhenDoctorNotFound() {
        StartVisitRequest request = new StartVisitRequest();
        request.setPatientId(10L);
        request.setDoctorId(20L);

        when(patientProfileRepository.findById(10L)).thenReturn(Optional.of(patient(10L)));
        when(doctorProfileRepository.findById(20L)).thenReturn(Optional.empty());

        assertThrows(DoctorNotFoundException.class, () -> visitService.startVisit(request));
    }

    @Test
    void startVisitFromAppointment_throwsWhenCancelled() {
        Appointment appointment = appointment(100L, AppointmentStatus.CANCELLED);
        when(appointmentRepository.findById(100L)).thenReturn(Optional.of(appointment));
        when(visitRepository.existsByAppointmentId(100L)).thenReturn(false);

        assertThrows(
                InvalidVisitOperationException.class,
                () -> visitService.startVisitFromAppointment(100L, new StartVisitFromAppointmentRequest())
        );
    }

    @Test
    void startVisitFromAppointment_throwsWhenVisitAlreadyExists() {
        Appointment appointment = appointment(100L, AppointmentStatus.SCHEDULED);
        when(appointmentRepository.findById(100L)).thenReturn(Optional.of(appointment));
        when(visitRepository.existsByAppointmentId(100L)).thenReturn(true);

        assertThrows(
                InvalidVisitOperationException.class,
                () -> visitService.startVisitFromAppointment(100L, new StartVisitFromAppointmentRequest())
        );
    }

    @Test
    void startVisitFromAppointment_throwsWhenAppointmentMissing() {
        when(appointmentRepository.findById(100L)).thenReturn(Optional.empty());

        assertThrows(
                AppointmentNotFoundException.class,
                () -> visitService.startVisitFromAppointment(100L, new StartVisitFromAppointmentRequest())
        );
    }

    @Test
    void updateClinicalInformation_throwsWhenVisitNotInProgress() {
        Visit visit = new Visit(patient(10L), doctor(20L), LocalDateTime.of(2026, 1, 1, 9, 0), VisitStatus.COMPLETED);
        visit.setId(1L);

        when(visitRepository.findById(1L)).thenReturn(Optional.of(visit));

        UpdateVisitClinicalInfoRequest request = new UpdateVisitClinicalInfoRequest();
        request.setClinicalNotes("Updated");

        assertThrows(
                InvalidVisitOperationException.class,
                () -> visitService.updateClinicalInformation(1L, request)
        );
    }

    @Test
    void completeVisit_setsCompletedStatusAndEndedAt() {
        Visit visit = new Visit(patient(10L), doctor(20L), LocalDateTime.of(2026, 1, 1, 9, 0), VisitStatus.IN_PROGRESS);
        visit.setId(1L);
        when(visitRepository.findById(1L)).thenReturn(Optional.of(visit));
        when(visitRepository.save(any(Visit.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CompleteVisitRequest request = new CompleteVisitRequest();
        request.setEndedAt(LocalDateTime.of(2026, 1, 1, 10, 0));

        VisitResponse response = visitService.completeVisit(1L, request);

        assertEquals(VisitStatus.COMPLETED, response.getStatus());
        assertEquals(LocalDateTime.of(2026, 1, 1, 10, 0), response.getEndedAt());
    }

    @Test
    void completeVisit_throwsWhenEndedAtBeforeStartedAt() {
        Visit visit = new Visit(patient(10L), doctor(20L), LocalDateTime.of(2026, 1, 1, 9, 0), VisitStatus.IN_PROGRESS);
        visit.setId(1L);
        when(visitRepository.findById(1L)).thenReturn(Optional.of(visit));

        CompleteVisitRequest request = new CompleteVisitRequest();
        request.setEndedAt(LocalDateTime.of(2026, 1, 1, 8, 59));

        assertThrows(InvalidVisitOperationException.class, () -> visitService.completeVisit(1L, request));
    }

    @Test
    void cancelVisit_transitionsToCancelled() {
        Visit visit = new Visit(patient(10L), doctor(20L), LocalDateTime.of(2026, 1, 1, 9, 0), VisitStatus.IN_PROGRESS);
        visit.setId(1L);
        when(visitRepository.findById(1L)).thenReturn(Optional.of(visit));
        when(visitRepository.save(any(Visit.class))).thenAnswer(invocation -> invocation.getArgument(0));

        VisitResponse response = visitService.cancelVisit(1L);

        assertEquals(VisitStatus.CANCELLED, response.getStatus());
    }

    @Test
    void getVisitsForPatient_returnsVisitHistory() {
        Visit visit = new Visit(patient(10L), doctor(20L), LocalDateTime.now(), VisitStatus.IN_PROGRESS);
        visit.setId(1L);

        when(patientProfileRepository.existsById(10L)).thenReturn(true);
        when(visitRepository.findByPatientIdOrderByStartedAtDesc(10L)).thenReturn(List.of(visit));

        List<VisitResponse> result = visitService.getVisitsForPatient(10L);

        assertEquals(1, result.size());
        assertEquals(10L, result.get(0).getPatientId());
    }

    @Test
    void getVisitsForPatient_throwsWhenPatientMissing() {
        when(patientProfileRepository.existsById(10L)).thenReturn(false);

        assertThrows(PatientNotFoundException.class, () -> visitService.getVisitsForPatient(10L));
    }

    @Test
    void getVisitById_throwsWhenNotFound() {
        when(visitRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(VisitNotFoundException.class, () -> visitService.getVisitById(1L));
    }

    @Test
    void updateClinicalInformation_updatesStartedAtAndEndedAtConsistently() {
        Visit visit = new Visit(patient(10L), doctor(20L), LocalDateTime.of(2026, 1, 1, 9, 0), VisitStatus.IN_PROGRESS);
        visit.setId(1L);
        when(visitRepository.findById(1L)).thenReturn(Optional.of(visit));
        when(visitRepository.save(any(Visit.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UpdateVisitClinicalInfoRequest request = new UpdateVisitClinicalInfoRequest();
        request.setStartedAt(LocalDateTime.of(2026, 1, 1, 9, 30));
        request.setEndedAt(LocalDateTime.of(2026, 1, 1, 10, 0));
        request.setDiagnosis("Flu");

        VisitResponse response = visitService.updateClinicalInformation(1L, request);

        assertEquals("Flu", response.getDiagnosis());
        assertEquals(LocalDateTime.of(2026, 1, 1, 9, 30), response.getStartedAt());
        assertEquals(LocalDateTime.of(2026, 1, 1, 10, 0), response.getEndedAt());
        verify(visitRepository).save(any(Visit.class));
    }

    @Test
    void updateClinicalInformation_throwsWhenEndedAtBeforeStartedAt() {
        Visit visit = new Visit(patient(10L), doctor(20L), LocalDateTime.of(2026, 1, 1, 9, 0), VisitStatus.IN_PROGRESS);
        visit.setId(1L);
        when(visitRepository.findById(1L)).thenReturn(Optional.of(visit));

        UpdateVisitClinicalInfoRequest request = new UpdateVisitClinicalInfoRequest();
        request.setEndedAt(LocalDateTime.of(2026, 1, 1, 8, 0));

        InvalidVisitOperationException exception = assertThrows(
                InvalidVisitOperationException.class,
                () -> visitService.updateClinicalInformation(1L, request)
        );

        assertTrue(exception.getMessage().contains("end time"));
    }

    private PatientProfile patient(Long id) {
        Person person = new Person("Patient", "One", LocalDate.of(1990, 1, 1), "123");
        PatientProfile patientProfile = new PatientProfile(person, "MRN-" + id);
        patientProfile.setId(id);
        return patientProfile;
    }

    private DoctorProfile doctor(Long id) {
        Person person = new Person("Doctor", "One", LocalDate.of(1980, 1, 1), "456");
        DoctorProfile doctorProfile = new DoctorProfile(person, "LIC-" + id);
        doctorProfile.setId(id);
        return doctorProfile;
    }

    private Appointment appointment(Long id, AppointmentStatus status) {
        Appointment appointment = new Appointment(
                patient(10L),
                doctor(20L),
                LocalDateTime.of(2026, 1, 1, 9, 0),
                LocalDateTime.of(2026, 1, 1, 9, 30),
                AppointmentType.IN_PERSON,
                status
        );
        appointment.setId(id);
        return appointment;
    }
}
