package com.coderaah.medtrack.pharmacy.service;

import com.coderaah.medtrack.identity.domain.UserAccount;
import com.coderaah.medtrack.identity.exception.UserAccountNotFoundException;
import com.coderaah.medtrack.identity.repository.UserAccountRepository;
import com.coderaah.medtrack.pharmacy.domain.Pharmacy;
import com.coderaah.medtrack.pharmacy.domain.PharmacyStaffMembership;
import com.coderaah.medtrack.pharmacy.dto.AssignPharmacyStaffMembershipRequest;
import com.coderaah.medtrack.pharmacy.dto.ChangePharmacyStaffRoleRequest;
import com.coderaah.medtrack.pharmacy.dto.PharmacyStaffMembershipResponse;
import com.coderaah.medtrack.pharmacy.exception.*;
import com.coderaah.medtrack.pharmacy.repository.PharmacyRepository;
import com.coderaah.medtrack.pharmacy.repository.PharmacyStaffMembershipRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class PharmacyStaffMembershipService {

    private final PharmacyStaffMembershipRepository pharmacyStaffMembershipRepository;

    private final PharmacyRepository pharmacyRepository;

    private final UserAccountRepository userAccountRepository;

    public PharmacyStaffMembershipService(PharmacyStaffMembershipRepository pharmacyStaffMembershipRepository, PharmacyRepository pharmacyRepository, UserAccountRepository userAccountRepository) {
        this.pharmacyStaffMembershipRepository = pharmacyStaffMembershipRepository;
        this.pharmacyRepository = pharmacyRepository;
        this.userAccountRepository = userAccountRepository;
    }


    public PharmacyStaffMembershipResponse assignStaff(Long pharmacyId, AssignPharmacyStaffMembershipRequest request) {

        Pharmacy pharmacy = pharmacyRepository.findById(pharmacyId)
                .orElseThrow(() -> new PharmacyNotFoundException(
                        "Pharmacy with id " + pharmacyId + " not found."
                ));

        if (!pharmacy.isActive()) {
            throw new PharmacyNotActiveException(
                    "Pharmacy with id " + pharmacyId + " is not active."
            );
        }
        UserAccount userAccount = userAccountRepository.findById(request.getUserAccountId())
                .orElseThrow(() -> new UserAccountNotFoundException(
                        "UserAccount with id " + request.getUserAccountId() + " not found."
                ));

        if (pharmacyStaffMembershipRepository.existsByUserAccount_IdAndPharmacy_IdAndActiveTrue(request.getUserAccountId(), pharmacyId)) {
            throw new DuplicateActivePharmacyStaffMembershipException(
                    "User account " + request.getUserAccountId() + " already has an active membership in the pharmacy " + pharmacyId
            );
        }
        PharmacyStaffMembership pharmacyStaffMembership = new PharmacyStaffMembership(
                userAccount, pharmacy, request.getStaffRole(), LocalDateTime.now()
        );

        PharmacyStaffMembership savedPharmacyStaffMembership = pharmacyStaffMembershipRepository.save(pharmacyStaffMembership);

        return toResponse(savedPharmacyStaffMembership);
    }

    @Transactional(readOnly = true)
    public PharmacyStaffMembershipResponse getStaffMembership(Long membershipId) {
         return toResponse(findMembershipOrThrow(membershipId));
    }

    @Transactional(readOnly = true)
    public List<PharmacyStaffMembershipResponse> getPharmacyStaff(Long pharmacyId, Boolean active) {
        ensurePharmacyExists(pharmacyId);
        if (Boolean.TRUE.equals(active)) {
            return pharmacyStaffMembershipRepository.findByPharmacy_IdAndActiveTrue(pharmacyId)
                    .stream()
                    .map(this::toResponse)
                    .toList();
        }
        return pharmacyStaffMembershipRepository.findByPharmacy_Id(pharmacyId)
                .stream().map(this::toResponse).toList();

    }


    public PharmacyStaffMembershipResponse changeStaffRole(Long membershipId, ChangePharmacyStaffRoleRequest request) {
        PharmacyStaffMembership pharmacyStaffMembership = findMembershipOrThrow(membershipId);
        if (!pharmacyStaffMembership.isActive()) {
            throw new PharmacyStaffMembershipNotActiveException(
                    "Pharmacy staff membership " + membershipId + " has ended and cannot be changed."
            );
        }
        pharmacyStaffMembership.setStaffRole(request.getStaffRole());
        return toResponse(pharmacyStaffMembershipRepository.save(pharmacyStaffMembership));
    }

    public PharmacyStaffMembershipResponse endStaffMembership (Long membershipId) {
        PharmacyStaffMembership pharmacyStaffMembership = findMembershipOrThrow(membershipId);
        if (!pharmacyStaffMembership.isActive()) {
            throw new PharmacyStaffMembershipNotActiveException(
                    "Pharmacy staff membership " + membershipId + " has ended and cannot be changed."
            );
        }
        pharmacyStaffMembership.setActive(false);
        pharmacyStaffMembership.setEndedAt(LocalDateTime.now());
        return toResponse(pharmacyStaffMembershipRepository.save(pharmacyStaffMembership));
    }


    private void ensurePharmacyExists(Long pharmacyId) {
        if (!pharmacyRepository.existsById(pharmacyId)) {
            throw new PharmacyNotFoundException("Pharmacy with id " + pharmacyId + " not found.");
        }
    }

    private PharmacyStaffMembership findMembershipOrThrow(Long membershipId) {

        return pharmacyStaffMembershipRepository.findById(membershipId)
                .orElseThrow(() -> new PharmacyStaffMembershipNotFoundException(
                        "Staff with membership id " + membershipId + " was not found."
                ));
    }

    private PharmacyStaffMembershipResponse toResponse(PharmacyStaffMembership pharmacyStaffMembership) {
        PharmacyStaffMembershipResponse response = new PharmacyStaffMembershipResponse();
        response.setId(pharmacyStaffMembership.getId());
        response.setPharmacyId(pharmacyStaffMembership.getPharmacy().getId());
        response.setUserAccountId(pharmacyStaffMembership.getUserAccount().getId());
        response.setStaffRole(pharmacyStaffMembership.getStaffRole());
        response.setStartedAt(pharmacyStaffMembership.getStartedAt());
        response.setEndedAt(pharmacyStaffMembership.getEndedAt());
        response.setActive(pharmacyStaffMembership.isActive());

        return response;
    }
}
