package com.coderaah.medtrack.pharmacy.controller;

import com.coderaah.medtrack.identity.exception.UserAccountNotFoundException;
import com.coderaah.medtrack.pharmacy.domain.PharmacyStaffRole;
import com.coderaah.medtrack.pharmacy.dto.PharmacyStaffMembershipResponse;
import com.coderaah.medtrack.pharmacy.exception.DuplicateActivePharmacyStaffMembershipException;
import com.coderaah.medtrack.pharmacy.exception.PharmacyNotFoundException;
import com.coderaah.medtrack.pharmacy.exception.PharmacyStaffMembershipNotActiveException;
import com.coderaah.medtrack.pharmacy.exception.PharmacyStaffMembershipNotFoundException;
import com.coderaah.medtrack.pharmacy.service.PharmacyStaffMembershipService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PharmacyStaffMembershipController.class)
public class PharmacyStaffMembershipControllerTest {

    private static final String PHARMACY_STAFF = "/api/pharmacies/1/staff";
    private static final String MEMBERSHIPS = "/api/pharmacy-staff-memberships";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PharmacyStaffMembershipService pharmacyStaffMembershipService;

    private PharmacyStaffMembershipResponse activeResponse() {
        PharmacyStaffMembershipResponse response = new PharmacyStaffMembershipResponse();
        response.setId(10L);
        response.setPharmacyId(1L);
        response.setUserAccountId(7L);
        response.setStaffRole(PharmacyStaffRole.PHARMACIST);
        response.setStartedAt(LocalDateTime.of(2026, 10, 1, 9, 0));
        response.setEndedAt(null);
        response.setActive(true);
        return response;
    }

    private PharmacyStaffMembershipResponse endedResponse() {
        PharmacyStaffMembershipResponse response = activeResponse();
        response.setId(20L);
        response.setActive(false);
        response.setEndedAt(LocalDateTime.of(2026, 10, 15, 17, 0));
        return response;
    }

    //assigning

    @Test
    void assignStaff_returnsCreated() throws Exception {

        when(pharmacyStaffMembershipService.assignStaff(eq(1L), any())).thenReturn(activeResponse());

        mockMvc.perform(post(PHARMACY_STAFF)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "userAccountId": 7, "staffRole": "PHARMACIST" }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.userAccountId").value(7))
                .andExpect(jsonPath("$.staffRole").value("PHARMACIST"))
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    void assignStaff_returnsBadRequest_whenUserAccountIdMissing() throws Exception {

        mockMvc.perform(post(PHARMACY_STAFF)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "staffRole": "PHARMACIST" }
                                """))
                .andExpect(status().isBadRequest());

        verify(pharmacyStaffMembershipService, never()).assignStaff(anyLong(), any());
    }

    @Test
    void assignStaff_returnsBadRequest_whenStaffRoleInvalid() throws Exception {

        mockMvc.perform(post(PHARMACY_STAFF)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "userAccountId": 7, "staffRole": "DOCTOR" }
                                """))
                .andExpect(status().isBadRequest());

