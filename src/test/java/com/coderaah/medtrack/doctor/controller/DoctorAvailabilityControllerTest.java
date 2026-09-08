package com.coderaah.medtrack.doctor.controller;

import com.coderaah.medtrack.doctor.domain.ScheduleExceptionReason;
import com.coderaah.medtrack.doctor.domain.ScheduleExceptionType;
import com.coderaah.medtrack.doctor.dto.DoctorAvailabilityRuleRequest;
import com.coderaah.medtrack.doctor.dto.DoctorAvailabilityRuleResponse;
import com.coderaah.medtrack.doctor.dto.DoctorScheduleExceptionRequest;
import com.coderaah.medtrack.doctor.dto.DoctorScheduleExceptionResponse;
import com.coderaah.medtrack.doctor.exception.AvailabilityRuleNotFoundException;
import com.coderaah.medtrack.doctor.exception.CannotCancelPastScheduleException;
import com.coderaah.medtrack.doctor.exception.InvalidScheduleTimeRangeException;
import com.coderaah.medtrack.doctor.exception.ScheduleExceptionNotFoundException;
import com.coderaah.medtrack.doctor.service.DoctorAvailabilityService;
import com.coderaah.medtrack.doctor.service.DoctorScheduleExceptionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DoctorAvailabilityController.class)
class DoctorAvailabilityControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private DoctorAvailabilityService availabilityService;

    @MockBean
    private DoctorScheduleExceptionService exceptionService;

    @Test
    void shouldCreateAvailabilityAndReturn201() throws Exception {

        DoctorAvailabilityRuleRequest request =
                new DoctorAvailabilityRuleRequest(
                        DayOfWeek.MONDAY,
                        LocalTime.of(9, 0),
                        LocalTime.of(13, 0)
                );

        DoctorAvailabilityRuleResponse response =
                new DoctorAvailabilityRuleResponse(
                        10L,
                        1L,
                        DayOfWeek.MONDAY,
                        LocalTime.of(9, 0),
                        LocalTime.of(13, 0),
                        true
                );

        when(availabilityService.addRule(eq(1L), any()))
                .thenReturn(response);

        mockMvc.perform(
                        post("/api/doctors/1/availability")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.doctorId").value(1))
                .andExpect(jsonPath("$.dayOfWeek").value("MONDAY"))
                .andExpect(jsonPath("$.startTime").value("09:00:00"))
                .andExpect(jsonPath("$.endTime").value("13:00:00"))
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    void shouldGetAvailabilityAndReturn200() throws Exception {

        DoctorAvailabilityRuleResponse response =
                new DoctorAvailabilityRuleResponse(
                        10L,
                        1L,
                        DayOfWeek.MONDAY,
                        LocalTime.of(9, 0),
                        LocalTime.of(13, 0),
                        true
                );

        when(availabilityService.getRules(1L))
                .thenReturn(List.of(response));

        mockMvc.perform(
                        get("/api/doctors/1/availability")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(10))
                .andExpect(jsonPath("$[0].doctorId").value(1))
                .andExpect(jsonPath("$[0].dayOfWeek").value("MONDAY"))
                .andExpect(jsonPath("$[0].active").value(true));
    }

    @Test
    void shouldUpdateAvailabilityAndReturn200() throws Exception {

        DoctorAvailabilityRuleRequest request =
                new DoctorAvailabilityRuleRequest(
                        DayOfWeek.TUESDAY,
                        LocalTime.of(10, 0),
                        LocalTime.of(16, 0)
                );

        DoctorAvailabilityRuleResponse response =
                new DoctorAvailabilityRuleResponse(
                        10L,
                        1L,
                        DayOfWeek.TUESDAY,
                        LocalTime.of(10, 0),
                        LocalTime.of(16, 0),
                        true
                );

        when(availabilityService.updateRule(eq(1L), eq(10L), any()))
                .thenReturn(response);

        mockMvc.perform(
                        put("/api/doctors/1/availability/10")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.dayOfWeek").value("TUESDAY"))
                .andExpect(jsonPath("$.startTime").value("10:00:00"))
                .andExpect(jsonPath("$.endTime").value("16:00:00"));
    }

    @Test
    void shouldDeactivateAvailabilityAndReturn204() throws Exception {

        mockMvc.perform(
                        delete("/api/doctors/1/availability/10")
                )
                .andExpect(status().isNoContent());

        verify(availabilityService)
                .deactivateRule(1L, 10L);
    }

    @Test
    void shouldCreateScheduleExceptionAndReturn201() throws Exception {

        LocalDateTime startsAt =
                LocalDateTime.of(2026, 9, 10, 9, 0);

        LocalDateTime endsAt =
                LocalDateTime.of(2026, 9, 10, 13, 0);

        DoctorScheduleExceptionRequest request =
                new DoctorScheduleExceptionRequest(
                        startsAt,
                        endsAt,
                        ScheduleExceptionType.UNAVAILABLE,
                        ScheduleExceptionReason.VACATION,
                        "Annual vacation"
                );

        DoctorScheduleExceptionResponse response =
                new DoctorScheduleExceptionResponse(
                        20L,
                        1L,
                        startsAt,
                        endsAt,
                        ScheduleExceptionType.UNAVAILABLE,
                        ScheduleExceptionReason.VACATION,
                        "Annual vacation"
                );

        when(exceptionService.createException(eq(1L), any()))
                .thenReturn(response);

        mockMvc.perform(
                        post("/api/doctors/1/schedule-exceptions")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(20))
                .andExpect(jsonPath("$.doctorId").value(1))
                .andExpect(jsonPath("$.exceptionType").value("UNAVAILABLE"))
                .andExpect(jsonPath("$.reasonType").value("VACATION"))
                .andExpect(jsonPath("$.reason").value("Annual vacation"));
    }

    @Test
    void shouldGetScheduleExceptionsAndReturn200() throws Exception {

        LocalDateTime startsAt =
                LocalDateTime.of(2026, 9, 10, 9, 0);

        LocalDateTime endsAt =
                LocalDateTime.of(2026, 9, 10, 13, 0);

        DoctorScheduleExceptionResponse response =
                new DoctorScheduleExceptionResponse(
                        20L,
                        1L,
                        startsAt,
                        endsAt,
                        ScheduleExceptionType.UNAVAILABLE,
                        ScheduleExceptionReason.SICK_LEAVE,
                        "Sick leave"
                );

        when(exceptionService.getExceptions(1L))
                .thenReturn(List.of(response));

        mockMvc.perform(
                        get("/api/doctors/1/schedule-exceptions")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(20))
                .andExpect(jsonPath("$[0].doctorId").value(1))
                .andExpect(jsonPath("$[0].exceptionType").value("UNAVAILABLE"))
                .andExpect(jsonPath("$[0].reasonType").value("SICK_LEAVE"))
                .andExpect(jsonPath("$[0].reason").value("Sick leave"));
    }

    @Test
    void shouldDeleteScheduleExceptionAndReturn204() throws Exception {

        mockMvc.perform(
                        delete("/api/doctors/1/schedule-exceptions/20")
                )
                .andExpect(status().isNoContent());

        verify(exceptionService)
                .cancelException(1L, 20L);
    }

    @Test
    void shouldRejectInvalidAvailabilityTimeRangeAndReturn400()
            throws Exception {

        DoctorAvailabilityRuleRequest request =
                new DoctorAvailabilityRuleRequest(
                        DayOfWeek.MONDAY,
                        LocalTime.of(14, 0),
                        LocalTime.of(9, 0)
                );

        when(availabilityService.addRule(eq(1L), any()))
                .thenThrow(new InvalidScheduleTimeRangeException(
                        "startTime must be before endTime"
                ));

        mockMvc.perform(
                        post("/api/doctors/1/availability")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn404WhenAvailabilityRuleIsNotFound()
            throws Exception {

        DoctorAvailabilityRuleRequest request =
                new DoctorAvailabilityRuleRequest(
                        DayOfWeek.MONDAY,
                        LocalTime.of(9, 0),
                        LocalTime.of(13, 0)
                );

        when(availabilityService.updateRule(eq(1L), eq(999L), any()))
                .thenThrow(new AvailabilityRuleNotFoundException(
                        "Availability rule not found"
                ));

        mockMvc.perform(
                        put("/api/doctors/1/availability/999")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturn404WhenScheduleExceptionIsNotFound()
            throws Exception {

        when(exceptionService.getExceptions(999L))
                .thenThrow(new ScheduleExceptionNotFoundException(
                        "Schedule exception not found"
                ));

        mockMvc.perform(
                        get("/api/doctors/999/schedule-exceptions")
                )
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturn400WhenCancellingPastScheduleException()
            throws Exception {

        doThrow(new CannotCancelPastScheduleException(
                "Only future schedule exceptions can be cancelled"
        ))
                .when(exceptionService)
                .cancelException(1L, 20L);

        mockMvc.perform(
                        delete("/api/doctors/1/schedule-exceptions/20")
                )
                .andExpect(status().isBadRequest());
    }
}