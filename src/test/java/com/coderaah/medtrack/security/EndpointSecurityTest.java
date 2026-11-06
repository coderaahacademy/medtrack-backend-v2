package com.coderaah.medtrack.security;

import com.coderaah.medtrack.common.config.SecurityConfig;
import com.coderaah.medtrack.prescription.controller.PrescriptionController;
import com.coderaah.medtrack.prescription.dto.PrescriptionResponse;
import com.coderaah.medtrack.prescription.service.PrescriptionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PrescriptionController.class)
@Import({SecurityConfig.class, CurrentUser.class})
class EndpointSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @MockitoBean
    private PrescriptionService prescriptionService;

    @Test
    void anonymousRequestIsRejected() throws Exception {
        mockMvc.perform(
                        patch("/api/prescriptions/1/issue"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(prescriptionService);
    }

    @Test
    void wrongRoleIsRejected() throws Exception {
        mockMvc.perform(
                        patch("/api/prescriptions/1/issue")
                                .with(jwt()
                                        .jwt(token -> token.subject("9"))
                                        .authorities(
                                                new SimpleGrantedAuthority(
                                                        "ROLE_PATIENT"))))
                .andExpect(status().isForbidden());

        verifyNoInteractions(prescriptionService);
    }

    @Test
    void actorComesFromTokenInsteadOfRequest() throws Exception {
        PrescriptionResponse response =
                new PrescriptionResponse();
        response.setId(1L);

        when(prescriptionService.issue(1L, 9L))
                .thenReturn(response);

        mockMvc.perform(
                        patch("/api/prescriptions/1/issue")
                                .param("actorUserId", "999")
                                .with(jwt()
                                        .jwt(token -> token.subject("9"))
                                        .authorities(
                                                new SimpleGrantedAuthority(
                                                        "ROLE_DOCTOR"))))
                .andExpect(status().isOk());

        verify(prescriptionService).issue(1L, 9L);
        verify(prescriptionService, never()).issue(1L, 999L);
    }
    @Test
    void bearerTokenRolesAreConvertedToSpringAuthorities() throws Exception {
        var token = org.springframework.security.oauth2.jwt.Jwt
                .withTokenValue("test-token")
                .header("alg", "HS256")
                .subject("9")
                .claim("roles", java.util.List.of("DOCTOR"))
                .build();

        when(jwtDecoder.decode("test-token"))
                .thenReturn(token);

        PrescriptionResponse response = new PrescriptionResponse();
        response.setId(1L);

        when(prescriptionService.issue(1L, 9L))
                .thenReturn(response);

        mockMvc.perform(patch("/api/prescriptions/1/issue")
                        .header("Authorization", "Bearer test-token"))
                .andExpect(status().isOk());

        verify(jwtDecoder).decode("test-token");
        verify(prescriptionService).issue(1L, 9L);
    }
}