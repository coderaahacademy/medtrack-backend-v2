package com.coderaah.medtrack.appointment.controller;

import com.coderaah.medtrack.appointment.domain.AppointmentType;
import com.coderaah.medtrack.appointment.dto.requestDto.AppointmentRequestDto;
import com.coderaah.medtrack.appointment.dto.responeDto.AppointmentResponseDto;
import com.coderaah.medtrack.appointment.service.AppointmentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(AppointmentController.class)
class AppointmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AppointmentService appointmentService;

    // POST /api/appointments


    @Test
    void create_returns201_whenRequestIsValid() throws Exception {
        AppointmentResponseDto response = new AppointmentResponseDto();
        response.setId(1L);
        when(appointmentService.createAppointment(any())).thenReturn(response);

        mockMvc.perform(post("/api/appointments")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(validRequest())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void create_returns400_whenReasonIsBlank() throws Exception {
        AppointmentRequestDto request = validRequest();
        request.setReason("");

        mockMvc.perform(post("/api/appointments")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    // GET /api/appointments/{id}


    @Test
    void getById_returns200_whenFound() throws Exception {
        AppointmentResponseDto response = new AppointmentResponseDto();
        response.setId(5L);
        when(appointmentService.getById(5L)).thenReturn(response);

        mockMvc.perform(get("/api/appointments/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5));
    }

    @Test
    void getById_returns404_whenNotFound() throws Exception {
        when(appointmentService.getById(999L))
                .thenThrow(new EntityNotFoundException("Appointment not found: 999"));

        mockMvc.perform(get("/api/appointments/999"))
                .andExpect(status().isNotFound());
    }

    // PATCH /api/appointments/{id}/confirm


    @Test
    void confirm_returns200() throws Exception {
        AppointmentResponseDto response = new AppointmentResponseDto();
        response.setId(7L);
        when(appointmentService.confirm(7L, 1L)).thenReturn(response);

        mockMvc.perform(patch("/api/appointments/7/confirm").param("actorUserId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(7));
    }


    @Test
    void confirm_returns409_whenTransitionIsInvalid() throws Exception {
        when(appointmentService.confirm(anyLong(), anyLong()))
                .thenThrow(new IllegalStateException("Invalid status transition: COMPLETED -> CONFIRMED"));

        mockMvc.perform(patch("/api/appointments/7/confirm").param("actorUserId", "1"))
                .andExpect(status().isConflict());
    }

    private AppointmentRequestDto validRequest() {
        AppointmentRequestDto dto = new AppointmentRequestDto();
        dto.setPatientId(1L);
        dto.setDoctorId(2L);
        dto.setLocation("Room 1");
        dto.setReason("a".repeat(100)); // چون @Size(min = 100) روی reason هست
        dto.setAppointmentType(AppointmentType.IN_PERSON);
        dto.setScheduledStart(LocalDateTime.now().plusDays(1));
        dto.setScheduledEnd(LocalDateTime.now().plusDays(1).plusHours(1));
        return dto;
    }
}
