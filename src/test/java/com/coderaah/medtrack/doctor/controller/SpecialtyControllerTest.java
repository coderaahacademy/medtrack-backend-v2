package com.coderaah.medtrack.doctor.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.coderaah.medtrack.common.exception.GlobalExceptionHandler;
import com.coderaah.medtrack.doctor.dto.CreateSpecialtyRequest;
import com.coderaah.medtrack.doctor.dto.SpecialtyResponse;
import com.coderaah.medtrack.doctor.exception.DuplicateSpecialtyCodeException;
import com.coderaah.medtrack.doctor.exception.DuplicateSpecialtyNameException;
import com.coderaah.medtrack.doctor.service.SpecialtyService;
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

@WebMvcTest(SpecialtyController.class)
@Import(GlobalExceptionHandler.class)
class SpecialtyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private SpecialtyService specialtyService;

    @Test
    void create_returns201WithBody() throws Exception {
        SpecialtyResponse response =
                new SpecialtyResponse(1L, "CARDIO", "Cardiology", true, LocalDateTime.now(), LocalDateTime.now());
        when(specialtyService.create(any())).thenReturn(response);

        mockMvc.perform(post("/api/specialties")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateSpecialtyRequest("CARDIO", "Cardiology"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.code").value("CARDIO"))
                .andExpect(jsonPath("$.name").value("Cardiology"))
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    void create_returns400WhenCodeBlank() throws Exception {
        mockMvc.perform(post("/api/specialties")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"\",\"name\":\"Cardiology\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_returns409WhenCodeAlreadyExists() throws Exception {
        when(specialtyService.create(any()))
                .thenThrow(new DuplicateSpecialtyCodeException("A specialty with code 'CARDIO' already exists"));

        mockMvc.perform(post("/api/specialties")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateSpecialtyRequest("CARDIO", "Cardiology"))))
                .andExpect(status().isConflict());
    }

    @Test
    void create_returns409WhenNameAlreadyExists() throws Exception {
        when(specialtyService.create(any()))
                .thenThrow(new DuplicateSpecialtyNameException("A specialty with name 'Cardiology' already exists"));

        mockMvc.perform(post("/api/specialties")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateSpecialtyRequest("CARD2", "Cardiology"))))
                .andExpect(status().isConflict());
    }

    @Test
    void findAll_returns200WithList() throws Exception {
        SpecialtyResponse response =
                new SpecialtyResponse(1L, "CARDIO", "Cardiology", true, LocalDateTime.now(), LocalDateTime.now());
        when(specialtyService.findAll()).thenReturn(List.of(response));

        mockMvc.perform(get("/api/specialties"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].code").value("CARDIO"));
    }
}