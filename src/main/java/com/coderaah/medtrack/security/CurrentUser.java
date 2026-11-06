package com.coderaah.medtrack.security;

import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

@Component
public class CurrentUser {

    public Long accountId() {
        Authentication authentication = SecurityContextHolder
                .getContext()
                .getAuthentication();

        if (!(authentication instanceof JwtAuthenticationToken jwt)
                || !authentication.isAuthenticated()) {

            throw new AuthenticationCredentialsNotFoundException(
                    "Authentication is required");
        }

        try {
            long accountId = Long.parseLong(
                    jwt.getToken().getSubject());

            if (accountId <= 0) {
                throw new NumberFormatException();
            }

            return accountId;
        } catch (NumberFormatException exception) {
            throw new AuthenticationCredentialsNotFoundException(
                    "Invalid authenticated account");
        }
    }
}