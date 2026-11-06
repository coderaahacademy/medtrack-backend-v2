package com.coderaah.medtrack.security.service;

import com.coderaah.medtrack.security.dto.LoginRequest;
import com.coderaah.medtrack.security.dto.LoginResponse;
import com.coderaah.medtrack.identity.domain.Role;
import com.coderaah.medtrack.identity.domain.UserAccount;
import com.coderaah.medtrack.identity.domain.UserAccountStatus;
import com.coderaah.medtrack.identity.domain.UserRole;
import com.coderaah.medtrack.identity.repository.UserAccountRepository;
import com.coderaah.medtrack.identity.repository.UserRoleRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {

    private final UserAccountRepository userAccountRepository;
    private final UserRoleRepository userRoleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService jwtTokenService;
    private final String dummyPasswordHash;

    public AuthService(
            UserAccountRepository userAccountRepository,
            UserRoleRepository userRoleRepository,
            PasswordEncoder passwordEncoder,
            JwtTokenService jwtTokenService) {

        this.userAccountRepository = userAccountRepository;
        this.userRoleRepository = userRoleRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenService = jwtTokenService;

        this.dummyPasswordHash =
                passwordEncoder.encode("unused-login-timing-value");
    }

    @Transactional
    public LoginResponse login(LoginRequest request) {
        String email = request.getEmail()
                .trim()
                .toLowerCase(Locale.ROOT);

        UserAccount account = userAccountRepository
                .findByEmail(email)
                .orElse(null);

        String storedHash = account == null
                ? dummyPasswordHash
                : account.getPasswordHash();

        boolean passwordMatches = passwordEncoder.matches(
                request.getPassword(),
                storedHash);

        if (account == null
                || !passwordMatches
                || account.getStatus() != UserAccountStatus.ACTIVE) {

            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Invalid email or password");
        }

        List<Role> roles = userRoleRepository
                .findByUserAccountIdOrderByRoleAsc(account.getId())
                .stream()
                .map(UserRole::getRole)
                .toList();

        String token = jwtTokenService.createAccessToken(
                account.getId(),
                roles);

        account.setLastLoginAt(LocalDateTime.now());
        userAccountRepository.save(account);

        return new LoginResponse(
                token,
                "Bearer",
                jwtTokenService.getExpiresInSeconds(),
                account.getId(),
                account.getEmail(),
                roles);
    }
}