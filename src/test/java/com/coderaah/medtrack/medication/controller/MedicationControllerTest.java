package com.coderaah.medtrack.medication.controller;

import com.coderaah.medtrack.medication.domain.DosageForm;
import com.coderaah.medtrack.medication.dto.MedicationResponse;
import com.coderaah.medtrack.medication.exception.MedicationNotFoundException;
import com.coderaah.medtrack.medication.service.MedicationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MedicationController.class)
public class MedicationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MedicationService medicationService;

    @Test
    void createMedication_returnsCreated() throws Exception {

        MedicationResponse response = new MedicationResponse();
        response.setId(1L);
        response.setGenericName("Amoxicillin");
        response.setBrandName("Amoxil");
        response.setStrength("500");
        response.setStrengthUnit("mg");
        response.setDosageForm(DosageForm.CAPSULE);
        response.setActive(true);

        when(medicationService.createMedication(any()))
                .thenReturn(response);

        mockMvc.perform(post("/api/medications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "genericName": "Amoxicillin",
                              "brandName": "Amoxil",
                              "strength": "500",
                              "strengthUnit": "mg",
                              "dosageForm": "CAPSULE"
                            }
                            """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.genericName").value("Amoxicillin"))
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    void createMedication_returnsBadRequest_whenGenericNameIsBlank() throws Exception {

        mockMvc.perform(post("/api/medications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "genericName": "",
                              "strength": "500",
                              "strengthUnit": "mg",
                              "dosageForm": "CAPSULE"
                            }
                            """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createMedication_returnsBadRequest_whenGenericNameExceedsMaxLength() throws Exception {

        String longGenericName = "A".repeat(256);

        mockMvc.perform(post("/api/medications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "genericName": "%s",
                              "strength": "500",
                              "strengthUnit": "mg",
                              "dosageForm": "CAPSULE"
                            }
                            """.formatted(longGenericName)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createMedication_returnsBadRequest_whenDosageFormIsMissing() throws Exception {

        mockMvc.perform(post("/api/medications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "genericName": "Amoxicillin",
                              "strength": "500",
                              "strengthUnit": "mg"
                            }
                            """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getMedicationById_returnsOk() throws Exception {

        MedicationResponse response = new MedicationResponse();
        response.setId(1L);
        response.setGenericName("Ibuprofen");
        response.setActive(false);

        when(medicationService.getMedicationById(1L))
                .thenReturn(response);

        mockMvc.perform(get("/api/medications/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.genericName").value("Ibuprofen"))
                .andExpect(jsonPath("$.active").value(false));
    }

    @Test
    void getMedicationById_returnsNotFound() throws Exception {

        when(medicationService.getMedicationById(999L))
                .thenThrow(new MedicationNotFoundException("Medication not found"));

        mockMvc.perform(get("/api/medications/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void listMedications_returnsOk() throws Exception {

        MedicationResponse medication1 = new MedicationResponse();
        medication1.setId(1L);
        medication1.setGenericName("Ibuprofen");

        MedicationResponse medication2 = new MedicationResponse();
        medication2.setId(2L);
        medication2.setGenericName("Paracetamol");

        when(medicationService.listMedications(null, null))
                .thenReturn(List.of(medication1, medication2));

        mockMvc.perform(get("/api/medications"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].genericName").value("Ibuprofen"))
                .andExpect(jsonPath("$[1].genericName").value("Paracetamol"));
    }

    @Test
    void listMedications_filtersByActive() throws Exception {

        MedicationResponse response = new MedicationResponse();
        response.setId(1L);
        response.setGenericName("Ibuprofen");
        response.setActive(true);

        when(medicationService.listMedications(true, null))
                .thenReturn(List.of(response));

        mockMvc.perform(get("/api/medications").param("active", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].active").value(true));
    }

    @Test
    void listMedications_filtersBySearch() throws Exception {

        MedicationResponse response = new MedicationResponse();
        response.setId(1L);
        response.setGenericName("Amoxicillin");

        when(medicationService.listMedications(null, "amox"))
                .thenReturn(List.of(response));

        mockMvc.perform(get("/api/medications").param("search", "amox"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].genericName").value("Amoxicillin"));
    }

    @Test
    void updateMedication_returnsOk() throws Exception {

        MedicationResponse response = new MedicationResponse();
        response.setId(1L);
        response.setGenericName("Ibuprofen");
        response.setBrandName("Nurofen");
        response.setStrength("400");
        response.setStrengthUnit("mg");
        response.setDosageForm(DosageForm.TABLET);

        when(medicationService.updateMedication(eq(1L), any()))
                .thenReturn(response);

        mockMvc.perform(put("/api/medications/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "genericName": "Ibuprofen",
                              "brandName": "Nurofen",
                              "strength": "400",
                              "strengthUnit": "mg",
                              "dosageForm": "TABLET"
                            }
                            """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.brandName").value("Nurofen"))
                .andExpect(jsonPath("$.strength").value("400"));
    }

    @Test
    void updateMedication_returnsNotFound() throws Exception {

        when(medicationService.updateMedication(eq(999L), any()))
                .thenThrow(new MedicationNotFoundException("Medication not found"));

        mockMvc.perform(put("/api/medications/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "genericName": "Ibuprofen",
                              "strength": "400",
                              "strengthUnit": "mg",
                              "dosageForm": "TABLET"
                            }
                            """))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateStatus_returnsOk() throws Exception {

        MedicationResponse response = new MedicationResponse();
        response.setId(1L);
        response.setGenericName("Ibuprofen");
        response.setActive(false);

        when(medicationService.updateStatus(1L, false))
                .thenReturn(response);

        mockMvc.perform(patch("/api/medications/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "active": false
                            }
                            """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false));
    }

    @Test
    void updateStatus_returnsNotFound() throws Exception {

        when(medicationService.updateStatus(999L, true))
                .thenThrow(new MedicationNotFoundException("Medication not found"));

        mockMvc.perform(patch("/api/medications/999/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "active": true
                            }
                            """))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateStatus_returnsBadRequest_whenActiveIsMissing() throws Exception {

        mockMvc.perform(patch("/api/medications/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                            }
                            """))
                .andExpect(status().isBadRequest());
    }
}
