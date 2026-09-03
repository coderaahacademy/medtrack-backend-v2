package com.coderaah.medtrack.patient.controller;

import com.coderaah.medtrack.patient.dto.PatientRequest;
import com.coderaah.medtrack.patient.dto.PatientResponse;
import com.coderaah.medtrack.patient.exception.DuplicateMedicalRecordNumberException;
import com.coderaah.medtrack.patient.exception.PatientNotFoundException;
import com.coderaah.medtrack.patient.service.PatientService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@WebMvcTest(PatientController.class)
public class PatientControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PatientService patientService;

    @Test
    void getPatientById_returnsOk() throws Exception {

        PatientResponse response = new PatientResponse();
        response.setId(1L);
        response.setFirstName("John");
        response.setLastName("Smith");
        response.setMedicalRecordNumber("MRN-001");

        when(patientService.getPatientById(1L))
                .thenReturn(response);

        mockMvc.perform(get("/api/patients/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Smith"))
                .andExpect(jsonPath("$.medicalRecordNumber").value("MRN-001"));
    }

    @Test
    void getPatientById_returnsNotFound() throws Exception {

        when(patientService.getPatientById(99999L))
                .thenThrow(new PatientNotFoundException("Patient not found"));

        mockMvc.perform(get("/api/patients/99999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void createPatient_returnsCreated() throws Exception {

        PatientResponse response = new PatientResponse();
        response.setId(1L);
        response.setFirstName("John");
        response.setLastName("Smith");
        response.setMedicalRecordNumber("MRN-001");

        when(patientService.createPatient(org.mockito.ArgumentMatchers.any(PatientRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/patients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {
                      "firstName": "John",
                      "lastName": "Smith",
                      "medicalRecordNumber": "MRN-001"
                    }
                    """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Smith"))
                .andExpect(jsonPath("$.medicalRecordNumber").value("MRN-001"));
    }

    @Test
    void createPatient_returnsBadRequest_whenRequiredFieldIsBlank() throws Exception {

        mockMvc.perform(post("/api/patients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "firstName": "",
                              "lastName": "Smith",
                              "medicalRecordNumber": "MRN-002"
                            }
                            """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createPatient_returnsBadRequest_whenMedicalRecordNumberExists() throws Exception {

        when(patientService.createPatient(org.mockito.ArgumentMatchers.any(PatientRequest.class)))
                .thenThrow(new DuplicateMedicalRecordNumberException("Medical record number already exists"));

        mockMvc.perform(post("/api/patients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "firstName": "Sara",
                              "lastName": "Jones",
                              "medicalRecordNumber": "MRN-001"
                            }
                            """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getAllPatients_returnsOk() throws Exception {

        PatientResponse patient1 = new PatientResponse();
        patient1.setId(1L);
        patient1.setFirstName("John");

        PatientResponse patient2 = new PatientResponse();
        patient2.setId(2L);
        patient2.setFirstName("Sara");

        when(patientService.getAllPatients())
                .thenReturn(List.of(patient1, patient2));

        mockMvc.perform(get("/api/patients"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].firstName").value("John"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].firstName").value("Sara"));
    }

    @Test
    void updatePatient_returnsOk() throws Exception {

        PatientResponse response = new PatientResponse();
        response.setId(1L);
        response.setFirstName("John");
        response.setLastName("Updated");
        response.setMedicalRecordNumber("MRN-001");

        when(patientService.updatePatient(
                org.mockito.ArgumentMatchers.eq(1L),
                org.mockito.ArgumentMatchers.any(PatientRequest.class)
        )).thenReturn(response);

        mockMvc.perform(put("/api/patients/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {
                      "firstName": "John",
                      "lastName": "Updated",
                      "medicalRecordNumber": "MRN-001"
                    }
                    """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Updated"))
                .andExpect(jsonPath("$.medicalRecordNumber").value("MRN-001"));
    }
    @Test
    void updatePatient_returnsNotFound_whenPatientDoesNotExist() throws Exception {

        when(patientService.updatePatient(
                org.mockito.ArgumentMatchers.eq(99999L),
                org.mockito.ArgumentMatchers.any(PatientRequest.class)
        )).thenThrow(new PatientNotFoundException("Patient not found"));

        mockMvc.perform(put("/api/patients/99999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "firstName": "John",
                              "lastName": "Smith",
                              "medicalRecordNumber": "MRN-001"
                            }
                            """))
                .andExpect(status().isNotFound());
    }

    @Test
    void updatePatient_returnsBadRequest_whenMedicalRecordNumberExists() throws Exception {

        when(patientService.updatePatient(
                org.mockito.ArgumentMatchers.eq(1L),
                org.mockito.ArgumentMatchers.any(PatientRequest.class)
        )).thenThrow(new DuplicateMedicalRecordNumberException("Medical record number already exists"));

        mockMvc.perform(put("/api/patients/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "firstName": "John",
                              "lastName": "Smith",
                              "medicalRecordNumber": "MRN-002"
                            }
                            """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createPatient_returnsBadRequest_whenFirstNameExceedsMaximumLength() throws Exception {

        String longFirstName = "A".repeat(101);

        mockMvc.perform(post("/api/patients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                          "firstName": "%s",
                          "lastName": "Smith",
                          "medicalRecordNumber": "MRN-003"
                        }
                        """.formatted(longFirstName)))
                .andExpect(status().isBadRequest());
    }
}
