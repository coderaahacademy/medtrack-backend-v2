package com.coderaah.medtrack.pharmacy.service;

import com.coderaah.medtrack.medication.domain.DosageForm;
import com.coderaah.medtrack.medication.domain.Medication;
import com.coderaah.medtrack.medication.exception.MedicationNotFoundException;
import com.coderaah.medtrack.medication.repository.MedicationRepository;
import com.coderaah.medtrack.pharmacy.domain.InventoryMovement;
import com.coderaah.medtrack.pharmacy.domain.InventoryMovementType;
import com.coderaah.medtrack.pharmacy.domain.Pharmacy;
import com.coderaah.medtrack.pharmacy.domain.PharmacyInventory;
import com.coderaah.medtrack.pharmacy.dto.AddInventoryItemRequest;
import com.coderaah.medtrack.pharmacy.dto.InventoryItemResponse;
import com.coderaah.medtrack.pharmacy.dto.StockAdjustmentRequest;
import com.coderaah.medtrack.pharmacy.exception.DuplicateInventoryItemException;
import com.coderaah.medtrack.pharmacy.exception.InvalidStockOperationException;
import com.coderaah.medtrack.pharmacy.exception.InventoryItemNotFoundException;
import com.coderaah.medtrack.pharmacy.exception.PharmacyNotFoundException;
import com.coderaah.medtrack.pharmacy.repository.InventoryMovementRepository;
import com.coderaah.medtrack.pharmacy.repository.PharmacyInventoryRepository;
import com.coderaah.medtrack.pharmacy.repository.PharmacyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class InventoryServiceTest {

    private PharmacyInventoryRepository inventoryRepository;
    private InventoryMovementRepository movementRepository;
    private PharmacyRepository pharmacyRepository;
    private MedicationRepository medicationRepository;
    private InventoryService inventoryService;

    private Pharmacy pharmacy;
    private Medication medication;

    @BeforeEach
    void setUp() {
        inventoryRepository = mock(PharmacyInventoryRepository.class);
        movementRepository = mock(InventoryMovementRepository.class);
        pharmacyRepository = mock(PharmacyRepository.class);
        medicationRepository = mock(MedicationRepository.class);
        inventoryService = new InventoryService(
                inventoryRepository, movementRepository, pharmacyRepository, medicationRepository);

        pharmacy = new Pharmacy("Apotheke Nord", "L-123");
        pharmacy.setId(1L);

        medication = new Medication("Ibuprofen", "400", "mg", DosageForm.TABLET);
        medication.setId(10L);

        when(inventoryRepository.save(any(PharmacyInventory.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    private PharmacyInventory inventory(int onHand, int reserved, Integer reorderLevel) {
        PharmacyInventory inventory = new PharmacyInventory(pharmacy, medication, onHand, reserved, reorderLevel);
        inventory.setId(5L);
        when(inventoryRepository.findByIdAndPharmacy_Id(5L, 1L)).thenReturn(Optional.of(inventory));
        return inventory;
    }

    private StockAdjustmentRequest adjustment(int change, String reason) {
        StockAdjustmentRequest request = new StockAdjustmentRequest();
        request.setQuantityChange(change);
        request.setReason(reason);
        return request;
    }

    private AddInventoryItemRequest addRequest(int initialQuantity) {
        AddInventoryItemRequest request = new AddInventoryItemRequest();
        request.setMedicationId(10L);
        request.setInitialQuantity(initialQuantity);
        request.setReorderLevel(5);
        return request;
    }

    // ---------- add medication to inventory ----------

    @Test
    void addInventoryItem_createsItemAndRestockMovement() {

        when(pharmacyRepository.findById(1L)).thenReturn(Optional.of(pharmacy));
        when(medicationRepository.findById(10L)).thenReturn(Optional.of(medication));
        when(inventoryRepository.existsByPharmacy_IdAndMedication_Id(1L, 10L)).thenReturn(false);

        InventoryItemResponse result = inventoryService.addInventoryItem(1L, addRequest(20));

        assertEquals(20, result.getQuantityOnHand());
        assertEquals(0, result.getQuantityReserved());
        assertEquals("Ibuprofen", result.getMedicationName());

        ArgumentCaptor<InventoryMovement> captor = ArgumentCaptor.forClass(InventoryMovement.class);
        verify(movementRepository).save(captor.capture());
        assertEquals(InventoryMovementType.RESTOCK, captor.getValue().getMovementType());
        assertEquals(20, captor.getValue().getOnHandDelta());
    }

    @Test
    void addInventoryItem_withZeroQuantity_createsNoMovement() {

        when(pharmacyRepository.findById(1L)).thenReturn(Optional.of(pharmacy));
        when(medicationRepository.findById(10L)).thenReturn(Optional.of(medication));

        inventoryService.addInventoryItem(1L, addRequest(0));

        verify(movementRepository, never()).save(any());
    }

    @Test
    void addInventoryItem_throwsException_whenMedicationDoesNotExist() {

        when(pharmacyRepository.findById(1L)).thenReturn(Optional.of(pharmacy));
        when(medicationRepository.findById(10L)).thenReturn(Optional.empty());

        assertThrows(MedicationNotFoundException.class,
                () -> inventoryService.addInventoryItem(1L, addRequest(20)));

        verify(inventoryRepository, never()).save(any());
    }

    @Test
    void addInventoryItem_throwsException_whenMedicationAlreadyInInventory() {

        when(pharmacyRepository.findById(1L)).thenReturn(Optional.of(pharmacy));
        when(medicationRepository.findById(10L)).thenReturn(Optional.of(medication));
        when(inventoryRepository.existsByPharmacy_IdAndMedication_Id(1L, 10L)).thenReturn(true);

        assertThrows(DuplicateInventoryItemException.class,
                () -> inventoryService.addInventoryItem(1L, addRequest(20)));

        verify(inventoryRepository, never()).save(any());
    }

    @Test
    void addInventoryItem_throwsException_whenPharmacyDoesNotExist() {

        when(pharmacyRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(PharmacyNotFoundException.class,
                () -> inventoryService.addInventoryItem(1L, addRequest(20)));
    }

    // ---------- stock adjustments ----------

    @Test
    void adjustStock_increase_updatesStockAndRecordsMovement() {

        PharmacyInventory inventory = inventory(10, 0, null);

        InventoryItemResponse result = inventoryService.adjustStock(1L, 5L, adjustment(5, "  Delivery  "));

        assertEquals(15, result.getQuantityOnHand());
        assertEquals(15, inventory.getQuantityOnHand());

        ArgumentCaptor<InventoryMovement> captor = ArgumentCaptor.forClass(InventoryMovement.class);
        verify(movementRepository).save(captor.capture());
        assertEquals(InventoryMovementType.ADJUSTMENT, captor.getValue().getMovementType());
        assertEquals(5, captor.getValue().getOnHandDelta());
        assertEquals("Delivery", captor.getValue().getReason());
    }

    @Test
    void adjustStock_decrease_updatesStock() {

        inventory(10, 0, null);

        InventoryItemResponse result = inventoryService.adjustStock(1L, 5L, adjustment(-4, "Damaged"));

        assertEquals(6, result.getQuantityOnHand());
        verify(movementRepository).save(any(InventoryMovement.class));
    }

    @Test
    void adjustStock_throwsException_whenStockWouldBecomeNegative() {

        PharmacyInventory inventory = inventory(3, 0, null);

        assertThrows(InvalidStockOperationException.class,
                () -> inventoryService.adjustStock(1L, 5L, adjustment(-5, "Too much")));

        assertEquals(3, inventory.getQuantityOnHand());
        verify(inventoryRepository, never()).save(any());
        verify(movementRepository, never()).save(any());
    }

    @Test
    void adjustStock_throwsException_whenReservedWouldExceedOnHand() {

        PharmacyInventory inventory = inventory(10, 8, null);

        assertThrows(InvalidStockOperationException.class,
                () -> inventoryService.adjustStock(1L, 5L, adjustment(-5, "Correction")));

        assertEquals(10, inventory.getQuantityOnHand());
        verify(movementRepository, never()).save(any());
    }

    @Test
    void adjustStock_throwsException_whenChangeIsZero() {

        assertThrows(InvalidStockOperationException.class,
                () -> inventoryService.adjustStock(1L, 5L, adjustment(0, "Nothing")));

        verify(movementRepository, never()).save(any());
    }

    @Test
    void adjustStock_throwsException_whenItemBelongsToOtherPharmacy() {

        when(inventoryRepository.findByIdAndPharmacy_Id(5L, 2L)).thenReturn(Optional.empty());

        assertThrows(InventoryItemNotFoundException.class,
                () -> inventoryService.adjustStock(2L, 5L, adjustment(5, "Delivery")));
    }

    // ---------- reorder level & low stock ----------

    @Test
    void updateReorderLevel_changesLevelWithoutMovement() {

        inventory(10, 0, null);

        InventoryItemResponse result = inventoryService.updateReorderLevel(1L, 5L, 12);

        assertEquals(12, result.getReorderLevel());
        assertTrue(result.isLowStock());
        verify(movementRepository, never()).save(any());
    }

    @Test
    void lowStock_usesAvailableQuantity() {

        inventory(10, 6, 5);

        InventoryItemResponse result = inventoryService.getInventoryItem(1L, 5L);

        assertEquals(4, result.getAvailableQuantity());
        assertTrue(result.isLowStock());
    }

    @Test
    void lowStock_isFalse_whenNoReorderLevelConfigured() {

        inventory(0, 0, null);

        InventoryItemResponse result = inventoryService.getInventoryItem(1L, 5L);

        assertFalse(result.isLowStock());
    }

    // ---------- reading ----------

    @Test
    void getInventory_throwsException_whenPharmacyDoesNotExist() {

        when(pharmacyRepository.existsById(99L)).thenReturn(false);

        assertThrows(PharmacyNotFoundException.class,
                () -> inventoryService.getInventory(99L));
    }

    @Test
    void getMovements_throwsException_whenItemBelongsToOtherPharmacy() {

        when(inventoryRepository.findByIdAndPharmacy_Id(5L, 2L)).thenReturn(Optional.empty());

        assertThrows(InventoryItemNotFoundException.class,
                () -> inventoryService.getMovements(2L, 5L));

        verify(movementRepository, never()).findByInventory_IdOrderByCreatedAtAsc(any());
    }
}