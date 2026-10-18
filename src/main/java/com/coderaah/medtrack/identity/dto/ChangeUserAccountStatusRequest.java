package com.coderaah.medtrack.identity.dto;

import com.coderaah.medtrack.identity.domain.UserAccountStatus;
import jakarta.validation.constraints.NotNull;

public class ChangeUserAccountStatusRequest {

    @NotNull
    private UserAccountStatus status;

    public ChangeUserAccountStatusRequest() {
    }

    public UserAccountStatus getStatus() {
        return status;
    }

    public void setStatus(UserAccountStatus status) {
        this.status = status;
    }
}