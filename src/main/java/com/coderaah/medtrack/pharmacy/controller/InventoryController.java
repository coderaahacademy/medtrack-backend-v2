package com.coderaah.medtrack.pharmacy.controller;

import com.coderaah.medtrack.pharmacy.dto.AddInventoryItemRequest;
import com.coderaah.medtrack.pharmacy.dto.InventoryItemResponse;
import com.coderaah.medtrack.pharmacy.dto.InventoryMovementResponse;
import com.coderaah.medtrack.pharmacy.dto.ReorderLevelRequest;
import com.coderaah.medtrack.pharmacy.dto.StockAdjustmentRequest;
import com.coderaah.medtrack.pharmacy.service.InventoryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/pharmacies/{pharmacyId}/inventory")
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @PostMapping
    public ResponseEntity<InventoryItemResponse> addInventoryItem(
            @PathVariable Long pharmacyId,
            @Valid @RequestBody AddInventoryItemRequest request) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(inventoryService.addInventoryItem(pharmacyId, request));
    }

    @GetMapping
    public ResponseEntity<List<InventoryItemResponse>> getInventory(@PathVariable Long pharmacyId) {
        return ResponseEntity.ok(inventoryService.getInventory(pharmacyId));
    }

    @GetMapping("/low-stock")
    public ResponseEntity<List<InventoryItemResponse>> getLowStockItems(@PathVariable Long pharmacyId) {
        return ResponseEntity.ok(inventoryService.getLowStockItems(pharmacyId));
    }

    @GetMapping("/{inventoryId}")
    public ResponseEntity<InventoryItemResponse> getInventoryItem(
            @PathVariable Long pharmacyId,
            @PathVariable Long inventoryId) {

        return ResponseEntity.ok(inventoryService.getInventoryItem(pharmacyId, inventoryId));
    }

    @PostMapping("/{inventoryId}/adjustments")
    public ResponseEntity<InventoryItemResponse> adjustStock(
            @PathVariable Long pharmacyId,
            @PathVariable Long inventoryId,
            @Valid @RequestBody StockAdjustmentRequest request) {

        return ResponseEntity.ok(inventoryService.adjustStock(pharmacyId, inventoryId, request));
    }

    @PatchMapping("/{inventoryId}/reorder-level")
    public ResponseEntity<InventoryItemResponse> updateReorderLevel(
            @PathVariable Long pharmacyId,
            @PathVariable Long inventoryId,
            @Valid @RequestBody ReorderLevelRequest request) {

        return ResponseEntity.ok(
                inventoryService.updateReorderLevel(pharmacyId, inventoryId, request.getReorderLevel()));
    }

    @GetMapping("/{inventoryId}/movements")
    public ResponseEntity<List<InventoryMovementResponse>> getMovements(
            @PathVariable Long pharmacyId,
            @PathVariable Long inventoryId) {

        return ResponseEntity.ok(inventoryService.getMovements(pharmacyId, inventoryId));
    }
}