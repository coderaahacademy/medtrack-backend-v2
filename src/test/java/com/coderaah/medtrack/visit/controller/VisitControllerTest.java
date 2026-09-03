package com.coderaah.medtrack.visit.controller;

import com.coderaah.medtrack.appointment.exception.AppointmentNotFoundException;
import com.coderaah.medtrack.doctor.exception.DoctorNotFoundException;
import com.coderaah.medtrack.patient.exception.PatientNotFoundException;
import com.coderaah.medtrack.visit.domain.VisitStatus;
import com.coderaah.medtrack.visit.dto.VisitResponse;
import com.coderaah.medtrack.visit.exception.InvalidVisitOperationException;
import com.coderaah.medtrack.visit.exception.VisitNotFoundException;
import com.coderaah.medtrack.visit.service.VisitService;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(VisitController.class)
class VisitControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private VisitService visitService;

    @Test
    void startVisit_returnsCreated() throws Exception {
        VisitResponse response = visitResponse(1L, VisitStatus.IN_PROGRESS);

        when(visitService.startVisit(any())).thenReturn(response);

        mockMvc.perform(post("/api/visits")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "patientId": 10,
                                  "doctorId": 20,
                                  "symptoms": "headache"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));
    }

    @Test
    void startVisit_returnsBadRequestWhenMissingRequiredFields() throws Exception {
        mockMvc.perform(post("/api/visits")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "symptoms": "headache"
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getVisitById_returnsOk() throws Exception {
        when(visitService.getVisitById(1L)).thenReturn(visitResponse(1L, VisitStatus.IN_PROGRESS));

        mockMvc.perform(get("/api/visits/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void getVisitById_returnsNotFound() throws Exception {
        when(visitService.getVisitById(9L)).thenThrow(new VisitNotFoundException("Visit not found"));

        mockMvc.perform(get("/api/visits/9"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getVisitsForPatient_returnsOk() throws Exception {
        when(visitService.getVisitsForPatient(10L)).thenReturn(List.of(visitResponse(1L, VisitStatus.COMPLETED)));

        mockMvc.perform(get("/api/patients/10/visits"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("COMPLETED"));
    }

    @Test
    void getVisitsForPatient_returnsNotFoundWhenPatientMissing() throws Exception {
        when(visitService.getVisitsForPatient(99L)).thenThrow(new PatientNotFoundException("Patient not found"));

        mockMvc.perform(get("/api/patients/99/visits"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getVisitsForDoctor_returnsNotFoundWhenDoctorMissing() throws Exception {
        when(visitService.getVisitsForDoctor(55L)).thenThrow(new DoctorNotFoundException("Doctor not found"));

        mockMvc.perform(get("/api/doctors/55/visits"))
                .andExpect(status().isNotFound());
    }

    @Test
    void startVisitFromAppointment_returnsCreated() throws Exception {
        when(visitService.startVisitFromAppointment(eq(100L), any())).thenReturn(visitResponse(2L, VisitStatus.IN_PROGRESS));

        mockMvc.perform(post("/api/appointments/100/visit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "clinicalNotes": "Initial check"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(2));
    }

    @Test
    void startVisitFromAppointment_returnsBadRequestForInvalidAppointmentStatus() throws Exception {
        when(visitService.startVisitFromAppointment(eq(100L), any()))
                .thenThrow(new InvalidVisitOperationException("Cannot start visit from cancelled or no-show appointment"));

        mockMvc.perform(post("/api/appointments/100/visit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getVisitByAppointment_returnsNotFoundWhenAppointmentUnknown() throws Exception {
        when(visitService.getVisitByAppointment(100L)).thenThrow(new AppointmentNotFoundException("Appointment not found"));

        mockMvc.perform(get("/api/appointments/100/visit"))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateClinicalInformation_returnsOk() throws Exception {
        when(visitService.updateClinicalInformation(eq(1L), any())).thenReturn(visitResponse(1L, VisitStatus.IN_PROGRESS));

        mockMvc.perform(put("/api/visits/1/clinical-notes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "symptoms": "Updated symptoms",
                                  "diagnosis": "Updated diagnosis",
                                  "clinicalNotes": "Updated notes"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void completeVisit_returnsOk() throws Exception {
        when(visitService.completeVisit(eq(1L), any())).thenReturn(visitResponse(1L, VisitStatus.COMPLETED));

        mockMvc.perform(patch("/api/visits/1/complete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "endedAt": "2026-01-01T10:00:00"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }

    @Test
    void cancelVisit_returnsBadRequestForInvalidTransition() throws Exception {
        when(visitService.cancelVisit(1L)).thenThrow(new InvalidVisitOperationException("Only an in-progress visit can be modified"));

        mockMvc.perform(patch("/api/visits/1/cancel"))
                .andExpect(status().isBadRequest());
    }

    private VisitResponse visitResponse(Long id, VisitStatus status) {
        VisitResponse response = new VisitResponse();
        response.setId(id);
        response.setPatientId(10L);
        response.setDoctorId(20L);
        response.setStatus(status);
        response.setStartedAt(LocalDateTime.of(2026, 1, 1, 9, 0));
        return response;
    }
}
