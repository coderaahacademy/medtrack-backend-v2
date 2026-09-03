package com.coderaah.medtrack.common.exception;

import java.time.LocalDateTime;

public record ApiError(LocalDateTime timestamp, int status, String error, String message) {

}