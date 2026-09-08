package com.coderaah.medtrack.doctor.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.coderaah.medtrack.common.exception.GlobalExceptionHandler;
import com.coderaah.medtrack.doctor.domain.DoctorRelationshipType;
import com.coderaah.medtrack.doctor.dto.CreatePatientDoctorRelationshipRequest;
import com.coderaah.medtrack.doctor.dto.PatientDoctorRelationshipResponse;
import com.coderaah.medtrack.doctor.exception.ActiveFamilyDoctorAlreadyExistsException;
import com.coderaah.medtrack.doctor.exception.RelationshipAlreadyEndedException;
import com.coderaah.medtrack.doctor.service.PatientDoctorRelationshipService;
import com.coderaah.medtrack.patient.exception.PatientNotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(PatientDoctorRelationshipController.class)
@Import(GlobalExceptionHandler.class)
class PatientDoctorRelationshipControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PatientDoctorRelationshipService relationshipService;

    @Test
    void assign_returns201WithBody() throws Exception {
        PatientDoctorRelationshipResponse response = new PatientDoctorRelationshipResponse(
                1L, 1L, 2L, DoctorRelationshipType.FAMILY_DOCTOR, LocalDateTime.now(), null, true);
        when(relationshipService.assign(eq(1L), any())).thenReturn(response);

        mockMvc.perform(post("/api/patients/1/doctors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CreatePatientDoctorRelationshipRequest(2L, DoctorRelationshipType.FAMILY_DOCTOR))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.relationshipType").value("FAMILY_DOCTOR"))
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    void assign_returns400WhenRelationshipTypeMissing() throws Exception {
        mockMvc.perform(post("/api/patients/1/doctors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"doctorId\":2}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void assign_returns404WhenPatientMissing() throws Exception {
        when(relationshipService.assign(eq(99L), any()))
                .thenThrow(new PatientNotFoundException("Patient 99 not found"));

        mockMvc.perform(post("/api/patients/99/doctors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CreatePatientDoctorRelationshipRequest(2L, DoctorRelationshipType.SPECIALIST))))
                .andExpect(status().isNotFound());
    }

    @Test
    void assign_returns409WhenActiveFamilyDoctorExists() throws Exception {
        when(relationshipService.assign(eq(1L), any()))
                .thenThrow(new ActiveFamilyDoctorAlreadyExistsException("Patient 1 already has an active family doctor"));

        mockMvc.perform(post("/api/patients/1/doctors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CreatePatientDoctorRelationshipRequest(2L, DoctorRelationshipType.FAMILY_DOCTOR))))
                .andExpect(status().isConflict());
    }

    @Test
    void findDoctorsForPatient_returns200WithList() throws Exception {
        PatientDoctorRelationshipResponse response = new PatientDoctorRelationshipResponse(
                1L, 1L, 2L, DoctorRelationshipType.SPECIALIST, LocalDateTime.now(), null, true);
        when(relationshipService.findActiveDoctorsForPatient(1L)).thenReturn(List.of(response));

        mockMvc.perform(get("/api/patients/1/doctors"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].doctorId").value(2));
    }

    @Test
    void findPatientsForDoctor_returns200WithList() throws Exception {
        PatientDoctorRelationshipResponse response = new PatientDoctorRelationshipResponse(
                1L, 1L, 2L, DoctorRelationshipType.SPECIALIST, LocalDateTime.now(), null, true);
        when(relationshipService.findActivePatientsForDoctor(2L)).thenReturn(List.of(response));

        mockMvc.perform(get("/api/doctors/2/patients"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].patientId").value(1));
    }

    @Test
    void end_returns200WithDeactivatedBody() throws Exception {
        PatientDoctorRelationshipResponse response = new PatientDoctorRelationshipResponse(
                1L, 1L, 2L, DoctorRelationshipType.FAMILY_DOCTOR, LocalDateTime.now(), LocalDateTime.now(), false);
        when(relationshipService.end(1L)).thenReturn(response);

        mockMvc.perform(patch("/api/patient-doctor-relationships/1/end"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false));
    }

    @Test
    void end_returns409WhenAlreadyEnded() throws Exception {
        when(relationshipService.end(1L))
                .thenThrow(new RelationshipAlreadyEndedException("Patient-doctor relationship 1 has already ended"));

        mockMvc.perform(patch("/api/patient-doctor-relationships/1/end"))
                .andExpect(status().isConflict());
    }
}