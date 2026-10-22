package com.coderaah.medtrack.pharmacy.dto;

import com.coderaah.medtrack.pharmacy.domain.PharmacyStaffRole;

import java.time.LocalDateTime;

public class PharmacyStaffMembershipResponse {

    private Long id;
    private Long pharmacyId;
    private Long userAccountId;
    private PharmacyStaffRole staffRole;
    private LocalDateTime startedAt;
    private LocalDateTime endedAt;
    private boolean active;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getPharmacyId() {
        return pharmacyId;
    }

    public void setPharmacyId(Long pharmacyId) {
        this.pharmacyId = pharmacyId;
    }

    public Long getUserAccountId() {
        return userAccountId;
    }

    public void setUserAccountId(Long userAccountId) {
        this.userAccountId = userAccountId;
    }

    public PharmacyStaffRole getStaffRole() {
        return staffRole;
    }

    public void setStaffRole(PharmacyStaffRole staffRole) {
        this.staffRole = staffRole;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(LocalDateTime startedAt) {
        this.startedAt = startedAt;
    }

    public LocalDateTime getEndedAt() {
        return endedAt;
    }

    public void setEndedAt(LocalDateTime endedAt) {
        this.endedAt = endedAt;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
