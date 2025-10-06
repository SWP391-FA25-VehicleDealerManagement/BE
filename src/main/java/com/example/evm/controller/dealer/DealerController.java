package com.example.evm.controller.dealer;

import com.example.evm.dto.auth.ApiResponse;
import com.example.evm.entity.dealer.Dealer;

import com.example.evm.service.dealer.DealerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/dealers")
@RequiredArgsConstructor
public class DealerController {

    private final DealerService dealerService;

    // Removed the broad GetAll; use name-based lookup below

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_EVM_STAFF', 'ROLE_DEALER_STAFF', 'ROLE_DEALER_MANAGER')")
    public ResponseEntity<ApiResponse<Dealer>> getDealerById(@PathVariable Long id) {
        Dealer dealer = dealerService.getDealerById(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Dealer retrieved successfully", dealer));
    }

    @PostMapping("/create")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_EVM_STAFF', 'ROLE_DEALER_STAFF', 'ROLE_DEALER_MANAGER')")
    public ResponseEntity<ApiResponse<Dealer>> createDealer(@RequestBody Dealer dealer) {
        Dealer createdDealer = dealerService.createDealer(dealer);
        return ResponseEntity.ok(new ApiResponse<>(true, "Dealer created successfully", createdDealer));
    }
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_EVM_STAFF')")
    public ResponseEntity<ApiResponse<Dealer>> updateDealer(@PathVariable Long id, @RequestBody Dealer dealer) {
        dealer.setDealerId(id);
        Dealer updatedDealer = dealerService.updateDealer(dealer);
        return ResponseEntity.ok(new ApiResponse<>(true, "Dealer updated successfully", updatedDealer));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteDealer(@PathVariable Long id) {
        dealerService.deleteDealer(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Dealer deleted successfully", null));
    }


    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_EVM_STAFF', 'ROLE_DEALER_STAFF', 'ROLE_DEALER_MANAGER')")
    public ResponseEntity<ApiResponse<Dealer>> getDealerByName(@RequestParam String dealerName) {
        Dealer dealer = dealerService.getDealerByName(dealerName);
        return ResponseEntity.ok(new ApiResponse<>(true, "Dealer retrieved successfully", dealer));
    }
}