package com.coderaah.medtrack.doctor.controller;

import com.coderaah.medtrack.doctor.dto.DoctorAvailabilityRuleRequest;
import com.coderaah.medtrack.doctor.dto.DoctorAvailabilityRuleResponse;
import com.coderaah.medtrack.doctor.dto.DoctorScheduleExceptionRequest;
import com.coderaah.medtrack.doctor.dto.DoctorScheduleExceptionResponse;
import com.coderaah.medtrack.doctor.service.DoctorAvailabilityService;
import com.coderaah.medtrack.doctor.service.DoctorScheduleExceptionService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/doctors/{doctorId}")
public class DoctorAvailabilityController {

    private final DoctorAvailabilityService availabilityService;
    private final DoctorScheduleExceptionService exceptionService;

    public DoctorAvailabilityController(
            DoctorAvailabilityService availabilityService,
            DoctorScheduleExceptionService exceptionService) {
        this.availabilityService = availabilityService;
        this.exceptionService = exceptionService;
    }

    @PostMapping("/availability")
    public ResponseEntity<DoctorAvailabilityRuleResponse> addAvailability(
            @PathVariable Long doctorId,
            @Valid @RequestBody DoctorAvailabilityRuleRequest request) {

        return ResponseEntity.ok(
                availabilityService.addRule(doctorId, request)
        );
    }

    @GetMapping("/availability")
    public ResponseEntity<List<DoctorAvailabilityRuleResponse>> getAvailability(
            @PathVariable Long doctorId) {

        return ResponseEntity.ok(
                availabilityService.getRules(doctorId)
        );
    }

    @PutMapping("/availability/{ruleId}")
    public ResponseEntity<DoctorAvailabilityRuleResponse> updateAvailability(
            @PathVariable Long doctorId,
            @PathVariable Long ruleId,
            @Valid @RequestBody DoctorAvailabilityRuleRequest request) {

        return ResponseEntity.ok(
                availabilityService.updateRule(
                        doctorId,
                        ruleId,
                        request
                )
        );
    }

    @DeleteMapping("/availability/{ruleId}")
    public ResponseEntity<Void> deactivateAvailability(
            @PathVariable Long doctorId,
            @PathVariable Long ruleId) {

        availabilityService.deactivateRule(doctorId, ruleId);

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/schedule-exceptions")
    public ResponseEntity<DoctorScheduleExceptionResponse> createScheduleException(
            @PathVariable Long doctorId,
            @Valid @RequestBody DoctorScheduleExceptionRequest request) {

        return ResponseEntity.ok(
                exceptionService.createException(
                        doctorId,
                        request
                )
        );
    }

    @GetMapping("/schedule-exceptions")
    public ResponseEntity<List<DoctorScheduleExceptionResponse>> getScheduleExceptions(
            @PathVariable Long doctorId) {

        return ResponseEntity.ok(
                exceptionService.getExceptions(doctorId)
        );
    }
}