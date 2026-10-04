package com.coderaah.medtrack.appointment.mapper;

import com.coderaah.medtrack.appointment.domain.AppointmentStatusHistory;
import com.coderaah.medtrack.appointment.dto.AppointmentStatusHistoryResponse;
import org.springframework.stereotype.Component;

@Component
public class AppointmentStatusHistoryMapper {

    public AppointmentStatusHistoryResponse toResponse(AppointmentStatusHistory history) {
        AppointmentStatusHistoryResponse response = new AppointmentStatusHistoryResponse();
        response.setId(history.getId());
        response.setAppointmentId(history.getAppointment().getId());
        response.setChangedByUserId(history.getChangedByUser().getId());
        response.setReason(history.getReason());
        response.setOldStatus(history.getOldStatus());
        response.setNewStatus(history.getNewStatus());
        response.setChangedAt(history.getChangedAt());
        return response;
    }
}
