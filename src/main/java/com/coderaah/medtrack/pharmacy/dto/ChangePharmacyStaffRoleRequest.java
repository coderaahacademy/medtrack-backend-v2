package com.coderaah.medtrack.pharmacy.dto;

import com.coderaah.medtrack.pharmacy.domain.PharmacyStaffRole;
import jakarta.validation.constraints.NotNull;

public class ChangePharmacyStaffRoleRequest {

    @NotNull
    private PharmacyStaffRole staffRole;

    public PharmacyStaffRole getStaffRole() {
        return staffRole;
    }

    public void setStaffRole(PharmacyStaffRole staffRole) {
        this.staffRole = staffRole;
    }
}
