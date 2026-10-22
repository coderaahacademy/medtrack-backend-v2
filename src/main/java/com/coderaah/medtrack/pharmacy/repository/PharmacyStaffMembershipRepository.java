package com.coderaah.medtrack.pharmacy.repository;

import com.coderaah.medtrack.pharmacy.domain.PharmacyStaffMembership;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PharmacyStaffMembershipRepository extends JpaRepository<PharmacyStaffMembership, Long> {

    boolean existsByUserAccount_IdAndPharmacy_IdAndActiveTrue(Long userId, Long pharmacyId);


    List<PharmacyStaffMembership> findByPharmacy_Id(Long pharmacyId);

    List<PharmacyStaffMembership> findByPharmacy_IdAndActiveTrue(Long pharmacyId);
}
