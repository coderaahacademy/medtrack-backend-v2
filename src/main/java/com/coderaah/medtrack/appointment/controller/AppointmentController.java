package com.coderaah.medtrack.appointment.controller;

import com.coderaah.medtrack.appointment.dto.AppointmentRequest;
import com.coderaah.medtrack.appointment.dto.AppointmentResponse;
import com.coderaah.medtrack.appointment.dto.CancelAppointmentRequest;
import com.coderaah.medtrack.appointment.service.AppointmentService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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


    @PostMapping("/appointments")
    public ResponseEntity<AppointmentResponse> create(@Valid @RequestBody AppointmentRequest request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(appointmentService.createAppointment(request));
    }

    @GetMapping("/appointments/{id}")
    public ResponseEntity<AppointmentResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(appointmentService.getById(id));
    }

    @GetMapping("/patients/{patientId}/appointments")
    public ResponseEntity<List<AppointmentResponse>> getByPatient(@PathVariable Long patientId) {
        return ResponseEntity.ok(appointmentService.getByPatient(patientId));
    }

    @GetMapping("/doctors/{doctorId}/appointments")
    public ResponseEntity<List<AppointmentResponse>> getByDoctor(
            @PathVariable Long doctorId,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        if (date != null) {
            return ResponseEntity.ok(appointmentService.getByDoctorAndDate(doctorId, date));
        }
        return ResponseEntity.ok(appointmentService.getByDoctor(doctorId));
    }

    // actorUserId is a temporary stand-in until authentication provides the acting user
    @PatchMapping("/appointments/{id}/confirm")
    public ResponseEntity<AppointmentResponse> confirm(@PathVariable Long id,
                                                       @RequestParam Long actorUserId) {
        return ResponseEntity.ok(appointmentService.confirm(id, actorUserId));
    }

    @PatchMapping("/appointments/{id}/cancel")
    public ResponseEntity<AppointmentResponse> cancel(@PathVariable Long id,
                                                      @RequestParam Long actorUserId,
                                                      @Valid @RequestBody CancelAppointmentRequest request) {
        return ResponseEntity.ok(appointmentService.cancel(id, actorUserId, request.getReason()));
    }

    @PatchMapping("/appointments/{id}/complete")
    public ResponseEntity<AppointmentResponse> complete(@PathVariable Long id,
                                                        @RequestParam Long actorUserId) {
        return ResponseEntity.ok(appointmentService.complete(id, actorUserId));
    }

    @PatchMapping("/appointments/{id}/no-show")
    public ResponseEntity<AppointmentResponse> noShow(@PathVariable Long id,
                                                      @RequestParam Long actorUserId) {
        return ResponseEntity.ok(appointmentService.noShow(id, actorUserId));
    }
}
