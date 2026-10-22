package com.coderaah.medtrack.pharmacy.dto;

import com.coderaah.medtrack.pharmacy.domain.PharmacyStaffRole;
import jakarta.validation.constraints.NotNull;

public class AssignPharmacyStaffMembershipRequest {

    @NotNull
    private Long userAccountId;

    @NotNull
    private PharmacyStaffRole staffRole;

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
}
