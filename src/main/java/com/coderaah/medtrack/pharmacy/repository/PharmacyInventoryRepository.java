package com.coderaah.medtrack.pharmacy.repository;

import com.coderaah.medtrack.pharmacy.domain.PharmacyInventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PharmacyInventoryRepository extends JpaRepository<PharmacyInventory, Long> {

    boolean existsByPharmacy_IdAndMedication_Id(Long pharmacyId, Long medicationId);

    List<PharmacyInventory> findByPharmacy_Id(Long pharmacyId);

    Optional<PharmacyInventory> findByIdAndPharmacy_Id(Long id, Long pharmacyId);

    @Query("""
            SELECT i FROM PharmacyInventory i
            WHERE i.pharmacy.id = :pharmacyId
              AND i.reorderLevel IS NOT NULL
              AND (i.quantityOnHand - i.quantityReserved) <= i.reorderLevel
            """)
    List<PharmacyInventory> findLowStock(@Param("pharmacyId") Long pharmacyId);
}