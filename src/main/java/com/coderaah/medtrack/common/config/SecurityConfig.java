package com.coderaah.medtrack.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter authoritiesConverter =
                new JwtGrantedAuthoritiesConverter();

        authoritiesConverter.setAuthoritiesClaimName("roles");
        authoritiesConverter.setAuthorityPrefix("ROLE_");

        JwtAuthenticationConverter converter =
                new JwtAuthenticationConverter();

        converter.setJwtGrantedAuthoritiesConverter(
                authoritiesConverter);

        return converter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JwtAuthenticationConverter jwtAuthenticationConverter)
            throws Exception {

        http
                // Credentials are sent using Bearer headers, not cookies.
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)

                .sessionManagement(session -> session
                        .sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS))

                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/auth/login")
                        .permitAll()

                        .requestMatchers(
                                "/api/users",
                                "/api/users/**")
                        .hasRole("ADMIN")

                        .requestMatchers(
                                "/api/pharmacies/*/staff",
                                "/api/pharmacies/*/staff/**",
                                "/api/pharmacy-staff-memberships/**")
                        .hasRole("ADMIN")

                        .requestMatchers(
                                "/api/pharmacies",
                                "/api/pharmacies/**")
                        .hasAnyRole("PHARMACY_STAFF", "ADMIN")

                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/patients/*/visits",
                                "/api/patients/*/prescriptions")
                        .hasAnyRole("PATIENT", "DOCTOR", "ADMIN")

                        .requestMatchers(
                                "/api/visits",
                                "/api/visits/**",
                                "/api/prescriptions",
                                "/api/prescriptions/**",
                                "/api/appointments/*/visit",
                                "/api/doctors/*/visits")
                        .hasAnyRole("DOCTOR", "ADMIN")

                        .requestMatchers(
                                HttpMethod.PATCH,
                                "/api/appointments/*/cancel")
                        .hasAnyRole("PATIENT", "DOCTOR", "ADMIN")

                        .requestMatchers(
                                HttpMethod.PATCH,
                                "/api/appointments/**")
                        .hasAnyRole("DOCTOR", "ADMIN")

                        .requestMatchers(
                                "/api/appointments",
                                "/api/appointments/**",
                                "/api/patients/*/appointments",
                                "/api/doctors/*/appointments")
                        .hasAnyRole("PATIENT", "DOCTOR", "ADMIN")

                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/patients",
                                "/api/patients/*")
                        .hasAnyRole("PATIENT", "DOCTOR", "ADMIN")

                        .requestMatchers("/api/**")
                        .hasAnyRole("DOCTOR", "ADMIN")

                        .anyRequest()
                        .denyAll())

                .oauth2ResourceServer(resourceServer -> resourceServer
                        .jwt(jwt -> jwt
                                .jwtAuthenticationConverter(
                                        jwtAuthenticationConverter)));

        return http.build();
    }
}