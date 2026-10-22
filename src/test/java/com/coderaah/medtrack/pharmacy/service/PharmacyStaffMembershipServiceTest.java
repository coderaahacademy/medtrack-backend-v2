package com.coderaah.medtrack.pharmacy.service;

import com.coderaah.medtrack.identity.domain.UserAccount;
import com.coderaah.medtrack.identity.exception.UserAccountNotFoundException;
import com.coderaah.medtrack.identity.repository.UserAccountRepository;
import com.coderaah.medtrack.pharmacy.domain.Pharmacy;
import com.coderaah.medtrack.pharmacy.domain.PharmacyStaffMembership;
import com.coderaah.medtrack.pharmacy.domain.PharmacyStaffRole;
import com.coderaah.medtrack.pharmacy.dto.AssignPharmacyStaffMembershipRequest;
import com.coderaah.medtrack.pharmacy.dto.ChangePharmacyStaffRoleRequest;
import com.coderaah.medtrack.pharmacy.dto.PharmacyStaffMembershipResponse;
import com.coderaah.medtrack.pharmacy.exception.DuplicateActivePharmacyStaffMembershipException;
import com.coderaah.medtrack.pharmacy.exception.PharmacyNotActiveException;
import com.coderaah.medtrack.pharmacy.exception.PharmacyNotFoundException;
import com.coderaah.medtrack.pharmacy.exception.PharmacyStaffMembershipNotActiveException;
import com.coderaah.medtrack.pharmacy.exception.PharmacyStaffMembershipNotFoundException;
import com.coderaah.medtrack.pharmacy.repository.PharmacyRepository;
import com.coderaah.medtrack.pharmacy.repository.PharmacyStaffMembershipRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class PharmacyStaffMembershipServiceTest {

    private PharmacyStaffMembershipRepository pharmacyStaffMembershipRepository;
    private PharmacyRepository pharmacyRepository;
    private UserAccountRepository userAccountRepository;
    private PharmacyStaffMembershipService pharmacyStaffMembershipService;

    private Pharmacy pharmacy;
    private UserAccount userAccount;

    @BeforeEach
    void setUp() {
        pharmacyStaffMembershipRepository = mock(PharmacyStaffMembershipRepository.class);
        pharmacyRepository = mock(PharmacyRepository.class);
        userAccountRepository = mock(UserAccountRepository.class);
        pharmacyStaffMembershipService = new PharmacyStaffMembershipService(
                pharmacyStaffMembershipRepository, pharmacyRepository, userAccountRepository);

        pharmacy = new Pharmacy("Apotheke Nord", "L-123"); // this constructor sets active = true
        pharmacy.setId(1L);

        userAccount = new UserAccount();
        userAccount.setId(7L);


        when(pharmacyStaffMembershipRepository.save(any(PharmacyStaffMembership.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }



    private AssignPharmacyStaffMembershipRequest assignRequest(PharmacyStaffRole role) {
        AssignPharmacyStaffMembershipRequest request = new AssignPharmacyStaffMembershipRequest();
        request.setUserAccountId(7L);
        request.setStaffRole(role);
        return request;
    }

    private PharmacyStaffMembership activeMembership(Long id) {
        PharmacyStaffMembership membership = new PharmacyStaffMembership(
                userAccount, pharmacy, PharmacyStaffRole.STAFF, LocalDateTime.now().minusDays(30));
        membership.setId(id);
        when(pharmacyStaffMembershipRepository.findById(id)).thenReturn(Optional.of(membership));
        return membership;
    }

    private PharmacyStaffMembership endedMembership(Long id) {
        PharmacyStaffMembership membership = new PharmacyStaffMembership(
                userAccount, pharmacy, PharmacyStaffRole.PHARMACIST, LocalDateTime.now().minusDays(90));
        membership.setId(id);
        membership.setActive(false);
        membership.setEndedAt(LocalDateTime.now().minusDays(10));
        when(pharmacyStaffMembershipRepository.findById(id)).thenReturn(Optional.of(membership));
        return membership;
    }

    // assigning user to pharmacy

    @Test
    void assignStaff_createsActiveMembership() {

        when(pharmacyRepository.findById(1L)).thenReturn(Optional.of(pharmacy));
        when(userAccountRepository.findById(7L)).thenReturn(Optional.of(userAccount));
        when(pharmacyStaffMembershipRepository.existsByUserAccount_IdAndPharmacy_IdAndActiveTrue(7L, 1L)).thenReturn(false);

        PharmacyStaffMembershipResponse result = pharmacyStaffMembershipService.assignStaff(1L, assignRequest(PharmacyStaffRole.PHARMACIST));

        assertEquals(1L, result.getPharmacyId());
        assertEquals(7L, result.getUserAccountId());
        assertEquals(PharmacyStaffRole.PHARMACIST, result.getStaffRole());
        assertTrue(result.isActive());
        assertNotNull(result.getStartedAt());
        assertNull(result.getEndedAt());

        ArgumentCaptor<PharmacyStaffMembership> captor = ArgumentCaptor.forClass(PharmacyStaffMembership.class);
        verify(pharmacyStaffMembershipRepository).save(captor.capture());
        assertTrue(captor.getValue().isActive());
        assertNull(captor.getValue().getEndedAt());
    }

    @Test
    void assignStaff_throwsException_whenPharmacyDoesNotExist() {

        when(pharmacyRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(PharmacyNotFoundException.class,
                () -> pharmacyStaffMembershipService.assignStaff(1L, assignRequest(PharmacyStaffRole.STAFF)));

        verify(pharmacyStaffMembershipRepository, never()).save(any());
    }

    @Test
    void assignStaff_throwsException_whenPharmacyIsNotActive() {

        pharmacy.setActive(false);
        when(pharmacyRepository.findById(1L)).thenReturn(Optional.of(pharmacy));

        assertThrows(PharmacyNotActiveException.class,
                () -> pharmacyStaffMembershipService.assignStaff(1L, assignRequest(PharmacyStaffRole.STAFF)));

        verify(pharmacyStaffMembershipRepository, never()).save(any());
    }

    @Test
    void assignStaff_throwsException_whenUserAccountDoesNotExist() {

        when(pharmacyRepository.findById(1L)).thenReturn(Optional.of(pharmacy));
        when(userAccountRepository.findById(7L)).thenReturn(Optional.empty());

        assertThrows(UserAccountNotFoundException.class,
                () -> pharmacyStaffMembershipService.assignStaff(1L, assignRequest(PharmacyStaffRole.STAFF)));

        verify(pharmacyStaffMembershipRepository, never()).save(any());
    }

    @Test
    void assignStaff_throwsException_whenUserAlreadyHasActiveMembership() {

        when(pharmacyRepository.findById(1L)).thenReturn(Optional.of(pharmacy));
        when(userAccountRepository.findById(7L)).thenReturn(Optional.of(userAccount));
        when(pharmacyStaffMembershipRepository.existsByUserAccount_IdAndPharmacy_IdAndActiveTrue(7L, 1L)).thenReturn(true);

        assertThrows(DuplicateActivePharmacyStaffMembershipException.class,
                () -> pharmacyStaffMembershipService.assignStaff(1L, assignRequest(PharmacyStaffRole.STAFF)));

        verify(pharmacyStaffMembershipRepository, never()).save(any());
    }

    @Test
    void assignStaff_succeeds_whenUserOnlyHasEndedMembershipInSamePharmacy() {


        when(pharmacyRepository.findById(1L)).thenReturn(Optional.of(pharmacy));
        when(userAccountRepository.findById(7L)).thenReturn(Optional.of(userAccount));
        when(pharmacyStaffMembershipRepository.existsByUserAccount_IdAndPharmacy_IdAndActiveTrue(7L, 1L)).thenReturn(false);

        PharmacyStaffMembershipResponse result = pharmacyStaffMembershipService.assignStaff(1L, assignRequest(PharmacyStaffRole.MANAGER));

        assertTrue(result.isActive());
        verify(pharmacyStaffMembershipRepository).save(any(PharmacyStaffMembership.class));
    }

    //  retrieving membership

    @Test
    void getStaffMembership_returnsEndedMembership() {

        PharmacyStaffMembership ended = endedMembership(20L);

        PharmacyStaffMembershipResponse result = pharmacyStaffMembershipService.getStaffMembership(20L);

        assertEquals(20L, result.getId());
        assertFalse(result.isActive());
        assertEquals(ended.getEndedAt(), result.getEndedAt());
        assertEquals(ended.getStartedAt(), result.getStartedAt());
    }

    @Test
    void getStaffMembership_throwsException_whenNotFound() {

        when(pharmacyStaffMembershipRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(PharmacyStaffMembershipNotFoundException.class,
                () -> pharmacyStaffMembershipService.getStaffMembership(99L));
    }

    //listing pharmacy staff

    @Test
    void getPharmacyStaff_withoutFilter_returnsAllIncludingHistory() {

        PharmacyStaffMembership active = activeMembership(10L);
        PharmacyStaffMembership ended = endedMembership(11L);
        when(pharmacyRepository.existsById(1L)).thenReturn(true);
        when(pharmacyStaffMembershipRepository.findByPharmacy_Id(1L)).thenReturn(List.of(active, ended));

        List<PharmacyStaffMembershipResponse> result = pharmacyStaffMembershipService.getPharmacyStaff(1L, null);

        assertEquals(2, result.size());
        assertTrue(result.get(0).isActive());
        assertFalse(result.get(1).isActive());
        verify(pharmacyStaffMembershipRepository, never()).findByPharmacy_IdAndActiveTrue(any());
    }

    @Test
    void getPharmacyStaff_withActiveTrue_returnsOnlyActive() {

        PharmacyStaffMembership active = activeMembership(10L);
        when(pharmacyRepository.existsById(1L)).thenReturn(true);
        when(pharmacyStaffMembershipRepository.findByPharmacy_IdAndActiveTrue(1L)).thenReturn(List.of(active));

        List<PharmacyStaffMembershipResponse> result = pharmacyStaffMembershipService.getPharmacyStaff(1L, true);

        assertEquals(1, result.size());
        assertTrue(result.get(0).isActive());
        verify(pharmacyStaffMembershipRepository, never()).findByPharmacy_Id(any());
    }

    @Test
    void getPharmacyStaff_throwsException_whenPharmacyDoesNotExist() {

        when(pharmacyRepository.existsById(1L)).thenReturn(false);

        assertThrows(PharmacyNotFoundException.class,
                () -> pharmacyStaffMembershipService.getPharmacyStaff(1L, null));
    }

    //changing role

    @Test
    void changeStaffRole_updatesRole_whenMembershipIsActive() {

        activeMembership(10L);
        ChangePharmacyStaffRoleRequest request = new ChangePharmacyStaffRoleRequest();
        request.setStaffRole(PharmacyStaffRole.MANAGER);

        PharmacyStaffMembershipResponse result = pharmacyStaffMembershipService.changeStaffRole(10L, request);

        assertEquals(PharmacyStaffRole.MANAGER, result.getStaffRole());
        verify(pharmacyStaffMembershipRepository).save(any(PharmacyStaffMembership.class));
    }

    @Test
    void changeStaffRole_throwsException_whenMembershipHasEnded() {

        PharmacyStaffMembership ended = endedMembership(20L);
        ChangePharmacyStaffRoleRequest request = new ChangePharmacyStaffRoleRequest();
        request.setStaffRole(PharmacyStaffRole.MANAGER);

        assertThrows(PharmacyStaffMembershipNotActiveException.class,
                () -> pharmacyStaffMembershipService.changeStaffRole(20L, request));

        assertEquals(PharmacyStaffRole.PHARMACIST, ended.getStaffRole()); // role unchanged
        verify(pharmacyStaffMembershipRepository, never()).save(any());
    }

    @Test
    void changeStaffRole_throwsException_whenMembershipNotFound() {

        when(pharmacyStaffMembershipRepository.findById(99L)).thenReturn(Optional.empty());
        ChangePharmacyStaffRoleRequest request = new ChangePharmacyStaffRoleRequest();
        request.setStaffRole(PharmacyStaffRole.MANAGER);

        assertThrows(PharmacyStaffMembershipNotFoundException.class,
                () -> pharmacyStaffMembershipService.changeStaffRole(99L, request));
    }

    // ending membership

    @Test
    void endStaffMembership_setsInactiveAndEndedAt_withoutDeleting() {

        activeMembership(10L);

        PharmacyStaffMembershipResponse result = pharmacyStaffMembershipService.endStaffMembership(10L);

        assertFalse(result.isActive());
        assertNotNull(result.getEndedAt());
        verify(pharmacyStaffMembershipRepository).save(any(PharmacyStaffMembership.class));
        verify(pharmacyStaffMembershipRepository, never()).delete(any());
        verify(pharmacyStaffMembershipRepository, never()).deleteById(any());
    }

    @Test
    void endStaffMembership_throwsException_whenAlreadyEnded() {

        PharmacyStaffMembership ended = endedMembership(20L);
        LocalDateTime originalEndedAt = ended.getEndedAt();

        assertThrows(PharmacyStaffMembershipNotActiveException.class,
                () -> pharmacyStaffMembershipService.endStaffMembership(20L));

        assertEquals(originalEndedAt, ended.getEndedAt());
        verify(pharmacyStaffMembershipRepository, never()).save(any());
    }
}