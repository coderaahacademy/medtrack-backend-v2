package com.coderaah.medtrack.appointment.controller;

import com.coderaah.medtrack.appointment.domain.AppointmentStatus;
import com.coderaah.medtrack.appointment.dto.AppointmentStatusHistoryResponse;
import com.coderaah.medtrack.appointment.service.AppointmentStatusHistoryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import com.coderaah.medtrack.common.config.SecurityConfig;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AppointmentStatusHistoryController.class)
@Import(SecurityConfig.class)
class AppointmentStatusHistoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AppointmentStatusHistoryService historyService;
    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Test
    void getHistory_returnsOk_withHistoryRecords() throws Exception {
        AppointmentStatusHistoryResponse record = new AppointmentStatusHistoryResponse();
        record.setId(1L);
        record.setAppointmentId(7L);
        record.setOldStatus(AppointmentStatus.SCHEDULED);
        record.setNewStatus(AppointmentStatus.CONFIRMED);
        record.setChangedByUserId(9L);
        when(historyService.getHistoryForAppointment(7L)).thenReturn(List.of(record));

        mockMvc.perform(get("/api/appointments/7/history") .with(jwt()
                        .jwt(token -> token.subject("9"))
                        .authorities(
                                new SimpleGrantedAuthority("ROLE_DOCTOR"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].appointmentId").value(7))
                .andExpect(jsonPath("$[0].oldStatus").value("SCHEDULED"))
                .andExpect(jsonPath("$[0].newStatus").value("CONFIRMED"))
                .andExpect(jsonPath("$[0].changedByUserId").value(9));
    }

    @Test
    void getHistory_returnsOk_withEmptyArray_whenNoHistory() throws Exception {
        when(historyService.getHistoryForAppointment(8L)).thenReturn(List.of());

        mockMvc.perform(get("/api/appointments/8/history")
                                .with(jwt()
                                        .jwt(token -> token.subject("9"))
                                        .authorities(
                                                new SimpleGrantedAuthority("ROLE_DOCTOR"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }
}
