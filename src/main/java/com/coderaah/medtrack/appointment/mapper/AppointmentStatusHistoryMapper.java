package com.coderaah.medtrack.appointment.mapper;
import com.coderaah.medtrack.appointment.domain.AppointmentStatusHistory;
import com.coderaah.medtrack.appointment.dto.requestDto.AppointmentStatusHistoryRequestDto;
import com.coderaah.medtrack.appointment.dto.responeDto.AppointmentStatusHistoryResponeDto;


public class AppointmentStatusHistoryMapper {
    public AppointmentStatusHistoryMapper() {
    }

    public AppointmentStatusHistory convertToAppointmentStatusHistory(AppointmentStatusHistoryRequestDto appointmentStatusHistoryRequestDto) {
        AppointmentStatusHistory appointmentStatusHistory = new AppointmentStatusHistory();
        appointmentStatusHistory.setReason(appointmentStatusHistoryRequestDto.getReason());
        appointmentStatusHistory.setNewStatus(appointmentStatusHistoryRequestDto.getNewStatus());
        return appointmentStatusHistory;
    }


    public AppointmentStatusHistoryResponeDto convertToAppointmentStatusHistoryResponeDto(AppointmentStatusHistory appointmentStatusHistory) {
        AppointmentStatusHistoryResponeDto appointmentStatusHistoryResponeDto = new AppointmentStatusHistoryResponeDto();
        appointmentStatusHistoryResponeDto.setId(appointmentStatusHistory.getId());
        appointmentStatusHistoryResponeDto.setAppointmentId(appointmentStatusHistory.getAppointment().getId());
        appointmentStatusHistoryResponeDto.setChangedByUserId(appointmentStatusHistory.getChangedByUser().getId());
        appointmentStatusHistoryResponeDto.setReason(appointmentStatusHistory.getReason());
        appointmentStatusHistoryResponeDto.setNewStatus(appointmentStatusHistory.getNewStatus());
        appointmentStatusHistoryResponeDto.setOldStatus(appointmentStatusHistory.getOldStatus());
        appointmentStatusHistoryResponeDto.setChangedAt(appointmentStatusHistory.getChangedAt());
        return appointmentStatusHistoryResponeDto;


    }
}
