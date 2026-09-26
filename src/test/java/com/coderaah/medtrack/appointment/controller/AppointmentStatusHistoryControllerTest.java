package com.coderaah.medtrack.appointment.controller;

import com.coderaah.medtrack.appointment.dto.responeDto.AppointmentStatusHistoryResponeDto;
import com.coderaah.medtrack.appointment.service.AppointmentStatusHistoryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AppointmentStatusHistoryController.class)
class AppointmentStatusHistoryControllerTest {

    @Autowired
    private MockMvc mockMvc;


    @MockBean
    private AppointmentStatusHistoryService historyService;

    @Test
    void getHistory_returns200_withList() throws Exception {
        AppointmentStatusHistoryResponeDto dto = new AppointmentStatusHistoryResponeDto();
        dto.setId(1L);
        when(historyService.getHistoryForAppointment(7L)).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/appointments/7/history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void getHistory_returns200_withEmptyList_whenNoHistory() throws Exception {
        when(historyService.getHistoryForAppointment(8L)).thenReturn(List.of());

        mockMvc.perform(get("/api/appointments/8/history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }
}
