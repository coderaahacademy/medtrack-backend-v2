package com.coderaah.medtrack.prescription.domain;

import com.coderaah.medtrack.identity.domain.UserAccount;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "prescription_status_history")
public class PrescriptionStatusHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prescription_id", nullable = false)
    private Prescription prescription;

    @Enumerated(EnumType.STRING)
    @Column(name = "old_status", length = 50)
    private PrescriptionStatus oldStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "new_status", nullable = false, length = 50)
    private PrescriptionStatus newStatus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "changed_by_user_id", nullable = false)
    private UserAccount changedByUser;

    @Column(name = "reason", length = 255)
    private String reason;

    @Column(name = "changed_at", nullable = false, updatable = false)
    private LocalDateTime changedAt;

    public PrescriptionStatusHistory() {
    }

    public PrescriptionStatusHistory(Prescription prescription, PrescriptionStatus oldStatus, PrescriptionStatus newStatus, UserAccount changedByUser, String reason) {
        this.prescription = prescription;
        this.oldStatus = oldStatus;
        this.newStatus = newStatus;
        this.changedByUser = changedByUser;
        this.reason = reason;
    }

    @PrePersist
    protected void onCreate() {
        if (changedAt == null) {
            changedAt = LocalDateTime.now();
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Prescription getPrescription() {
        return prescription;
    }

    public void setPrescription(Prescription prescription) {
        this.prescription = prescription;
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

    public UserAccount getChangedByUser() {
        return changedByUser;
    }

    public void setChangedByUser(UserAccount changedByUser) {
        this.changedByUser = changedByUser;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public LocalDateTime getChangedAt() {
        return changedAt;
    }

    public void setChangedAt(LocalDateTime changedAt) {
        this.changedAt = changedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PrescriptionStatusHistory that)) return false;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
