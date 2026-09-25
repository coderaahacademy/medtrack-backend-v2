package com.coderaah.medtrack.patient.controller;

import com.coderaah.medtrack.patient.domain.PatientConditionStatus;
import com.coderaah.medtrack.patient.dto.ConditionResponse;
import com.coderaah.medtrack.patient.exception.ConditionNotFoundException;
import com.coderaah.medtrack.patient.exception.PatientNotFoundException;
import com.coderaah.medtrack.patient.service.PatientConditionService;
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
public class PatientConditionControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PatientConditionService patientConditionService;

    @Test
    void createCondition_returnsCreated() throws Exception {

        ConditionResponse response = new ConditionResponse();
        response.setId(1L);
        response.setPatientId(1L);
        response.setConditionName("Diabetes");
        response.setConditionCode("E11");
        response.setStatus(PatientConditionStatus.ACTIVE);

        when(patientConditionService.createCondition(eq(1L), any()))
                .thenReturn(response);

        mockMvc.perform(post("/api/patients/1/conditions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "conditionName": "Diabetes",
                              "conditionCode": "E11",
                              "status": "ACTIVE"
                            }
                            """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.patientId").value(1))
                .andExpect(jsonPath("$.conditionName").value("Diabetes"))
                .andExpect(jsonPath("$.conditionCode").value("E11"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void createCondition_returnsBadRequest_whenConditionNameIsBlank() throws Exception {

        mockMvc.perform(post("/api/patients/1/conditions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "conditionName": ""
                            }
                            """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createCondition_returnsNotFound_whenPatientDoesNotExist() throws Exception {

        when(patientConditionService.createCondition(eq(999L), any()))
                .thenThrow(new PatientNotFoundException("Patient not found"));

        mockMvc.perform(post("/api/patients/999/conditions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "conditionName": "Diabetes"
                            }
                            """))
                .andExpect(status().isNotFound());
    }

    @Test
    void getConditions_returnsOk() throws Exception {

        ConditionResponse condition1 = new ConditionResponse();
        condition1.setId(1L);
        condition1.setConditionName("Diabetes");

        ConditionResponse condition2 = new ConditionResponse();
        condition2.setId(2L);
        condition2.setConditionName("Asthma");

        when(patientConditionService.getConditions(1L, null))
                .thenReturn(List.of(condition1, condition2));

        mockMvc.perform(get("/api/patients/1/conditions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].conditionName").value("Diabetes"))
                .andExpect(jsonPath("$[1].conditionName").value("Asthma"));
    }

    @Test
    void getConditions_returnsOnlyActive_whenStatusFilterProvided() throws Exception {

        ConditionResponse activeCondition = new ConditionResponse();
        activeCondition.setId(1L);
        activeCondition.setConditionName("Diabetes");
        activeCondition.setStatus(PatientConditionStatus.ACTIVE);

        when(patientConditionService.getConditions(1L, PatientConditionStatus.ACTIVE))
                .thenReturn(List.of(activeCondition));

        mockMvc.perform(get("/api/patients/1/conditions?status=ACTIVE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("ACTIVE"));
    }

    @Test
    void getConditions_returnsNotFound_whenPatientDoesNotExist() throws Exception {

        when(patientConditionService.getConditions(eq(999L), any()))
                .thenThrow(new PatientNotFoundException("Patient not found"));

        mockMvc.perform(get("/api/patients/999/conditions"))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateCondition_returnsOk() throws Exception {

        ConditionResponse response = new ConditionResponse();
        response.setId(10L);
        response.setConditionName("Diabetes");
        response.setNotes("Type 2, controlled with medication");

        when(patientConditionService.updateCondition(eq(1L), eq(10L), any()))
                .thenReturn(response);

        mockMvc.perform(put("/api/patients/1/conditions/10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "conditionName": "Diabetes",
                              "notes": "Type 2, controlled with medication"
                            }
                            """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.conditionName").value("Diabetes"))
                .andExpect(jsonPath("$.notes").value("Type 2, controlled with medication"));
    }

    @Test
    void updateCondition_returnsNotFound_whenConditionDoesNotBelongToPatient() throws Exception {

        when(patientConditionService.updateCondition(eq(1L), eq(999L), any()))
                .thenThrow(new ConditionNotFoundException("Condition not found"));

        mockMvc.perform(put("/api/patients/1/conditions/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "conditionName": "Diabetes"
                            }
                            """))
                .andExpect(status().isNotFound());
    }

    @Test
    void changeConditionStatus_returnsOk() throws Exception {

        ConditionResponse response = new ConditionResponse();
        response.setId(10L);
        response.setStatus(PatientConditionStatus.RESOLVED);

        when(patientConditionService.changeConditionStatus(1L, 10L, PatientConditionStatus.RESOLVED))
                .thenReturn(response);

        mockMvc.perform(patch("/api/patients/1/conditions/10/status")
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
    void changeConditionStatus_returnsBadRequest_whenStatusIsMissing() throws Exception {

        mockMvc.perform(patch("/api/patients/1/conditions/10/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {}
                            """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void changeConditionStatus_returnsNotFound_whenConditionDoesNotExist() throws Exception {

        when(patientConditionService.changeConditionStatus(1L, 999L, PatientConditionStatus.RESOLVED))
                .thenThrow(new ConditionNotFoundException("Condition not found"));

        mockMvc.perform(patch("/api/patients/1/conditions/999/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "status": "RESOLVED"
                            }
                            """))
                .andExpect(status().isNotFound());
    }
}
