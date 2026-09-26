package com.coderaah.medtrack.appointment.repository;

import com.coderaah.medtrack.appointment.domain.Appointment;
import com.coderaah.medtrack.appointment.domain.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    List<Appointment> findByPatientId(Long patientId);

    List<Appointment> findByDoctorId(Long doctorId);

    List<Appointment> findByDoctorIdAndScheduledStartBetween(
            Long doctorId, LocalDateTime start, LocalDateTime end);


    List<Appointment> findByDoctorIdAndStatusIn(
            Long doctorId, List<AppointmentStatus> statuses);

    List<Appointment> findByDoctorIdAndScheduledStartLessThanAndScheduledEndGreaterThan(
            Long doctorId, LocalDateTime end, LocalDateTime start);
}