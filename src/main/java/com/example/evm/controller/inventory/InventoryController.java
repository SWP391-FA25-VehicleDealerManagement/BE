package com.example.evm.controller.inventory;

import com.example.evm.dto.inventory.InventoryResponse;
import com.example.evm.entity.inventory.InventoryStock;
import com.example.evm.service.inventory.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @PreAuthorize("hasAnyRole('ADMIN', 'EVM_STAFF', 'DEALER_MANAGER')")
    @GetMapping
    public ResponseEntity<List<InventoryResponse>> getAll() {
        return ResponseEntity.ok(inventoryService.getAll());
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'EVM_STAFF')")
    @PostMapping
    public ResponseEntity<String> addStock(@RequestBody InventoryStock stock) {
        inventoryService.addStock(stock);
        return ResponseEntity.ok("Inventory added successfully");
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'EVM_STAFF', 'DEALER_MANAGER')")
    @PutMapping("/{id}")
    public ResponseEntity<String> updateStock(@PathVariable Integer id, @RequestBody InventoryStock stock) {
        inventoryService.updateStock(id, stock);
        return ResponseEntity.ok("Inventory updated successfully");
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'EVM_STAFF')")
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteStock(@PathVariable Integer id) {
        inventoryService.deleteStock(id);
        return ResponseEntity.ok("Inventory deleted successfully");
    }
}
