package com.coderaah.medtrack.pharmacy.controller;

import com.coderaah.medtrack.pharmacy.dto.AssignPharmacyStaffMembershipRequest;
import com.coderaah.medtrack.pharmacy.dto.ChangePharmacyStaffRoleRequest;
import com.coderaah.medtrack.pharmacy.dto.PharmacyStaffMembershipResponse;
import com.coderaah.medtrack.pharmacy.service.PharmacyStaffMembershipService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class PharmacyStaffMembershipController {
    private final PharmacyStaffMembershipService pharmacyStaffMembershipService;

    public PharmacyStaffMembershipController(PharmacyStaffMembershipService pharmacyStaffMembershipService) {
        this.pharmacyStaffMembershipService = pharmacyStaffMembershipService;
    }

    @PostMapping("/pharmacies/{pharmacyId}/staff")
    public ResponseEntity<PharmacyStaffMembershipResponse> assignStaff(
            @PathVariable Long pharmacyId,
            @Valid @RequestBody AssignPharmacyStaffMembershipRequest requestPharmacyStaffMembership
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(pharmacyStaffMembershipService.assignStaff(pharmacyId, requestPharmacyStaffMembership));
    }

    @GetMapping("/pharmacies/{pharmacyId}/staff")
    public ResponseEntity<List<PharmacyStaffMembershipResponse>> getPharmacyStaff(
            @PathVariable Long pharmacyId,
            @RequestParam(required = false) Boolean active
    ) {
        return ResponseEntity
                .ok(pharmacyStaffMembershipService.getPharmacyStaff(pharmacyId, active));
    }

    @GetMapping("/pharmacy-staff-memberships/{membershipId}")
    public ResponseEntity<PharmacyStaffMembershipResponse> getStaffMembership(
            @PathVariable Long membershipId
    ) {
        return ResponseEntity
                .ok(pharmacyStaffMembershipService.getStaffMembership(membershipId));
    }

    @PatchMapping("/pharmacy-staff-memberships/{membershipId}/role")
    public ResponseEntity<PharmacyStaffMembershipResponse> changeStaffRole(
            @PathVariable Long membershipId,
            @Valid @RequestBody ChangePharmacyStaffRoleRequest requestPharmacyStaffRole
    ) {
        return ResponseEntity
                .ok(pharmacyStaffMembershipService.changeStaffRole(membershipId, requestPharmacyStaffRole));
    }

    @PatchMapping("/pharmacy-staff-memberships/{membershipId}/end")
    public ResponseEntity<PharmacyStaffMembershipResponse> endStaffMembership(
            @PathVariable Long membershipId
    ) {
        return ResponseEntity
                .ok(pharmacyStaffMembershipService.endStaffMembership(membershipId));
    }
}
