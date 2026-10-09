package com.coderaah.medtrack.visit.repository;

import com.coderaah.medtrack.visit.domain.Visit;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VisitRepository extends JpaRepository<Visit, Long> {

    List<Visit> findByPatient_IdOrderByStartedAtDesc(Long patientId);

    List<Visit> findByDoctor_IdOrderByStartedAtDesc(Long doctorId);

    Optional<Visit> findByAppointment_Id(Long appointmentId);

    boolean existsByAppointment_Id(Long appointmentId);
}