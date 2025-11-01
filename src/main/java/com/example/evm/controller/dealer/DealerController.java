package com.example.evm.controller.dealer;

import com.example.evm.dto.auth.ApiResponse;
import com.example.evm.dto.dealer.CreateDealerRequest;
import com.example.evm.entity.dealer.Dealer;

import com.example.evm.service.dealer.DealerService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

import java.util.List;

@RestController
@RequestMapping("/api/dealers")
@RequiredArgsConstructor
@Slf4j
public class DealerController {

    private final DealerService dealerService;

    @GetMapping("/all")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'EVM_STAFF', 'DEALER_STAFF', 'DEALER_MANAGER')")
    public ResponseEntity<ApiResponse<List<Dealer>>> getAllDealers() {
        List<Dealer> dealers = dealerService.getAllDealers();
        return ResponseEntity.ok(new ApiResponse<>(true, "Active dealers retrieved successfully", dealers));
    }

    @GetMapping("/inactive")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'EVM_STAFF')")
    public ResponseEntity<ApiResponse<List<Dealer>>> getInactiveDealers() {
        List<Dealer> inactive = dealerService.getInactiveDealers();
        return ResponseEntity.ok(new ApiResponse<>(true, "Inactive dealers retrieved successfully", inactive));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'EVM_STAFF', 'DEALER_STAFF', 'DEALER_MANAGER')")
    public ResponseEntity<ApiResponse<Dealer>> getDealerById(@PathVariable Long id) {
        Dealer dealer = dealerService.getDealerById(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Dealer retrieved successfully", dealer));
    }

    @GetMapping("/search")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'EVM_STAFF', 'DEALER_STAFF', 'DEALER_MANAGER')")
    public ResponseEntity<ApiResponse<Dealer>> getDealerByName(@RequestParam String name) {
        Dealer dealer = dealerService.getDealerByName(name);
        return ResponseEntity.ok(new ApiResponse<>(true, "Dealer retrieved successfully", dealer));
    }

    @PostMapping("/create")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'EVM_STAFF')")
    public ResponseEntity<ApiResponse<Dealer>> createDealer(@Valid @RequestBody CreateDealerRequest request) {
        // Convert DTO to Entity
        Dealer dealer = new Dealer();
        dealer.setDealerName(request.getDealerName());
        dealer.setPhone(request.getPhone());
        dealer.setAddress(request.getAddress());
        dealer.setCreatedBy(request.getCreatedBy());
        
        Dealer createdDealer = dealerService.createDealer(dealer);
        return ResponseEntity.ok(new ApiResponse<>(true, "Dealer created successfully", createdDealer));
    }

    @PutMapping("/update/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'EVM_STAFF')")
    public ResponseEntity<ApiResponse<Dealer>> updateDealer(@PathVariable Long id, @Valid @RequestBody Dealer dealer) {
        dealer.setDealerId(id);
        Dealer updatedDealer = dealerService.updateDealer(dealer);
        return ResponseEntity.ok(new ApiResponse<>(true, "Dealer updated successfully", updatedDealer));
    }

    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'EVM_STAFF')")
    public ResponseEntity<ApiResponse<Void>> deleteDealer(@PathVariable Long id) {
        dealerService.deleteDealer(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Dealer deactivated successfully", null));
    }

    @PutMapping("/activate/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'EVM_STAFF', 'DEALER_STAFF', 'DEALER_MANAGER')")
    public ResponseEntity<ApiResponse<Void>> activateDealer(@PathVariable Long id) {
        dealerService.activateDealer(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Dealer reactivated successfully", null));
    }

}