package com.coderaah.medtrack.pharmacy.controller;

import com.coderaah.medtrack.pharmacy.domain.InventoryMovementType;
import com.coderaah.medtrack.pharmacy.domain.PharmacyInventory;
import com.coderaah.medtrack.pharmacy.dto.InventoryItemResponse;
import com.coderaah.medtrack.pharmacy.dto.InventoryMovementResponse;
import com.coderaah.medtrack.pharmacy.exception.DuplicateInventoryItemException;
import com.coderaah.medtrack.pharmacy.exception.InvalidStockOperationException;
import com.coderaah.medtrack.pharmacy.exception.InventoryItemNotFoundException;
import com.coderaah.medtrack.pharmacy.service.InventoryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(InventoryController.class)
public class InventoryControllerTest {

    private static final String BASE = "/api/pharmacies/1/inventory";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private InventoryService inventoryService;

    private InventoryItemResponse sampleItem() {
        InventoryItemResponse response = new InventoryItemResponse();
        response.setId(5L);
        response.setPharmacyId(1L);
        response.setMedicationId(10L);
        response.setMedicationName("Ibuprofen");
        response.setQuantityOnHand(20);
        response.setAvailableQuantity(20);
        response.setReorderLevel(5);
        return response;
    }

    // ---------- add ----------

    @Test
    void addInventoryItem_returnsCreated() throws Exception {

        when(inventoryService.addInventoryItem(eq(1L), any())).thenReturn(sampleItem());

        mockMvc.perform(post(BASE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "medicationId": 10, "initialQuantity": 20, "reorderLevel": 5 }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.medicationName").value("Ibuprofen"))
                .andExpect(jsonPath("$.quantityOnHand").value(20));
    }

    @Test
    void addInventoryItem_returnsBadRequest_whenInitialQuantityNegative() throws Exception {

        mockMvc.perform(post(BASE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "medicationId": 10, "initialQuantity": -1 }
                                """))
                .andExpect(status().isBadRequest());

        verify(inventoryService, never()).addInventoryItem(anyLong(), any());
    }

    @Test
    void addInventoryItem_returnsBadRequest_whenMedicationIdMissing() throws Exception {

        mockMvc.perform(post(BASE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "initialQuantity": 20 }
                                """))
                .andExpect(status().isBadRequest());

        verify(inventoryService, never()).addInventoryItem(anyLong(), any());
    }

    @Test
    void addInventoryItem_returnsConflict_whenDuplicate() throws Exception {

        when(inventoryService.addInventoryItem(eq(1L), any()))
                .thenThrow(new DuplicateInventoryItemException("duplicate"));

        mockMvc.perform(post(BASE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "medicationId": 10, "initialQuantity": 20 }
                                """))
                .andExpect(status().isConflict());
    }

    // ---------- adjustments ----------

    @Test
    void adjustStock_returnsOk() throws Exception {

        when(inventoryService.adjustStock(eq(1L), eq(5L), any())).thenReturn(sampleItem());

        mockMvc.perform(post(BASE + "/5/adjustments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "quantityChange": 5, "reason": "Delivery" }
                                """))
                .andExpect(status().isOk());
    }

    @Test
    void adjustStock_returnsBadRequest_whenReasonMissing() throws Exception {

        mockMvc.perform(post(BASE + "/5/adjustments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "quantityChange": 5, "reason": "   " }
                                """))
                .andExpect(status().isBadRequest());

        verify(inventoryService, never()).adjustStock(anyLong(), anyLong(), any());
    }

    @Test
    void adjustStock_returnsConflict_whenStockWouldBecomeNegative() throws Exception {

        when(inventoryService.adjustStock(eq(1L), eq(5L), any()))
                .thenThrow(new InvalidStockOperationException("Stock cannot become negative"));

        mockMvc.perform(post(BASE + "/5/adjustments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "quantityChange": -100, "reason": "Too much" }
                                """))
                .andExpect(status().isConflict());
    }

    @Test
    void adjustStock_returnsConflict_whenConcurrentModification() throws Exception {

        when(inventoryService.adjustStock(eq(1L), eq(5L), any()))
                .thenThrow(new ObjectOptimisticLockingFailureException(PharmacyInventory.class, 5L));

        mockMvc.perform(post(BASE + "/5/adjustments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "quantityChange": 5, "reason": "Delivery" }
                                """))
                .andExpect(status().isConflict());
    }

    // ---------- reading ----------

    @Test
    void getLowStockItems_routesToLowStockEndpoint() throws Exception {

        when(inventoryService.getLowStockItems(1L)).thenReturn(List.of(sampleItem()));

        mockMvc.perform(get(BASE + "/low-stock"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].medicationName").value("Ibuprofen"));

        verify(inventoryService, never()).getInventoryItem(anyLong(), anyLong());
    }

    @Test
    void getInventoryItem_returnsNotFound_whenMissing() throws Exception {

        when(inventoryService.getInventoryItem(1L, 99L))
                .thenThrow(new InventoryItemNotFoundException("not found"));

        mockMvc.perform(get(BASE + "/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getMovements_returnsHistory() throws Exception {

        InventoryMovementResponse movement = new InventoryMovementResponse();
        movement.setId(1L);
        movement.setMovementType(InventoryMovementType.RESTOCK);
        movement.setOnHandDelta(20);
        movement.setReason("Initial stock");

        when(inventoryService.getMovements(1L, 5L)).thenReturn(List.of(movement));

        mockMvc.perform(get(BASE + "/5/movements"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].movementType").value("RESTOCK"))
                .andExpect(jsonPath("$[0].onHandDelta").value(20));
    }

    // ---------- reorder level ----------

    @Test
    void updateReorderLevel_returnsBadRequest_whenBodyEmpty() throws Exception {

        mockMvc.perform(patch(BASE + "/5/reorder-level")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());

        verify(inventoryService, never()).updateReorderLevel(anyLong(), anyLong(), anyInt());
    }
}