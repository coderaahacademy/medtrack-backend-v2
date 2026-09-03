package com.coderaah.medtrack.appointment.repository;

import com.coderaah.medtrack.appointment.domain.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
}
