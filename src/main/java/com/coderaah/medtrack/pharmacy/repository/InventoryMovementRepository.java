package com.coderaah.medtrack.pharmacy.repository;

import com.coderaah.medtrack.pharmacy.domain.InventoryMovement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InventoryMovementRepository extends JpaRepository<InventoryMovement, Long> {

    List<InventoryMovement> findByInventory_IdOrderByCreatedAtAsc(Long inventoryId);
}