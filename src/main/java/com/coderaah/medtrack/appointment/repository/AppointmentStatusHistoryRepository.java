package com.coderaah.medtrack.appointment.repository;
import com.coderaah.medtrack.appointment.domain.AppointmentStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AppointmentStatusHistoryRepository extends JpaRepository<AppointmentStatusHistory, Long> {

    List<AppointmentStatusHistory> findByAppointmentIdOrderByChangedAtAsc(Long appointmentId);
}

