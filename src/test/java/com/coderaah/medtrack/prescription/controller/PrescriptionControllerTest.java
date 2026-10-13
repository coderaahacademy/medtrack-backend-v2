package com.coderaah.medtrack.prescription.controller;

import com.coderaah.medtrack.prescription.dto.AddPrescriptionItemRequest;
import com.coderaah.medtrack.prescription.dto.CancelPrescriptionRequest;
import com.coderaah.medtrack.prescription.dto.CreatePrescriptionRequest;
import com.coderaah.medtrack.prescription.dto.PrescriptionItemResponse;
import com.coderaah.medtrack.prescription.dto.PrescriptionResponse;
import com.coderaah.medtrack.prescription.dto.PrescriptionStatusHistoryResponse;
import com.coderaah.medtrack.prescription.service.PrescriptionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PrescriptionController.class)
class PrescriptionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PrescriptionService prescriptionService;

    private PrescriptionResponse prescriptionResponse() {
        PrescriptionResponse response = new PrescriptionResponse();
        response.setId(1L);
        return response;
    }

    @Test
    void createPrescription_returnsCreatedAndLocation() throws Exception {
        when(prescriptionService.createPrescription(
                eq(4L), any(CreatePrescriptionRequest.class), eq(9L)))
                .thenReturn(prescriptionResponse());

        mockMvc.perform(post("/api/visits/4/prescriptions")
                        .param("actorUserId", "9")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isCreated())
                .andExpect(header().string(
                        "Location", "/api/prescriptions/1"))
                .andExpect(jsonPath("$.id").value(1));

        verify(prescriptionService).createPrescription(
                eq(4L), any(CreatePrescriptionRequest.class), eq(9L));
    }

    @Test
    void createPrescription_returnsBadRequestWhenActorIsMissing()
            throws Exception {
        mockMvc.perform(post("/api/visits/4/prescriptions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addItem_returnsCreated() throws Exception {
        when(prescriptionService.addItem(
                eq(1L), any(AddPrescriptionItemRequest.class)))
                .thenReturn(new PrescriptionItemResponse());

        mockMvc.perform(post("/api/prescriptions/1/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "medicationId": 2,
                              "dosage": "500 mg",
                              "frequency": "Twice daily",
                              "route": "Oral",
                              "durationDays": 5,
                              "quantityPrescribed": 10,
                              "instructions": "Take after meals"
                            }
                            """))
                .andExpect(status().isCreated());

        verify(prescriptionService).addItem(
                eq(1L), any(AddPrescriptionItemRequest.class));
    }

    @Test
    void addItem_returnsBadRequestWhenDosageIsBlank() throws Exception {
        mockMvc.perform(post("/api/prescriptions/1/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "medicationId": 2,
                              "dosage": " ",
                              "frequency": "Twice daily",
                              "quantityPrescribed": 10
                            }
                            """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addItem_returnsBadRequestWhenQuantityIsNotPositive()
            throws Exception {
        mockMvc.perform(post("/api/prescriptions/1/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "medicationId": 2,
                              "dosage": "500 mg",
                              "frequency": "Twice daily",
                              "quantityPrescribed": 0
                            }
                            """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateItem_returnsOk() throws Exception {
        when(prescriptionService.updateItem(
                eq(1L), eq(3L), any(AddPrescriptionItemRequest.class)))
                .thenReturn(new PrescriptionItemResponse());

        mockMvc.perform(put("/api/prescriptions/1/items/3")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "medicationId": 2,
                              "dosage": "250 mg",
                              "frequency": "Once daily",
                              "quantityPrescribed": 5
                            }
                            """))
                .andExpect(status().isOk());

        verify(prescriptionService).updateItem(
                eq(1L), eq(3L), any(AddPrescriptionItemRequest.class));
    }

    @Test
    void removeItem_returnsNoContent() throws Exception {
        mockMvc.perform(delete("/api/prescriptions/1/items/3"))
                .andExpect(status().isNoContent());

        verify(prescriptionService).removeItem(1L, 3L);
    }

    @Test
    void issue_returnsOk() throws Exception {
        when(prescriptionService.issue(1L, 9L))
                .thenReturn(prescriptionResponse());

        mockMvc.perform(patch("/api/prescriptions/1/issue")
                        .param("actorUserId", "9"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));

        verify(prescriptionService).issue(1L, 9L);
    }

    @Test
    void issue_returnsBadRequestWhenActorIsMissing() throws Exception {
        mockMvc.perform(patch("/api/prescriptions/1/issue"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void cancel_returnsOk() throws Exception {
        when(prescriptionService.cancel(
                eq(1L), any(CancelPrescriptionRequest.class), eq(9L)))
                .thenReturn(prescriptionResponse());

        mockMvc.perform(patch("/api/prescriptions/1/cancel")
                        .param("actorUserId", "9")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "cancellationReason": "Patient requested cancellation"
                            }
                            """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));

        verify(prescriptionService).cancel(
                eq(1L), any(CancelPrescriptionRequest.class), eq(9L));
    }

    @Test
    void cancel_returnsBadRequestWhenReasonIsBlank() throws Exception {
        mockMvc.perform(patch("/api/prescriptions/1/cancel")
                        .param("actorUserId", "9")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "cancellationReason": " "
                            }
                            """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getPrescription_returnsOk() throws Exception {
        when(prescriptionService.getPrescription(1L))
                .thenReturn(prescriptionResponse());

        mockMvc.perform(get("/api/prescriptions/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void getPrescriptionsForVisit_returnsOk() throws Exception {
        when(prescriptionService.getPrescriptionsForVisit(4L))
                .thenReturn(List.of(prescriptionResponse()));

        mockMvc.perform(get("/api/visits/4/prescriptions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void getPrescriptionsForPatient_returnsOk() throws Exception {
        when(prescriptionService.getPrescriptionsForPatient(7L))
                .thenReturn(List.of(prescriptionResponse()));

        mockMvc.perform(get("/api/patients/7/prescriptions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void getHistory_returnsOk() throws Exception {
        when(prescriptionService.getHistory(1L))
                .thenReturn(List.of(new PrescriptionStatusHistoryResponse()));

        mockMvc.perform(get("/api/prescriptions/1/history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1));
    }
}
