package com.coderaah.medtrack.visit.controller;

import com.coderaah.medtrack.appointment.exception.AppointmentNotFoundException;
import com.coderaah.medtrack.visit.exception.InvalidAppointmentForVisitException;
import com.coderaah.medtrack.visit.exception.VisitAlreadyExistsException;
import com.coderaah.medtrack.common.exception.GlobalExceptionHandler;
import com.coderaah.medtrack.doctor.exception.DoctorNotFoundException;
import com.coderaah.medtrack.patient.exception.PatientNotFoundException;
import com.coderaah.medtrack.visit.domain.VisitStatus;
import com.coderaah.medtrack.visit.dto.StartVisitRequest;
import com.coderaah.medtrack.visit.dto.UpdateClinicalNotesRequest;
import com.coderaah.medtrack.visit.dto.VisitResponse;
import com.coderaah.medtrack.visit.exception.InvalidVisitStatusException;
import com.coderaah.medtrack.visit.exception.VisitExceptionHandler;
import com.coderaah.medtrack.visit.exception.VisitNotFoundException;
import com.coderaah.medtrack.visit.service.VisitService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(VisitController.class)
@Import({GlobalExceptionHandler.class, VisitExceptionHandler.class})
class VisitControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private VisitService visitService;

    private VisitResponse visitResponse(Long id, VisitStatus status) {
        VisitResponse response = new VisitResponse();
        response.setId(id);
        response.setPatientId(1L);
        response.setDoctorId(2L);
        response.setStatus(status);
        response.setSymptoms("Fever");
        response.setDiagnosis("Flu");
        response.setClinicalNotes("Rest and fluids");
        response.setStartedAt(LocalDateTime.of(2026, 10, 7, 10, 0));
        return response;
    }

    // ---------- POST/api/visits ----------

    @Test
    void startVisit_shouldReturn201AndLocation_whenRequestIsValid() throws Exception {


        when(visitService.startVisit(any(StartVisitRequest.class)))
                .thenReturn(visitResponse(5L, VisitStatus.IN_PROGRESS));


        mockMvc.perform(post("/api/visits")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""

                                {
                              "patientId": 1,
                              "doctorId": 2,
                              "symptoms": "Fever"
                            }
                            """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/visits/5"))
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.patientId").value(1))
                .andExpect(jsonPath("$.doctorId").value(2))
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));
    }

    @Test
    void startVisit_shouldReturn400_whenPatientIdIsMissing() throws Exception {


        mockMvc.perform(post("/api/visits")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "doctorId": 2
                            }
                            """))
                .andExpect(status().isBadRequest());


        verifyNoInteractions(visitService);
    }

    @Test
    void startVisit_shouldReturn400_whenDoctorIdIsMissing() throws Exception {

        mockMvc.perform(post("/api/visits")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "patientId": 1
                            }
                            """))
                .andExpect(status().isBadRequest());


        verifyNoInteractions(visitService);
    }

    @Test
    void startVisit_shouldReturn404_whenPatientDoesNotExist() throws Exception {


        when(visitService.startVisit(any(StartVisitRequest.class)))
                .thenThrow(new PatientNotFoundException("Patient not found"));


        mockMvc.perform(post("/api/visits")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "patientId": 99,
                              "doctorId": 2
                            }
                            """))
                .andExpect(status().isNotFound());
    }

    @Test
    void startVisit_shouldReturn404_whenDoctorDoesNotExist() throws Exception {


        when(visitService.startVisit(any(StartVisitRequest.class)))
                .thenThrow(new DoctorNotFoundException("Doctor not found"));


        mockMvc.perform(post("/api/visits")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "patientId": 1,
                              "doctorId": 99
                            }
                            """))
                .andExpect(status().isNotFound());
    }

    // ---------- GET /api/visits/{id} ----------

    @Test
    void getVisitById_shouldReturnVisit_whenVisitExists() throws Exception {


        when(visitService.getVisitById(5L))
                .thenReturn(visitResponse(5L, VisitStatus.IN_PROGRESS));


        mockMvc.perform(get("/api/visits/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.symptoms").value("Fever"))
                .andExpect(jsonPath("$.diagnosis").value("Flu"))
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));
    }

    @Test
    void getVisitById_shouldReturn404_whenVisitDoesNotExist() throws Exception {


        when(visitService.getVisitById(99L))
                .thenThrow(new VisitNotFoundException(99L));


        mockMvc.perform(get("/api/visits/99"))
                .andExpect(status().isNotFound());
    }

    // ---------- GET /api/patients/patientId/visits ----------

    @Test
    void getVisitsByPatient_shouldReturnList_whenPatientExists() throws Exception {


        when(visitService.getVisitsByPatient(1L))
                .thenReturn(List.of(
                        visitResponse(5L, VisitStatus.COMPLETED),
                        visitResponse(6L, VisitStatus.IN_PROGRESS)));


        mockMvc.perform(get("/api/patients/1/visits"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(5))
                .andExpect(jsonPath("$[1].id").value(6));
    }

    @Test
    void getVisitsByPatient_shouldReturn404_whenPatientDoesNotExist() throws Exception {


        when(visitService.getVisitsByPatient(99L))
                .thenThrow(new PatientNotFoundException("Patient not found"));


        mockMvc.perform(get("/api/patients/99/visits"))
                .andExpect(status().isNotFound());
    }

    // ---------- GET /api/doctors/{doctorId}/visits ----------

    @Test
    void getVisitsByDoctor_shouldReturnList_whenDoctorExists() throws Exception {


        when(visitService.getVisitsByDoctor(2L))
                .thenReturn(List.of(visitResponse(5L, VisitStatus.COMPLETED)));


        mockMvc.perform(get("/api/doctors/2/visits"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].doctorId").value(2));
    }

    @Test
    void getVisitsByDoctor_shouldReturn404_whenDoctorDoesNotExist() throws Exception {


        when(visitService.getVisitsByDoctor(99L))
                .thenThrow(new DoctorNotFoundException("Doctor not found"));

        mockMvc.perform(get("/api/doctors/99/visits"))
                .andExpect(status().isNotFound());
    }

    // ---------- PUT /api/visits/{id}/clinical-notes ----------

    @Test
    void updateClinicalNotes_shouldReturnUpdatedVisit_whenRequestIsValid() throws Exception {


        when(visitService.updateClinicalNotes(eq(5L), any(UpdateClinicalNotesRequest.class)))
                .thenReturn(visitResponse(5L, VisitStatus.IN_PROGRESS));


        mockMvc.perform(put("/api/visits/5/clinical-notes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "symptoms": "Fever",
                              "diagnosis": "Flu",
                              "clinicalNotes": "Rest and fluids"
                            }
                            """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.symptoms").value("Fever"))
                .andExpect(jsonPath("$.diagnosis").value("Flu"))
                .andExpect(jsonPath("$.clinicalNotes").value("Rest and fluids"));
    }

    @Test
    void updateClinicalNotes_shouldReturn400_whenSymptomsAreTooLong() throws Exception {


        String tooLong = "a".repeat(5001);


        mockMvc.perform(put("/api/visits/5/clinical-notes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"symptoms\": \"" + tooLong + "\"}"))
                .andExpect(status().isBadRequest());


        verifyNoInteractions(visitService);
    }

    @Test
    void updateClinicalNotes_shouldReturn409_whenVisitIsNotInProgress() throws Exception {


        when(visitService.updateClinicalNotes(eq(5L), any(UpdateClinicalNotesRequest.class)))
                .thenThrow(new InvalidVisitStatusException(
                        "Cannot update clinical notes of a visit with status COMPLETED"));


        mockMvc.perform(put("/api/visits/5/clinical-notes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "diagnosis": "Flu"
                            }
                            """))
                .andExpect(status().isConflict());
    }

    @Test
    void updateClinicalNotes_shouldReturn404_whenVisitDoesNotExist() throws Exception {


        when(visitService.updateClinicalNotes(eq(99L), any(UpdateClinicalNotesRequest.class)))
                .thenThrow(new VisitNotFoundException(99L));


        mockMvc.perform(put("/api/visits/99/clinical-notes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "diagnosis": "Flu"
                            }
                            """))
                .andExpect(status().isNotFound());
    }

    // ---------- PATCH /api/visits/{id}/complete ----------

    @Test
    void completeVisit_shouldReturnCompletedVisit_whenVisitIsInProgress() throws Exception {


        VisitResponse response = visitResponse(5L, VisitStatus.COMPLETED);
        response.setEndedAt(LocalDateTime.of(2026, 10, 7, 10, 30));
        when(visitService.completeVisit(5L)).thenReturn(response);


        mockMvc.perform(patch("/api/visits/5/complete"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.endedAt").exists());
    }

    @Test
    void completeVisit_shouldReturn409_whenVisitIsNotInProgress() throws Exception {


        when(visitService.completeVisit(5L))
                .thenThrow(new InvalidVisitStatusException(
                        "Cannot complete a visit with status CANCELLED"));


        mockMvc.perform(patch("/api/visits/5/complete"))
                .andExpect(status().isConflict());
    }

    @Test
    void completeVisit_shouldReturn404_whenVisitDoesNotExist() throws Exception {


        when(visitService.completeVisit(99L))
                .thenThrow(new VisitNotFoundException(99L));


        mockMvc.perform(patch("/api/visits/99/complete"))
                .andExpect(status().isNotFound());
    }

    // ---------- PATCH /api/visits/{id}/cancel ----------

    @Test
    void cancelVisit_shouldReturnCancelledVisit_whenVisitIsInProgress() throws Exception {


        when(visitService.cancelVisit(5L))
                .thenReturn(visitResponse(5L, VisitStatus.CANCELLED));


        mockMvc.perform(patch("/api/visits/5/cancel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }

    @Test
    void cancelVisit_shouldReturn409_whenVisitIsAlreadyCompleted() throws Exception {


        when(visitService.cancelVisit(5L))
                .thenThrow(new InvalidVisitStatusException(
                        "Cannot cancel a visit with status COMPLETED"));


        mockMvc.perform(patch("/api/visits/5/cancel"))
                .andExpect(status().isConflict());
    }

    @Test
    void cancelVisit_shouldReturn404_whenVisitDoesNotExist() throws Exception {


        when(visitService.cancelVisit(99L))
                .thenThrow(new VisitNotFoundException(99L));


        mockMvc.perform(patch("/api/visits/99/cancel"))
                .andExpect(status().isNotFound());

    }

// ---------- POST /api/appointments/{appointmentId}/visit ----------

@Test
void startVisitFromAppointment_shouldReturn201AndLocation_whenAppointmentIsValid() throws Exception {


    VisitResponse response = visitResponse(7L, VisitStatus.IN_PROGRESS);
    response.setAppointmentId(5L);
    when(visitService.startVisitFromAppointment(5L)).thenReturn(response);


    mockMvc.perform(post("/api/appointments/5/visit"))
            .andExpect(status().isCreated())
            .andExpect(header().string("Location", "/api/visits/7"))
            .andExpect(jsonPath("$.id").value(7))
            .andExpect(jsonPath("$.appointmentId").value(5))
            .andExpect(jsonPath("$.status").value("IN_PROGRESS"));
}

@Test
void startVisitFromAppointment_shouldReturn404_whenAppointmentDoesNotExist() throws Exception {

    // Arrange
    when(visitService.startVisitFromAppointment(99L))
            .thenThrow(new AppointmentNotFoundException("Appointment not found with id: 99"));


    mockMvc.perform(post("/api/appointments/99/visit"))
            .andExpect(status().isNotFound());
}

@Test
void startVisitFromAppointment_shouldReturn409_whenAppointmentIsCancelled() throws Exception {


    when(visitService.startVisitFromAppointment(5L))
            .thenThrow(new InvalidAppointmentForVisitException(
                    "A cancelled appointment cannot start a visit"));


    mockMvc.perform(post("/api/appointments/5/visit"))
            .andExpect(status().isConflict());
}

@Test
void startVisitFromAppointment_shouldReturn409_whenAppointmentAlreadyHasVisit() throws Exception {


    when(visitService.startVisitFromAppointment(5L))
            .thenThrow(new VisitAlreadyExistsException(
                    "A visit already exists for this appointment"));


    mockMvc.perform(post("/api/appointments/5/visit"))
            .andExpect(status().isConflict());
}

@Test
void startVisit_shouldReturn409_whenAppointmentBelongsToDifferentPatient() throws Exception {


    when(visitService.startVisit(any(StartVisitRequest.class)))
            .thenThrow(new InvalidAppointmentForVisitException(
                    "Appointment belongs to a different patient"));


    mockMvc.perform(post("/api/visits")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                            {
                              "patientId": 1,
                              "doctorId": 2,
                              "appointmentId": 5
                            }
                            """))
            .andExpect(status().isConflict());
}

// ---------- GET /api/appointments/{appointmentId}/visit ----------

@Test
void getVisitByAppointment_shouldReturnVisit_whenVisitExists() throws Exception {


    VisitResponse response = visitResponse(7L, VisitStatus.IN_PROGRESS);
    response.setAppointmentId(5L);
    when(visitService.getVisitByAppointment(5L)).thenReturn(response);


    mockMvc.perform(get("/api/appointments/5/visit"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(7))
            .andExpect(jsonPath("$.appointmentId").value(5));
}

@Test
void getVisitByAppointment_shouldReturn404_whenAppointmentDoesNotExist() throws Exception {


    when(visitService.getVisitByAppointment(99L))
            .thenThrow(new AppointmentNotFoundException("Appointment not found with id: 99"));


    mockMvc.perform(get("/api/appointments/99/visit"))
            .andExpect(status().isNotFound());
}

@Test
void getVisitByAppointment_shouldReturn404_whenNoVisitExistsYet() throws Exception {


    when(visitService.getVisitByAppointment(5L))
            .thenThrow(new VisitNotFoundException("No visit exists for this appointment"));


    mockMvc.perform(get("/api/appointments/5/visit"))
            .andExpect(status().isNotFound());
}
}