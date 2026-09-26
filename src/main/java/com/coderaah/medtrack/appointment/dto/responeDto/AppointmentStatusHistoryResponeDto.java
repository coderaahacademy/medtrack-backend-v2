package com.coderaah.medtrack.appointment.dto.responeDto;

import com.coderaah.medtrack.appointment.domain.AppointmentStatus;

import java.time.LocalDateTime;

public class AppointmentStatusHistoryResponeDto {

    private Long id;
    private Long appointmentId;
    private AppointmentStatus oldStatus;
    private AppointmentStatus newStatus;
    private Long changedByUserId;
    private String reason;
    private LocalDateTime changedAt;
    public AppointmentStatusHistoryResponeDto() {}


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getChangedAt() {
        return changedAt;
    }

    public void setChangedAt(LocalDateTime changedAt) {
        this.changedAt = changedAt;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public Long getChangedByUserId() {
        return changedByUserId;
    }

    public void setChangedByUserId(Long changedByUserId) {
        this.changedByUserId = changedByUserId;
    }

    public AppointmentStatus getNewStatus() {
        return newStatus;
    }

    public void setNewStatus(AppointmentStatus newStatus) {
        this.newStatus = newStatus;
    }

    public AppointmentStatus getOldStatus() {
        return oldStatus;
    }

    public void setOldStatus(AppointmentStatus oldStatus) {
        this.oldStatus = oldStatus;
    }

    public Long getAppointmentId() {
        return appointmentId;
    }

    public void setAppointmentId(Long appointmentId) {
        this.appointmentId = appointmentId;
    }
}
