package com.coderaah.medtrack.appointment.service;

import com.coderaah.medtrack.appointment.domain.Appointment;
import com.coderaah.medtrack.appointment.domain.AppointmentStatus;
import com.coderaah.medtrack.appointment.domain.AppointmentStatusHistory;
import com.coderaah.medtrack.appointment.dto.responeDto.AppointmentStatusHistoryResponeDto;
import com.coderaah.medtrack.appointment.mapper.AppointmentStatusHistoryMapper;
import com.coderaah.medtrack.appointment.repository.AppointmentStatusHistoryRepository;
import com.coderaah.medtrack.identity.domain.UserAccount;
import com.coderaah.medtrack.identity.repository.UserAccountRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
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


    @Transactional
    public AppointmentStatusHistory record(Appointment appointment,
                                           AppointmentStatus oldStatus,
                                           AppointmentStatus newStatus,
                                           Long actorUserId,
                                           String reason) {

        UserAccount actor = userAccountRepository.findById(actorUserId)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + actorUserId));

        AppointmentStatusHistory history =
                new AppointmentStatusHistory(appointment, oldStatus, newStatus, actor, reason);


        return historyRepository.save(history);
    }


    @Transactional(readOnly = true)
    public List<AppointmentStatusHistoryResponeDto> getHistoryForAppointment(Long appointmentId) {
        return historyRepository
                .findByAppointmentIdOrderByChangedAtAsc(appointmentId).stream()
                .map(historyMapper::convertToAppointmentStatusHistoryResponeDto)
                .toList();
    }
}
