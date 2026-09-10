package com.coderaah.medtrack.pharmacy.repository;

import com.coderaah.medtrack.pharmacy.domain.Pharmacy;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PharmacyRepository
        extends JpaRepository<Pharmacy, Long> {

    boolean existsByLicenseNumber(String licenseNumber);

    boolean existsByLicenseNumberAndIdNot(String licenseNumber, Long id);
}