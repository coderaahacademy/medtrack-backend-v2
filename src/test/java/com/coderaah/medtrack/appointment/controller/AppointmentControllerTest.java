package com.coderaah.medtrack.appointment.controller;

import com.coderaah.medtrack.appointment.domain.AppointmentStatus;
import com.coderaah.medtrack.appointment.domain.AppointmentType;
import com.coderaah.medtrack.appointment.dto.AppointmentRequest;
import com.coderaah.medtrack.appointment.dto.AppointmentResponse;
import com.coderaah.medtrack.appointment.exception.AppointmentNotFoundException;
import com.coderaah.medtrack.appointment.exception.AppointmentOverlapException;
import com.coderaah.medtrack.appointment.exception.CancellationReasonRequiredException;
import com.coderaah.medtrack.appointment.exception.DoctorNotActiveException;
import com.coderaah.medtrack.appointment.exception.DoctorNotAvailableException;
import com.coderaah.medtrack.appointment.exception.InvalidAppointmentTimeException;
import com.coderaah.medtrack.appointment.exception.InvalidStatusTransitionException;
import com.coderaah.medtrack.appointment.service.AppointmentService;
import com.coderaah.medtrack.doctor.exception.DoctorNotFoundException;
import com.coderaah.medtrack.patient.exception.PatientNotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AppointmentController.class)
class AppointmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AppointmentService appointmentService;

    private AppointmentRequest validRequest() {
        AppointmentRequest request = new AppointmentRequest();
        request.setPatientId(1L);
        request.setDoctorId(2L);
        request.setLocation("Room 1");
        request.setReason("Annual check-up");
        request.setAppointmentType(AppointmentType.IN_PERSON);
        request.setScheduledStart(LocalDateTime.now().plusDays(1));
        request.setScheduledEnd(LocalDateTime.now().plusDays(1).plusHours(1));
        return request;
    }

    private AppointmentResponse responseWithId(Long id) {
        AppointmentResponse response = new AppointmentResponse();
        response.setId(id);
        return response;
    }

    private org.springframework.test.web.servlet.ResultActions postAppointment(AppointmentRequest request)
            throws Exception {
        return mockMvc.perform(post("/api/appointments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));
    }

    // ---------- POST /api/appointments ----------

    @Test
    void create_returnsCreated_whenRequestIsValid() throws Exception {
        AppointmentResponse response = responseWithId(1L);
        response.setStatus(AppointmentStatus.SCHEDULED);
        when(appointmentService.createAppointment(any(AppointmentRequest.class))).thenReturn(response);

        postAppointment(validRequest())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("SCHEDULED"));
    }

    @Test
    void create_returnsBadRequest_whenReasonExceeds255Characters() throws Exception {
        AppointmentRequest request = validRequest();
        request.setReason("a".repeat(256));

        postAppointment(request).andExpect(status().isBadRequest());

        verify(appointmentService, never()).createAppointment(any());
    }

    @Test
    void create_acceptsReasonOfExactly255Characters() throws Exception {
        AppointmentRequest request = validRequest();
        request.setReason("a".repeat(255));
        when(appointmentService.createAppointment(any(AppointmentRequest.class))).thenReturn(responseWithId(1L));

        postAppointment(request).andExpect(status().isCreated());
    }

    @Test
    void create_returnsBadRequest_whenLocationExceeds255Characters() throws Exception {
        AppointmentRequest request = validRequest();
        request.setLocation("a".repeat(256));

        postAppointment(request).andExpect(status().isBadRequest());

        verify(appointmentService, never()).createAppointment(any());
    }

    @Test
    void create_returnsBadRequest_whenPatientIdIsMissing() throws Exception {
        AppointmentRequest request = validRequest();
        request.setPatientId(null);

        postAppointment(request).andExpect(status().isBadRequest());
    }

    @Test
    void create_returnsBadRequest_whenStartIsInThePast() throws Exception {
        AppointmentRequest request = validRequest();
        request.setScheduledStart(LocalDateTime.now().minusDays(1));

        postAppointment(request).andExpect(status().isBadRequest());
    }

    @Test
    void create_returnsBadRequest_whenAppointmentTypeIsUnknown() throws Exception {
        mockMvc.perform(post("/api/appointments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "patientId": 1,
                                  "doctorId": 2,
                                  "appointmentType": "TELEPATHY",
                                  "scheduledStart": "2099-01-01T10:00:00",
                                  "scheduledEnd": "2099-01-01T11:00:00"
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_returnsBadRequest_whenStartIsNotBeforeEnd() throws Exception {
        when(appointmentService.createAppointment(any(AppointmentRequest.class)))
                .thenThrow(new InvalidAppointmentTimeException("Appointment start must be before its end"));

        postAppointment(validRequest()).andExpect(status().isBadRequest());
    }

    @Test
    void create_returnsNotFound_whenPatientDoesNotExist() throws Exception {
        when(appointmentService.createAppointment(any(AppointmentRequest.class)))
                .thenThrow(new PatientNotFoundException("Patient not found"));

        postAppointment(validRequest()).andExpect(status().isNotFound());
    }

    @Test
    void create_returnsNotFound_whenDoctorDoesNotExist() throws Exception {
        when(appointmentService.createAppointment(any(AppointmentRequest.class)))
                .thenThrow(new DoctorNotFoundException("Doctor not found"));

        postAppointment(validRequest()).andExpect(status().isNotFound());
    }

    @Test
    void create_returnsConflict_whenDoctorIsNotActive() throws Exception {
        when(appointmentService.createAppointment(any(AppointmentRequest.class)))
                .thenThrow(new DoctorNotActiveException("Doctor is not active"));

        postAppointment(validRequest()).andExpect(status().isConflict());
    }

    @Test
    void create_returnsConflict_whenDoctorIsNotAvailable() throws Exception {
        when(appointmentService.createAppointment(any(AppointmentRequest.class)))
                .thenThrow(new DoctorNotAvailableException("Doctor is unavailable during this period"));

        postAppointment(validRequest()).andExpect(status().isConflict());
    }

    @Test
    void create_returnsConflict_whenDoctorIsAlreadyBooked() throws Exception {
        when(appointmentService.createAppointment(any(AppointmentRequest.class)))
                .thenThrow(new AppointmentOverlapException("Doctor already has an overlapping appointment"));

        postAppointment(validRequest()).andExpect(status().isConflict());
    }

    // ---------- GET ----------

    @Test
    void getById_returnsOk_whenFound() throws Exception {
        when(appointmentService.getById(5L)).thenReturn(responseWithId(5L));

        mockMvc.perform(get("/api/appointments/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5));
    }

    @Test
    void getById_returnsNotFound_whenMissing() throws Exception {
        when(appointmentService.getById(999L))
                .thenThrow(new AppointmentNotFoundException("Appointment not found"));

        mockMvc.perform(get("/api/appointments/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getByPatient_returnsOk_withAppointments() throws Exception {
        when(appointmentService.getByPatient(3L)).thenReturn(List.of(responseWithId(1L), responseWithId(2L)));

        mockMvc.perform(get("/api/patients/3/appointments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].id").value(2));
    }

    @Test
    void getByDoctor_returnsAllAppointments_whenNoDateGiven() throws Exception {
        when(appointmentService.getByDoctor(4L)).thenReturn(List.of(responseWithId(1L)));

        mockMvc.perform(get("/api/doctors/4/appointments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));

        verify(appointmentService, never()).getByDoctorAndDate(anyLong(), any());
    }

    @Test
    void getByDoctor_filtersByDate_whenDateGiven() throws Exception {
        when(appointmentService.getByDoctorAndDate(4L, LocalDate.of(2026, 9, 10)))
                .thenReturn(List.of(responseWithId(7L)));

        mockMvc.perform(get("/api/doctors/4/appointments").param("date", "2026-09-10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(7));

        verify(appointmentService, never()).getByDoctor(anyLong());
    }

    @Test
    void getByDoctor_returnsBadRequest_whenDateIsMalformed() throws Exception {
        mockMvc.perform(get("/api/doctors/4/appointments").param("date", "10-09-2026"))
                .andExpect(status().isBadRequest());
    }

    // ---------- lifecycle ----------

    @Test
    void confirm_returnsOk() throws Exception {
        AppointmentResponse response = responseWithId(7L);
        response.setStatus(AppointmentStatus.CONFIRMED);
        when(appointmentService.confirm(7L, 1L)).thenReturn(response);

        mockMvc.perform(patch("/api/appointments/7/confirm").param("actorUserId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.status").value("CONFIRMED"));
    }

    @Test
    void confirm_returnsConflict_whenTransitionIsInvalid() throws Exception {
        when(appointmentService.confirm(anyLong(), anyLong()))
                .thenThrow(new InvalidStatusTransitionException("Invalid status transition: COMPLETED -> CONFIRMED"));

        mockMvc.perform(patch("/api/appointments/7/confirm").param("actorUserId", "1"))
                .andExpect(status().isConflict());
    }

    @Test
    void confirm_returnsNotFound_whenAppointmentIsMissing() throws Exception {
        when(appointmentService.confirm(anyLong(), anyLong()))
                .thenThrow(new AppointmentNotFoundException("Appointment not found"));

        mockMvc.perform(patch("/api/appointments/404/confirm").param("actorUserId", "1"))
                .andExpect(status().isNotFound());
    }

    @Test
    void confirm_returnsBadRequest_whenActorUserIdIsMissing() throws Exception {
        mockMvc.perform(patch("/api/appointments/7/confirm"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void cancel_returnsOk_andPassesReasonToService() throws Exception {
        AppointmentResponse response = responseWithId(7L);
        response.setStatus(AppointmentStatus.CANCELLED);
        response.setCancellationReason("Patient requested");
        when(appointmentService.cancel(7L, 1L, "Patient requested")).thenReturn(response);

        mockMvc.perform(patch("/api/appointments/7/cancel")
                        .param("actorUserId", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "reason": "Patient requested" }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"))
                .andExpect(jsonPath("$.cancellationReason").value("Patient requested"));
    }

    @Test
    void cancel_returnsBadRequest_whenReasonIsBlank() throws Exception {
        mockMvc.perform(patch("/api/appointments/7/cancel")
                        .param("actorUserId", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "reason": "  " }
                                """))
                .andExpect(status().isBadRequest());

        verify(appointmentService, never()).cancel(anyLong(), anyLong(), any());
    }

    @Test
    void cancel_returnsBadRequest_whenReasonExceeds255Characters() throws Exception {
        mockMvc.perform(patch("/api/appointments/7/cancel")
                        .param("actorUserId", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "reason": "%s" }
                                """.formatted("a".repeat(256))))
                .andExpect(status().isBadRequest());

        verify(appointmentService, never()).cancel(anyLong(), anyLong(), any());
    }

    @Test
    void cancel_returnsBadRequest_whenBodyIsMissing() throws Exception {
        mockMvc.perform(patch("/api/appointments/7/cancel").param("actorUserId", "1"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void cancel_returnsBadRequest_whenServiceRejectsMissingReason() throws Exception {
        when(appointmentService.cancel(eq(7L), eq(1L), any()))
                .thenThrow(new CancellationReasonRequiredException("Cancellation reason is required"));

        mockMvc.perform(patch("/api/appointments/7/cancel")
                        .param("actorUserId", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "reason": "x" }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void cancel_returnsConflict_whenTransitionIsInvalid() throws Exception {
        when(appointmentService.cancel(eq(7L), eq(1L), any()))
                .thenThrow(new InvalidStatusTransitionException("Invalid status transition: COMPLETED -> CANCELLED"));

        mockMvc.perform(patch("/api/appointments/7/cancel")
                        .param("actorUserId", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "reason": "Too late" }
                                """))
                .andExpect(status().isConflict());
    }

    @Test
    void complete_returnsOk() throws Exception {
        AppointmentResponse response = responseWithId(7L);
        response.setStatus(AppointmentStatus.COMPLETED);
        when(appointmentService.complete(7L, 1L)).thenReturn(response);

        mockMvc.perform(patch("/api/appointments/7/complete").param("actorUserId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }

    @Test
    void complete_returnsConflict_whenTransitionIsInvalid() throws Exception {
        when(appointmentService.complete(anyLong(), anyLong()))
                .thenThrow(new InvalidStatusTransitionException("Invalid status transition: SCHEDULED -> COMPLETED"));

        mockMvc.perform(patch("/api/appointments/7/complete").param("actorUserId", "1"))
                .andExpect(status().isConflict());
    }

    @Test
    void noShow_returnsOk() throws Exception {
        AppointmentResponse response = responseWithId(7L);
        response.setStatus(AppointmentStatus.NO_SHOW);
        when(appointmentService.noShow(7L, 1L)).thenReturn(response);

        mockMvc.perform(patch("/api/appointments/7/no-show").param("actorUserId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("NO_SHOW"));
    }

    @Test
    void noShow_returnsConflict_whenTransitionIsInvalid() throws Exception {
        when(appointmentService.noShow(anyLong(), anyLong()))
                .thenThrow(new InvalidStatusTransitionException("Invalid status transition: SCHEDULED -> NO_SHOW"));

        mockMvc.perform(patch("/api/appointments/7/no-show").param("actorUserId", "1"))
                .andExpect(status().isConflict());
    }
}
