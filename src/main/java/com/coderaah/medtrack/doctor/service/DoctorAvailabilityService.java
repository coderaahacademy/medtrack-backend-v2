package com.coderaah.medtrack.doctor.service;

import com.coderaah.medtrack.doctor.domain.DoctorAvailabilityRule;
import com.coderaah.medtrack.doctor.domain.DoctorProfile;
import com.coderaah.medtrack.doctor.dto.DoctorAvailabilityRuleRequest;
import com.coderaah.medtrack.doctor.dto.DoctorAvailabilityRuleResponse;
import com.coderaah.medtrack.doctor.repository.DoctorAvailabilityRuleRepository;
import com.coderaah.medtrack.doctor.repository.DoctorProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class DoctorAvailabilityService {

    private final DoctorAvailabilityRuleRepository availabilityRuleRepository;
    private final DoctorProfileRepository doctorProfileRepository;

    public DoctorAvailabilityService(
            DoctorAvailabilityRuleRepository availabilityRuleRepository,
            DoctorProfileRepository doctorProfileRepository) {
        this.availabilityRuleRepository = availabilityRuleRepository;
        this.doctorProfileRepository = doctorProfileRepository;
    }

    public DoctorAvailabilityRuleResponse addRule(
            Long doctorId,
            DoctorAvailabilityRuleRequest request) {

        validateTimeRange(request);

        DoctorProfile doctor = getDoctor(doctorId);

        DoctorAvailabilityRule rule = new DoctorAvailabilityRule(
                doctor,
                request.dayOfWeek(),
                request.startTime(),
                request.endTime()
        );

        DoctorAvailabilityRule saved =
                availabilityRuleRepository.save(rule);

        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<DoctorAvailabilityRuleResponse> getRules(Long doctorId) {

        getDoctor(doctorId);

        return availabilityRuleRepository
                .findByDoctorIdOrderByDayOfWeekAscStartTimeAsc(doctorId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public DoctorAvailabilityRuleResponse updateRule(
            Long doctorId,
            Long ruleId,
            DoctorAvailabilityRuleRequest request) {

        validateTimeRange(request);

        getDoctor(doctorId);

        DoctorAvailabilityRule rule =
                availabilityRuleRepository
                        .findByIdAndDoctorId(ruleId, doctorId)
                        .orElseThrow(() ->
                                new RuntimeException("Availability rule not found"));

        rule.setDayOfWeek(request.dayOfWeek());
        rule.setStartTime(request.startTime());
        rule.setEndTime(request.endTime());

        return toResponse(
                availabilityRuleRepository.save(rule)
        );
    }

    public void deactivateRule(Long doctorId, Long ruleId) {

        getDoctor(doctorId);

        DoctorAvailabilityRule rule =
                availabilityRuleRepository
                        .findByIdAndDoctorId(ruleId, doctorId)
                        .orElseThrow(() ->
                                new RuntimeException("Availability rule not found"));

        rule.setActive(false);

        availabilityRuleRepository.save(rule);
    }

    private DoctorProfile getDoctor(Long doctorId) {
        return doctorProfileRepository.findById(doctorId)
                .orElseThrow(() ->
                        new RuntimeException("Doctor not found"));
    }

    private void validateTimeRange(
            DoctorAvailabilityRuleRequest request) {

        if (!request.startTime().isBefore(request.endTime())) {
            throw new IllegalArgumentException(
                    "startTime must be before endTime");
        }
    }

    private DoctorAvailabilityRuleResponse toResponse(
            DoctorAvailabilityRule rule) {
        return new DoctorAvailabilityRuleResponse(
                rule.getId(),
                rule.getDoctor().getId(),
                rule.getDayOfWeek(),
                rule.getStartTime(),
                rule.getEndTime(),
                rule.isActive()
        );
    }
}