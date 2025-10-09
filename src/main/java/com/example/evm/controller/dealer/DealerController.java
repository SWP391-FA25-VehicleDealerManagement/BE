package com.example.evm.controller.dealer;

import com.example.evm.dto.auth.ApiResponse;
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
        return ResponseEntity.ok(new ApiResponse<>(true, "Dealers retrieved successfully", dealers));
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
    @PreAuthorize("hasAnyAuthority('ADMIN', 'EVM_STAFF', 'DEALER_STAFF', 'DEALER_MANAGER')")
    public ResponseEntity<ApiResponse<Dealer>> createDealer(@Valid @RequestBody Dealer dealer) {
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
        return ResponseEntity.ok(new ApiResponse<>(true, "Dealer deleted successfully", null));
    }


}