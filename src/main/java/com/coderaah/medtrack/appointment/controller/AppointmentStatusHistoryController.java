package com.coderaah.medtrack.appointment.controller;

import com.coderaah.medtrack.appointment.dto.responeDto.AppointmentStatusHistoryResponeDto;
import com.coderaah.medtrack.appointment.service.AppointmentStatusHistoryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentStatusHistoryController {

    private final AppointmentStatusHistoryService historyService;

    public AppointmentStatusHistoryController(AppointmentStatusHistoryService historyService) {
        this.historyService = historyService;
    }


    // GET /api/appointments/{appointmentId}/history
    @GetMapping("/{appointmentId}/history")
    public List<AppointmentStatusHistoryResponeDto> getHistory(@PathVariable Long appointmentId) {
        return historyService.getHistoryForAppointment(appointmentId);
    }
}
