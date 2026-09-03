package com.coderaah.medtrack.doctor.repository;

import com.coderaah.medtrack.doctor.domain.DoctorScheduleException;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DoctorScheduleExceptionRepository
        extends JpaRepository<DoctorScheduleException, Long> {

    List<DoctorScheduleException> findByDoctorIdOrderByStartsAtAsc(Long doctorId);

    Optional<DoctorScheduleException> findByIdAndDoctorId(Long id, Long doctorId);
}