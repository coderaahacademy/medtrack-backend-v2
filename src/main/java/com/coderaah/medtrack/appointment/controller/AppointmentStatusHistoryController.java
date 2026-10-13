package com.coderaah.medtrack.appointment.controller;

import com.coderaah.medtrack.appointment.dto.AppointmentStatusHistoryResponse;
import com.coderaah.medtrack.appointment.service.AppointmentStatusHistoryService;
import org.springframework.http.ResponseEntity;
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


    @GetMapping("/{appointmentId}/history")
    public ResponseEntity<List<AppointmentStatusHistoryResponse>> getHistory(@PathVariable Long appointmentId) {
        return ResponseEntity.ok(historyService.getHistoryForAppointment(appointmentId));
    }
}
