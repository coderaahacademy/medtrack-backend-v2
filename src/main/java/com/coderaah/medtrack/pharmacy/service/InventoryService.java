package com.coderaah.medtrack.pharmacy.service;

import com.coderaah.medtrack.medication.domain.Medication;
import com.coderaah.medtrack.medication.exception.MedicationNotFoundException;
import com.coderaah.medtrack.medication.repository.MedicationRepository;
import com.coderaah.medtrack.pharmacy.domain.InventoryMovement;
import com.coderaah.medtrack.pharmacy.domain.InventoryMovementType;
import com.coderaah.medtrack.pharmacy.domain.Pharmacy;
import com.coderaah.medtrack.pharmacy.domain.PharmacyInventory;
import com.coderaah.medtrack.pharmacy.dto.AddInventoryItemRequest;
import com.coderaah.medtrack.pharmacy.dto.InventoryItemResponse;
import com.coderaah.medtrack.pharmacy.dto.InventoryMovementResponse;
import com.coderaah.medtrack.pharmacy.dto.StockAdjustmentRequest;
import com.coderaah.medtrack.pharmacy.exception.DuplicateInventoryItemException;
import com.coderaah.medtrack.pharmacy.exception.InvalidStockOperationException;
import com.coderaah.medtrack.pharmacy.exception.InventoryItemNotFoundException;
import com.coderaah.medtrack.pharmacy.exception.PharmacyNotFoundException;
import com.coderaah.medtrack.pharmacy.repository.InventoryMovementRepository;
import com.coderaah.medtrack.pharmacy.repository.PharmacyInventoryRepository;
import com.coderaah.medtrack.pharmacy.repository.PharmacyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class InventoryService {

    private final PharmacyInventoryRepository inventoryRepository;
    private final InventoryMovementRepository movementRepository;
    private final PharmacyRepository pharmacyRepository;
    private final MedicationRepository medicationRepository;

    public InventoryService(PharmacyInventoryRepository inventoryRepository,
                            InventoryMovementRepository movementRepository,
                            PharmacyRepository pharmacyRepository,
                            MedicationRepository medicationRepository) {
        this.inventoryRepository = inventoryRepository;
        this.movementRepository = movementRepository;
        this.pharmacyRepository = pharmacyRepository;
        this.medicationRepository = medicationRepository;
    }

    public InventoryItemResponse addInventoryItem(Long pharmacyId, AddInventoryItemRequest request) {

        Pharmacy pharmacy = pharmacyRepository.findById(pharmacyId)
                .orElseThrow(() -> new PharmacyNotFoundException(
                        "Pharmacy with id " + pharmacyId + " not found"));

        Medication medication = medicationRepository.findById(request.getMedicationId())
                .orElseThrow(() -> new MedicationNotFoundException(
                        "Medication with id " + request.getMedicationId() + " not found"));

        if (inventoryRepository.existsByPharmacy_IdAndMedication_Id(pharmacyId, request.getMedicationId())) {
            throw new DuplicateInventoryItemException(
                    "Medication " + request.getMedicationId() + " is already in the inventory of pharmacy " + pharmacyId);
        }

        PharmacyInventory inventory = new PharmacyInventory(
                pharmacy, medication, request.getInitialQuantity(), 0, request.getReorderLevel());

        PharmacyInventory savedInventory = inventoryRepository.save(inventory);

        if (request.getInitialQuantity() > 0) {
            recordMovement(savedInventory, InventoryMovementType.RESTOCK,
                    request.getInitialQuantity(), "Initial stock");
        }

        return toResponse(savedInventory);
    }

    @Transactional(readOnly = true)
    public List<InventoryItemResponse> getInventory(Long pharmacyId) {
        ensurePharmacyExists(pharmacyId);
        return inventoryRepository.findByPharmacy_Id(pharmacyId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public InventoryItemResponse getInventoryItem(Long pharmacyId, Long inventoryId) {
        return toResponse(findInventoryItemOrThrow(pharmacyId, inventoryId));
    }

    public InventoryItemResponse adjustStock(Long pharmacyId, Long inventoryId, StockAdjustmentRequest request) {

        int change = request.getQuantityChange();
        if (change == 0) {
            throw new InvalidStockOperationException("Quantity change must not be zero");
        }

        PharmacyInventory inventory = findInventoryItemOrThrow(pharmacyId, inventoryId);

        int newOnHand = inventory.getQuantityOnHand() + change;

        if (newOnHand < 0) {
            throw new InvalidStockOperationException(
                    "Stock cannot become negative. On hand: " + inventory.getQuantityOnHand()
                            + ", requested change: " + change);
        }

        if (newOnHand < inventory.getQuantityReserved()) {
            throw new InvalidStockOperationException(
                    "Reserved quantity (" + inventory.getQuantityReserved()
                            + ") cannot exceed quantity on hand (" + newOnHand + ")");
        }

        inventory.setQuantityOnHand(newOnHand);
        PharmacyInventory savedInventory = inventoryRepository.save(inventory);

        recordMovement(savedInventory, InventoryMovementType.ADJUSTMENT, change, request.getReason().trim());

        return toResponse(savedInventory);
    }

    public InventoryItemResponse updateReorderLevel(Long pharmacyId, Long inventoryId, int reorderLevel) {

        PharmacyInventory inventory = findInventoryItemOrThrow(pharmacyId, inventoryId);

        inventory.setReorderLevel(reorderLevel);

        return toResponse(inventoryRepository.save(inventory));
    }

    @Transactional(readOnly = true)
    public List<InventoryItemResponse> getLowStockItems(Long pharmacyId) {
        ensurePharmacyExists(pharmacyId);
        return inventoryRepository.findLowStock(pharmacyId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<InventoryMovementResponse> getMovements(Long pharmacyId, Long inventoryId) {
        findInventoryItemOrThrow(pharmacyId, inventoryId);
        return movementRepository.findByInventory_IdOrderByCreatedAtAsc(inventoryId)
                .stream()
                .map(this::toMovementResponse)
                .toList();
    }

    private void recordMovement(PharmacyInventory inventory, InventoryMovementType type,
                                int onHandDelta, String reason) {
        InventoryMovement movement = new InventoryMovement(
                inventory, type, onHandDelta, 0, null, null, reason);
        movementRepository.save(movement);
    }

    private PharmacyInventory findInventoryItemOrThrow(Long pharmacyId, Long inventoryId) {
        return inventoryRepository.findByIdAndPharmacy_Id(inventoryId, pharmacyId)
                .orElseThrow(() -> new InventoryItemNotFoundException(
                        "Inventory item " + inventoryId + " not found in pharmacy " + pharmacyId));
    }

    private void ensurePharmacyExists(Long pharmacyId) {
        if (!pharmacyRepository.existsById(pharmacyId)) {
            throw new PharmacyNotFoundException("Pharmacy with id " + pharmacyId + " not found");
        }
    }

    private InventoryItemResponse toResponse(PharmacyInventory inventory) {
        int available = inventory.getQuantityOnHand() - inventory.getQuantityReserved();
        Integer reorderLevel = inventory.getReorderLevel();

        InventoryItemResponse response = new InventoryItemResponse();
        response.setId(inventory.getId());
        response.setPharmacyId(inventory.getPharmacy().getId());
        response.setMedicationId(inventory.getMedication().getId());
        response.setMedicationName(inventory.getMedication().getGenericName());
        response.setQuantityOnHand(inventory.getQuantityOnHand());
        response.setQuantityReserved(inventory.getQuantityReserved());
        response.setAvailableQuantity(available);
        response.setReorderLevel(reorderLevel);
        response.setLowStock(reorderLevel != null && available <= reorderLevel);
        return response;
    }

    private InventoryMovementResponse toMovementResponse(InventoryMovement movement) {
        InventoryMovementResponse response = new InventoryMovementResponse();
        response.setId(movement.getId());
        response.setMovementType(movement.getMovementType());
        response.setOnHandDelta(movement.getOnHandDelta());
        response.setReservedDelta(movement.getReservedDelta());
        response.setReason(movement.getReason());
        response.setCreatedAt(movement.getCreatedAt());
        return response;
    }
}