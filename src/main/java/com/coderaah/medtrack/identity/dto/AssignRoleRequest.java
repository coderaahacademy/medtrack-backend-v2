package com.coderaah.medtrack.identity.dto;

import com.coderaah.medtrack.identity.domain.Role;
import jakarta.validation.constraints.NotNull;

public class AssignRoleRequest {

    @NotNull
    private Role role;

    public AssignRoleRequest() {
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }
}