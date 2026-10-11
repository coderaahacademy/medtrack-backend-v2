
package com.coderaah.medtrack.prescription.dto;

import com.coderaah.medtrack.prescription.domain.PrescriptionStatus;
import java.time.LocalDateTime;

public class PrescriptionStatusHistoryResponse {

    private Long id;
    private PrescriptionStatus oldStatus;
    private PrescriptionStatus newStatus;
    private LocalDateTime changedAt;
    private String reason;
    private Long changedByUserId;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public PrescriptionStatus getOldStatus() {
        return oldStatus;
    }

    public void setOldStatus(PrescriptionStatus oldStatus) {
        this.oldStatus = oldStatus;
    }

    public PrescriptionStatus getNewStatus() {
        return newStatus;
    }

    public void setNewStatus(PrescriptionStatus newStatus) {
        this.newStatus = newStatus;
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
}
