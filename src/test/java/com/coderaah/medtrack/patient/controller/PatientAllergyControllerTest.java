package com.coderaah.medtrack.patient.controller;


import com.coderaah.medtrack.patient.domain.AllergySeverity;
import com.coderaah.medtrack.patient.domain.AllergyStatus;
import com.coderaah.medtrack.patient.dto.AllergyResponse;
import com.coderaah.medtrack.patient.exception.AllergyNotFoundException;
import com.coderaah.medtrack.patient.exception.PatientNotFoundException;
import com.coderaah.medtrack.patient.service.PatientAllergyService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(PatientAllergyController.class)
public class PatientAllergyControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PatientAllergyService patientAllergyService;

    @Test
    void createAllergy_returnsCreated() throws Exception {

        AllergyResponse response = new AllergyResponse();
        response.setId(1L);
        response.setPatientId(1L);
        response.setAllergen("Penicillin");
        response.setSeverity(AllergySeverity.MODERATE);
        response.setStatus(AllergyStatus.ACTIVE);

        when(patientAllergyService.createAllergy(eq(1L),any()))
                .thenReturn(response);

        mockMvc.perform(post("/api/patients/1/allergies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "allergen": "Penicillin",
                              "severity": "MODERATE",
                              "status": "ACTIVE"
                            }
                            """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.patientId").value(1))
                .andExpect(jsonPath("$.allergen").value("Penicillin"))
                .andExpect(jsonPath("$.severity").value("MODERATE"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void createAllergy_returnsBadRequest_whenAllergenIsBlank() throws Exception {

        mockMvc.perform(post("/api/patients/1/allergies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "allergen": ""
                            }
                            """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createAllergy_returnsNotFound_whenPatientDoesNotExist() throws Exception {

        when(patientAllergyService.createAllergy(eq(999L), any()))
                .thenThrow(new PatientNotFoundException("Patient not found"));

        mockMvc.perform(post("/api/patients/999/allergies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "allergen": "Penicillin"
                            }
                            """))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAllergies_returnsOk() throws Exception {

        AllergyResponse allergy1 = new AllergyResponse();
        allergy1.setId(1L);
        allergy1.setAllergen("Penicillin");

        AllergyResponse allergy2 = new AllergyResponse();
        allergy2.setId(2L);
        allergy2.setAllergen("Peanuts");

        when(patientAllergyService.getAllergies(1L, null))
                .thenReturn(List.of(allergy1, allergy2));

        mockMvc.perform(get("/api/patients/1/allergies"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].allergen").value("Penicillin"))
                .andExpect(jsonPath("$[1].allergen").value("Peanuts"));
    }

    @Test
    void getAllergies_returnsOnlyActive_whenStatusFilterProvided() throws Exception {

        AllergyResponse activeAllergy = new AllergyResponse();
        activeAllergy.setId(1L);
        activeAllergy.setAllergen("Penicillin");
        activeAllergy.setStatus(AllergyStatus.ACTIVE);

        when(patientAllergyService.getAllergies(1L, AllergyStatus.ACTIVE))
                .thenReturn(List.of(activeAllergy));

        mockMvc.perform(get("/api/patients/1/allergies?status=ACTIVE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("ACTIVE"));
    }

    @Test
    void getAllergies_returnsNotFound_whenPatientDoesNotExist() throws Exception {

        when(patientAllergyService.getAllergies(eq(999L), any()))
                .thenThrow(new PatientNotFoundException("Patient not found"));

        mockMvc.perform(get("/api/patients/999/allergies"))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateAllergy_returnsOk() throws Exception {

        AllergyResponse response = new AllergyResponse();
        response.setId(10L);
        response.setAllergen("Penicillin");
        response.setSeverity(AllergySeverity.SEVERE);

        when(patientAllergyService.updateAllergy(eq(1L), eq(10L), any()))
                .thenReturn(response);

        mockMvc.perform(put("/api/patients/1/allergies/10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "allergen": "Penicillin",
                              "severity": "SEVERE"
                            }
                            """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.allergen").value("Penicillin"))
                .andExpect(jsonPath("$.severity").value("SEVERE"));
    }

    @Test
    void updateAllergy_returnsNotFound_whenAllergyDoesNotBelongToPatient() throws Exception {

        when(patientAllergyService.updateAllergy(eq(1L), eq(999L), any()))
                .thenThrow(new AllergyNotFoundException("Allergy not found"));

        mockMvc.perform(put("/api/patients/1/allergies/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "allergen": "Penicillin"
                            }
                            """))
                .andExpect(status().isNotFound());
    }

    @Test
    void changeAllergyStatus_returnsOk() throws Exception {

        AllergyResponse response = new AllergyResponse();
        response.setId(10L);
        response.setStatus(AllergyStatus.RESOLVED);

        when(patientAllergyService.changeAllergyStatus(1L, 10L, AllergyStatus.RESOLVED))
                .thenReturn(response);

        mockMvc.perform(patch("/api/patients/1/allergies/10/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "status": "RESOLVED"
                            }
                            """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("RESOLVED"));
    }

    @Test
    void changeAllergyStatus_returnsBadRequest_whenStatusIsMissing() throws Exception {

        mockMvc.perform(patch("/api/patients/1/allergies/10/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {}
                            """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void changeAllergyStatus_returnsNotFound_whenAllergyDoesNotExist() throws Exception {

        when(patientAllergyService.changeAllergyStatus(1L, 999L, AllergyStatus.RESOLVED))
                .thenThrow(new AllergyNotFoundException("Allergy not found"));

        mockMvc.perform(patch("/api/patients/1/allergies/999/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "status": "RESOLVED"
                            }
                            """))
                .andExpect(status().isNotFound());
    }
}

