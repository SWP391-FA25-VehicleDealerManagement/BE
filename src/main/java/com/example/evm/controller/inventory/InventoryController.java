package com.example.evm.controller.inventory;

import com.example.evm.dto.inventory.InventoryResponse;
import com.example.evm.entity.inventory.InventoryStock;
import com.example.evm.service.inventory.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_EVM_STAFF', 'ROLE_DEALER_STAFF', 'ROLE_DEALER_MANAGER')")
    @GetMapping
    public ResponseEntity<List<InventoryResponse>> getAll() {
        return ResponseEntity.ok(inventoryService.getAll());
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('EVM_STAFF')")
    @PostMapping
    public ResponseEntity<String> addStock(@RequestBody InventoryStock stock) {
        inventoryService.addStock(stock);
        return ResponseEntity.ok("Inventory added successfully");
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('EVM_STAFF')")
    @PutMapping("/{id}")
    public ResponseEntity<String> updateStock(@PathVariable Integer id, @RequestBody InventoryStock stock) {
        inventoryService.updateStock(id, stock);
        return ResponseEntity.ok("Inventory updated successfully");
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('EVM_STAFF')")
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteStock(@PathVariable Integer id) {
        inventoryService.deleteStock(id);
        return ResponseEntity.ok("Inventory deleted successfully");
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('EVM_STAFF')")
    @PostMapping("/allocate")
    public ResponseEntity<Map<String, String>> allocateVehicle(@RequestBody Map<String, Integer> request) {
        String message = inventoryService.allocateVehicleToDealer(
                request.get("vehicleId"), request.get("dealerId"), request.get("quantity")
        );
        return ResponseEntity.ok(Map.of("message", message));
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('EVM_STAFF')")
    @PostMapping("/recall")
    public ResponseEntity<Map<String, String>> recallVehicle(@RequestBody Map<String, Integer> request) {
        String message = inventoryService.recallVehicleFromDealer(
                request.get("vehicleId"), request.get("dealerId"), request.get("quantity")
        );
        return ResponseEntity.ok(Map.of("message", message));
    }
}
