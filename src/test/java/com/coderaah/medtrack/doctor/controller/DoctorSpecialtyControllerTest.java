package com.coderaah.medtrack.doctor.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.coderaah.medtrack.common.exception.GlobalExceptionHandler;
import com.coderaah.medtrack.doctor.dto.AssignDoctorSpecialtyRequest;
import com.coderaah.medtrack.doctor.dto.DoctorSpecialtyResponse;
import com.coderaah.medtrack.doctor.exception.DoctorNotFoundException;
import com.coderaah.medtrack.doctor.exception.DoctorSpecialtyNotFoundException;
import com.coderaah.medtrack.doctor.exception.DuplicateDoctorSpecialtyException;
import com.coderaah.medtrack.doctor.service.DoctorSpecialtyService;
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

@WebMvcTest(DoctorSpecialtyController.class)
@Import(GlobalExceptionHandler.class)
class DoctorSpecialtyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private DoctorSpecialtyService doctorSpecialtyService;

    @Test
    void assign_returns201WithBody() throws Exception {
        DoctorSpecialtyResponse response =
                new DoctorSpecialtyResponse(1L, 1L, 2L, "CARDIO", "Cardiology", true, LocalDateTime.now());
        when(doctorSpecialtyService.assign(eq(1L), any())).thenReturn(response);

        mockMvc.perform(post("/api/doctors/1/specialties")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AssignDoctorSpecialtyRequest(2L, true))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.specialtyCode").value("CARDIO"))
                .andExpect(jsonPath("$.primarySpecialty").value(true));
    }

    @Test
    void assign_returns400WhenSpecialtyIdMissing() throws Exception {
        mockMvc.perform(post("/api/doctors/1/specialties")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"primarySpecialty\":false}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void assign_returns404WhenDoctorMissing() throws Exception {
        when(doctorSpecialtyService.assign(eq(99L), any()))
                .thenThrow(new DoctorNotFoundException("Doctor 99 not found"));

        mockMvc.perform(post("/api/doctors/99/specialties")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AssignDoctorSpecialtyRequest(2L, false))))
                .andExpect(status().isNotFound());
    }

    @Test
    void assign_returns409WhenDuplicateAssignment() throws Exception {
        when(doctorSpecialtyService.assign(eq(1L), any()))
                .thenThrow(new DuplicateDoctorSpecialtyException("Doctor 1 already has specialty 2 assigned"));

        mockMvc.perform(post("/api/doctors/1/specialties")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AssignDoctorSpecialtyRequest(2L, false))))
                .andExpect(status().isConflict());
    }

    @Test
    void findByDoctor_returns200WithList() throws Exception {
        DoctorSpecialtyResponse response =
                new DoctorSpecialtyResponse(1L, 1L, 2L, "CARDIO", "Cardiology", true, LocalDateTime.now());
        when(doctorSpecialtyService.findByDoctor(1L)).thenReturn(List.of(response));

        mockMvc.perform(get("/api/doctors/1/specialties"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].specialtyCode").value("CARDIO"));
    }

    @Test
    void remove_returns204() throws Exception {
        mockMvc.perform(delete("/api/doctors/1/specialties/2"))
                .andExpect(status().isNoContent());
    }

    @Test
    void remove_returns404WhenAssignmentMissing() throws Exception {
        doThrow(new DoctorSpecialtyNotFoundException("Doctor 1 has no assignment for specialty 2"))
                .when(doctorSpecialtyService).remove(1L, 2L);

        mockMvc.perform(delete("/api/doctors/1/specialties/2"))
                .andExpect(status().isNotFound());
    }
}