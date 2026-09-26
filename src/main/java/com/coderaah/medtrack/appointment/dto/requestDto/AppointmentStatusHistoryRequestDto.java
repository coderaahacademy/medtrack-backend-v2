package com.coderaah.medtrack.appointment.dto.requestDto;

import com.coderaah.medtrack.appointment.domain.AppointmentStatus;
import jakarta.validation.constraints.NotNull;


public class AppointmentStatusHistoryRequestDto {
    @NotNull
    private String reason;
    @NotNull
    private AppointmentStatus newStatus;

    public AppointmentStatusHistoryRequestDto() {
    }

    public void setReason(String reason) {
        this.reason = reason;
    }


    public void setNewStatus(AppointmentStatus newStatus) {
        this.newStatus = newStatus;
    }

    public String getReason() {
        return reason;
    }

    public AppointmentStatus getNewStatus() {
        return newStatus;
    }
}
