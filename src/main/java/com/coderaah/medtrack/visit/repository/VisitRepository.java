package com.coderaah.medtrack.visit.repository;

import com.coderaah.medtrack.visit.domain.Visit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface VisitRepository extends JpaRepository<Visit, Long> {

    List<Visit> findByPatientIdOrderByStartedAtDesc(Long patientId);

    List<Visit> findByDoctorIdOrderByStartedAtDesc(Long doctorId);

    Optional<Visit> findByAppointmentId(Long appointmentId);

    boolean existsByAppointmentId(Long appointmentId);
}