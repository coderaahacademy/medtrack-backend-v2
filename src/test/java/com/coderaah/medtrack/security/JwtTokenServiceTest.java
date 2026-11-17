package com.coderaah.medtrack.security;
import com.coderaah.medtrack.common.config.JwtConfig;
import com.coderaah.medtrack.identity.domain.Role;
import java.time.Duration;
import java.util.Base64;
import java.util.List;
import javax.crypto.SecretKey;

import com.coderaah.medtrack.security.service.JwtTokenService;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;

import static org.junit.jupiter.api.Assertions.*;

class JwtTokenServiceTest {

    @Test
    void tokenContainsAccountAndRoles() {
        JwtConfig config = new JwtConfig();

        SecretKey key = config.jwtSecretKey(
                Base64.getEncoder().encodeToString(new byte[32]));

        JwtTokenService service = new JwtTokenService(
                config.jwtEncoder(key),
                "medtrack",
                Duration.ofMinutes(30));

        JwtDecoder decoder = config.jwtDecoder(key, "medtrack");

        Jwt jwt = decoder.decode(
                service.createAccessToken(
                        9L,
                        List.of(Role.DOCTOR)));

        assertEquals("9", jwt.getSubject());
        assertEquals(
                List.of("DOCTOR"),
                jwt.getClaimAsStringList("roles"));

        assertNotNull(jwt.getIssuedAt());
        assertNotNull(jwt.getExpiresAt());

        assertEquals(
                Duration.ofMinutes(30),
                Duration.between(
                        jwt.getIssuedAt(),
                        jwt.getExpiresAt()));
    }

    @Test
    void tokenSignedWithDifferentKeyIsRejected() {
        JwtConfig config = new JwtConfig();

        byte[] firstBytes = new byte[32];
        byte[] secondBytes = new byte[32];
        secondBytes[0] = 1;

        SecretKey firstKey = config.jwtSecretKey(
                Base64.getEncoder().encodeToString(firstBytes));

        SecretKey secondKey = config.jwtSecretKey(
                Base64.getEncoder().encodeToString(secondBytes));

        JwtTokenService service = new JwtTokenService(
                config.jwtEncoder(firstKey),
                "medtrack",
                Duration.ofMinutes(30));

        String token = service.createAccessToken(
                9L,
                List.of(Role.DOCTOR));

        JwtDecoder decoder = config.jwtDecoder(
                secondKey,
                "medtrack");

        assertThrows(
                JwtException.class,
                () -> decoder.decode(token));
    }

    @Test
    void expiredTokenIsRejected() {
        JwtConfig config = new JwtConfig();

        SecretKey key = config.jwtSecretKey(
                Base64.getEncoder().encodeToString(new byte[32]));

        var now = java.time.Instant.now();

        var claims = org.springframework.security.oauth2.jwt
                .JwtClaimsSet.builder()
                .issuer("medtrack")
                .subject("9")
                .issuedAt(now.minusSeconds(600))
                .expiresAt(now.minusSeconds(300))
                .build();

        var header = org.springframework.security.oauth2.jwt
                .JwsHeader.with(
                        org.springframework.security.oauth2.jose.jws
                                .MacAlgorithm.HS256)
                .build();

        String token = config.jwtEncoder(key)
                .encode(org.springframework.security.oauth2.jwt
                        .JwtEncoderParameters.from(header, claims))
                .getTokenValue();

        JwtDecoder decoder = config.jwtDecoder(key, "medtrack");

        assertThrows(
                JwtException.class,
                () -> decoder.decode(token));
    }
}