package com.coderaah.medtrack.visit.dto;

import java.time.LocalDateTime;

public class CompleteVisitRequest {

    private LocalDateTime endedAt;

    public LocalDateTime getEndedAt() {
        return endedAt;
    }

    public void setEndedAt(LocalDateTime endedAt) {
        this.endedAt = endedAt;
    }
}
