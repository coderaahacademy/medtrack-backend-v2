package com.coderaah.medtrack.pharmacy.controller;

import com.coderaah.medtrack.pharmacy.dto.PharmacyResponse;
import com.coderaah.medtrack.pharmacy.exception.DuplicatePharmacyLicenseNumberException;
import com.coderaah.medtrack.pharmacy.exception.PharmacyNotFoundException;
import com.coderaah.medtrack.pharmacy.service.PharmacyService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PharmacyController.class)
public class PharmacyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PharmacyService pharmacyService;

    private static final String VALID_JSON = """
            {
              "name": "Apotheke Nord",
              "licenseNumber": "L-123",
              "city": "Hamburg"
            }
            """;

    private PharmacyResponse sampleResponse(boolean active) {
        PharmacyResponse response = new PharmacyResponse();
        response.setId(1L);
        response.setName("Apotheke Nord");
        response.setLicenseNumber("L-123");
        response.setCity("Hamburg");
        response.setActive(active);
        return response;
    }

    @Test
    void createPharmacy_returnsCreated() throws Exception {

        when(pharmacyService.createPharmacy(any())).thenReturn(sampleResponse(true));

        mockMvc.perform(post("/api/pharmacies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Apotheke Nord"))
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    void createPharmacy_returnsBadRequest_whenNameIsBlank() throws Exception {

        mockMvc.perform(post("/api/pharmacies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "name": "", "licenseNumber": "L-123" }
                                """))
                .andExpect(status().isBadRequest());

        verify(pharmacyService, never()).createPharmacy(any());
    }

    @Test
    void createPharmacy_returnsConflict_whenLicenseNumberExists() throws Exception {

        when(pharmacyService.createPharmacy(any()))
                .thenThrow(new DuplicatePharmacyLicenseNumberException("duplicate"));

        mockMvc.perform(post("/api/pharmacies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_JSON))
                .andExpect(status().isConflict());
    }

    @Test
    void listPharmacies_returnsOk() throws Exception {

        when(pharmacyService.listPharmacies()).thenReturn(List.of(sampleResponse(true)));

        mockMvc.perform(get("/api/pharmacies"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Apotheke Nord"));
    }

    @Test
    void getPharmacyById_returnsNotFound_whenMissing() throws Exception {

        when(pharmacyService.getPharmacyById(999L))
                .thenThrow(new PharmacyNotFoundException("not found"));

        mockMvc.perform(get("/api/pharmacies/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void updatePharmacy_returnsOk() throws Exception {

        when(pharmacyService.updatePharmacy(eq(1L), any())).thenReturn(sampleResponse(true));

        mockMvc.perform(put("/api/pharmacies/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.city").value("Hamburg"));
    }

    @Test
    void updatePharmacyStatus_returnsOk() throws Exception {

        when(pharmacyService.updatePharmacyStatus(1L, false)).thenReturn(sampleResponse(false));

        mockMvc.perform(patch("/api/pharmacies/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "active": false }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false));
    }

    @Test
    void updatePharmacyStatus_returnsBadRequest_whenActiveMissing() throws Exception {

        mockMvc.perform(patch("/api/pharmacies/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());

        verify(pharmacyService, never()).updatePharmacyStatus(any(), any(Boolean.class));
    }
}