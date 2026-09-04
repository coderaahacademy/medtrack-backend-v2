package com.coderaah.medtrack.doctor.repository;

import com.coderaah.medtrack.doctor.domain.DoctorAvailabilityRule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DoctorAvailabilityRuleRepository
        extends JpaRepository<DoctorAvailabilityRule, Long> {

    List<DoctorAvailabilityRule> findByDoctorIdOrderByDayOfWeekAscStartTimeAsc(Long doctorId);

    List<DoctorAvailabilityRule> findByDoctorIdAndActiveTrueOrderByDayOfWeekAscStartTimeAsc(Long doctorId);

    Optional<DoctorAvailabilityRule> findByIdAndDoctorId(Long id, Long doctorId);
}