        verify(pharmacyStaffMembershipService, never()).assignStaff(anyLong(), any());
    }

    @Test
    void assignStaff_returnsConflict_whenDuplicateActiveMembership() throws Exception {

        when(pharmacyStaffMembershipService.assignStaff(eq(1L), any()))
                .thenThrow(new DuplicateActivePharmacyStaffMembershipException("duplicate"));

        mockMvc.perform(post(PHARMACY_STAFF)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "userAccountId": 7, "staffRole": "STAFF" }
                                """))
                .andExpect(status().isConflict());
    }

    @Test
    void assignStaff_returnsNotFound_whenPharmacyDoesNotExist() throws Exception {

        when(pharmacyStaffMembershipService.assignStaff(eq(1L), any()))
                .thenThrow(new PharmacyNotFoundException("not found"));

        mockMvc.perform(post(PHARMACY_STAFF)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "userAccountId": 7, "staffRole": "STAFF" }
                                """))
                .andExpect(status().isNotFound());
    }

    @Test
    void assignStaff_returnsNotFound_whenUserAccountDoesNotExist() throws Exception {

        when(pharmacyStaffMembershipService.assignStaff(eq(1L), any()))
                .thenThrow(new UserAccountNotFoundException("not found"));

        mockMvc.perform(post(PHARMACY_STAFF)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "userAccountId": 999, "staffRole": "STAFF" }
                                """))
                .andExpect(status().isNotFound());
    }

    //listing

    @Test
    void getPharmacyStaff_withoutFilter_returnsAllIncludingHistory() throws Exception {

        when(pharmacyStaffMembershipService.getPharmacyStaff(eq(1L), isNull()))
                .thenReturn(List.of(activeResponse(), endedResponse()));

        mockMvc.perform(get(PHARMACY_STAFF))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].active").value(true))
                .andExpect(jsonPath("$[1].active").value(false));
    }

    @Test
    void getPharmacyStaff_withActiveTrue_passesFilterToService() throws Exception {

        when(pharmacyStaffMembershipService.getPharmacyStaff(1L, true)).thenReturn(List.of(activeResponse()));

        mockMvc.perform(get(PHARMACY_STAFF).param("active", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].active").value(true));
    }

    //retrieving

    @Test
    void getStaffMembership_returnsEndedMembership() throws Exception {

        when(pharmacyStaffMembershipService.getStaffMembership(20L)).thenReturn(endedResponse());

        mockMvc.perform(get(MEMBERSHIPS + "/20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(20))
                .andExpect(jsonPath("$.active").value(false))
                .andExpect(jsonPath("$.endedAt").isNotEmpty());
    }

    @Test
    void getStaffMembership_returnsNotFound() throws Exception {

        when(pharmacyStaffMembershipService.getStaffMembership(99L))
                .thenThrow(new PharmacyStaffMembershipNotFoundException("not found"));

        mockMvc.perform(get(MEMBERSHIPS + "/99"))
                .andExpect(status().isNotFound());
    }

    // changing role

    @Test
    void changeStaffRole_returnsOk() throws Exception {

        PharmacyStaffMembershipResponse changed = activeResponse();
        changed.setStaffRole(PharmacyStaffRole.MANAGER);
        when(pharmacyStaffMembershipService.changeStaffRole(eq(10L), any())).thenReturn(changed);

        mockMvc.perform(patch(MEMBERSHIPS + "/10/role")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "staffRole": "MANAGER" }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.staffRole").value("MANAGER"));
    }

    @Test
    void changeStaffRole_returnsBadRequest_whenStaffRoleMissing() throws Exception {

        mockMvc.perform(patch(MEMBERSHIPS + "/10/role")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());

        verify(pharmacyStaffMembershipService, never()).changeStaffRole(anyLong(), any());
    }

    @Test
    void changeStaffRole_returnsConflict_whenMembershipEnded() throws Exception {

        when(pharmacyStaffMembershipService.changeStaffRole(eq(20L), any()))
                .thenThrow(new PharmacyStaffMembershipNotActiveException("ended"));

        mockMvc.perform(patch(MEMBERSHIPS + "/20/role")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "staffRole": "MANAGER" }
                                """))
                .andExpect(status().isConflict());
    }

    //ending

    @Test
    void endStaffMembership_returnsOk() throws Exception {

        when(pharmacyStaffMembershipService.endStaffMembership(10L)).thenReturn(endedResponse());

        mockMvc.perform(patch(MEMBERSHIPS + "/10/end"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false))
                .andExpect(jsonPath("$.endedAt").isNotEmpty());
    }

    @Test
    void endStaffMembership_returnsConflict_whenAlreadyEnded() throws Exception {

        when(pharmacyStaffMembershipService.endStaffMembership(20L))
                .thenThrow(new PharmacyStaffMembershipNotActiveException("already ended"));

        mockMvc.perform(patch(MEMBERSHIPS + "/20/end"))
                .andExpect(status().isConflict());
    }
}