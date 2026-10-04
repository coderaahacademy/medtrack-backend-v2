package com.coderaah.medtrack.appointment.service;

import com.coderaah.medtrack.appointment.domain.Appointment;
import com.coderaah.medtrack.appointment.domain.AppointmentStatus;
import com.coderaah.medtrack.appointment.domain.AppointmentStatusHistory;
import com.coderaah.medtrack.appointment.dto.AppointmentStatusHistoryResponse;
import com.coderaah.medtrack.appointment.mapper.AppointmentStatusHistoryMapper;
import com.coderaah.medtrack.appointment.repository.AppointmentStatusHistoryRepository;
import com.coderaah.medtrack.identity.domain.UserAccount;
import com.coderaah.medtrack.identity.exception.UserAccountNotFoundException;
import com.coderaah.medtrack.identity.repository.UserAccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class AppointmentStatusHistoryService {

    private final AppointmentStatusHistoryRepository historyRepository;
    private final UserAccountRepository userAccountRepository;
    private final AppointmentStatusHistoryMapper historyMapper;

    public AppointmentStatusHistoryService(AppointmentStatusHistoryRepository historyRepository,
                                           UserAccountRepository userAccountRepository,
                                           AppointmentStatusHistoryMapper historyMapper) {
        this.historyRepository = historyRepository;
        this.userAccountRepository = userAccountRepository;
        this.historyMapper = historyMapper;
    }

    public AppointmentStatusHistory record(Appointment appointment,
                                           AppointmentStatus oldStatus,
                                           AppointmentStatus newStatus,
                                           Long actorUserId,
                                           String reason) {
        UserAccount actor = userAccountRepository.findById(actorUserId)
                .orElseThrow(() -> new UserAccountNotFoundException("User account not found"));

        return historyRepository.save(
                new AppointmentStatusHistory(appointment, oldStatus, newStatus, actor, reason));
    }

    @Transactional(readOnly = true)
    public List<AppointmentStatusHistoryResponse> getHistoryForAppointment(Long appointmentId) {
        return historyRepository.findByAppointmentIdOrderByChangedAtAsc(appointmentId).stream()
                .map(historyMapper::toResponse)
                .toList();
    }
}
