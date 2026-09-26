package com.coderaah.medtrack.appointment.controller;

import com.coderaah.medtrack.appointment.dto.requestDto.AppointmentRequestDto;
import com.coderaah.medtrack.appointment.dto.responeDto.AppointmentResponseDto;
import com.coderaah.medtrack.appointment.service.AppointmentService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api")
public class AppointmentController {

    private final AppointmentService appointmentService;

    public AppointmentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    //create
    // POST /api/appointments
    @PostMapping("/appointments")
    @ResponseStatus(HttpStatus.CREATED)
    public AppointmentResponseDto create(@Valid @RequestBody AppointmentRequestDto dto) {
        return appointmentService.createAppointment(dto);
    }

    // query

    // GET /api/appointments/{id}
    @GetMapping("/appointments/{id}")
    public AppointmentResponseDto getById(@PathVariable Long id) {
        return appointmentService.getById(id);
    }


    // GET /api/patients/{patientId}/appointments
    @GetMapping("/patients/{patientId}/appointments")
    public List<AppointmentResponseDto> getByPatient(@PathVariable Long patientId) {
        return appointmentService.getByPatient(patientId);
    }

    // GET /api/doctors/{doctorId}/appointments
    // GET /api/doctors/{doctorId}/appointments?date=YYYY-MM-DD
    @GetMapping("/doctors/{doctorId}/appointments")
    public List<AppointmentResponseDto> getByDoctor(
            @PathVariable Long doctorId,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        if (date != null) {
            return appointmentService.getByDoctorAndDate(doctorId, date);
        }
        return appointmentService.getByDoctor(doctorId);
    }

    // lifcycle

    // PATCH /api/appointments/{id}/confirm?actorUserId=1
    @PatchMapping("/appointments/{id}/confirm")
    public AppointmentResponseDto confirm(@PathVariable Long id,
                                          @RequestParam Long actorUserId) {
        return appointmentService.confirm(id, actorUserId);
    }

    // PATCH /api/appointments/{id}/cancel?actorUserId=1&reason=...
    @PatchMapping("/appointments/{id}/cancel")
    public AppointmentResponseDto cancel(@PathVariable Long id,
                                         @RequestParam Long actorUserId,
                                         @RequestParam String reason) {
        return appointmentService.cancel(id, actorUserId, reason);
    }

    // PATCH /api/appointments/{id}/complete?actorUserId=1
    @PatchMapping("/appointments/{id}/complete")
    public AppointmentResponseDto complete(@PathVariable Long id,
                                           @RequestParam Long actorUserId) {
        return appointmentService.complete(id, actorUserId);
    }

    // PATCH /api/appointments/{id}/no-show?actorUserId=1
    @PatchMapping("/appointments/{id}/no-show")
    public AppointmentResponseDto noShow(@PathVariable Long id,
                                         @RequestParam Long actorUserId) {
        return appointmentService.noShow(id, actorUserId);
    }
}
