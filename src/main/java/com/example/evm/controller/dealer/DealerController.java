package com.example.evm.controller.dealer;

import com.example.evm.dto.auth.ApiResponse;
import com.example.evm.entity.dealer.Dealer;

import com.example.evm.service.dealer.DealerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

<<<<<<< HEAD
=======
import java.util.List;
>>>>>>> 54ac894e9c24c5857ad6736606c5e3f39b001e8d

@RestController
@RequestMapping("/api/dealers")
@RequiredArgsConstructor
public class DealerController {

    private final DealerService dealerService;

<<<<<<< HEAD
    // Removed the broad GetAll; use name-based lookup below

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_EVM_STAFF', 'ROLE_DEALER_STAFF', 'ROLE_DEALER_MANAGER')")
    public ResponseEntity<ApiResponse<Dealer>> getDealerById(@PathVariable Long id) {
=======
    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_EVM_STAFF', 'ROLE_DEALER_STAFF', 'ROLE_DEALER_MANAGER')")
    public ResponseEntity<ApiResponse<List<Dealer>>> getAllDealers() {
        List<Dealer> dealers = dealerService.getAllDealers();
        return ResponseEntity.ok(new ApiResponse<>(true, "Dealers retrieved successfully", dealers));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_EVM_STAFF', 'ROLE_DEALER_STAFF', 'ROLE_DEALER_MANAGER')")
    public ResponseEntity<ApiResponse<Dealer>> getDealerById(@PathVariable Integer id) {
>>>>>>> 54ac894e9c24c5857ad6736606c5e3f39b001e8d
        Dealer dealer = dealerService.getDealerById(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Dealer retrieved successfully", dealer));
    }

<<<<<<< HEAD
    @PostMapping("/create")
=======
    @PostMapping
>>>>>>> 54ac894e9c24c5857ad6736606c5e3f39b001e8d
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_EVM_STAFF', 'ROLE_DEALER_STAFF', 'ROLE_DEALER_MANAGER')")
    public ResponseEntity<ApiResponse<Dealer>> createDealer(@RequestBody Dealer dealer) {
        Dealer createdDealer = dealerService.createDealer(dealer);
        return ResponseEntity.ok(new ApiResponse<>(true, "Dealer created successfully", createdDealer));
    }
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_EVM_STAFF')")
<<<<<<< HEAD
    public ResponseEntity<ApiResponse<Dealer>> updateDealer(@PathVariable Long id, @RequestBody Dealer dealer) {
=======
    public ResponseEntity<ApiResponse<Dealer>> updateDealer(@PathVariable Integer id, @RequestBody Dealer dealer) {
>>>>>>> 54ac894e9c24c5857ad6736606c5e3f39b001e8d
        dealer.setDealerId(id);
        Dealer updatedDealer = dealerService.updateDealer(dealer);
        return ResponseEntity.ok(new ApiResponse<>(true, "Dealer updated successfully", updatedDealer));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN')")
<<<<<<< HEAD
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
=======

    public ResponseEntity<ApiResponse<Void>> deleteDealer(@PathVariable Integer id) {
        dealerService.deleteDealer(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Dealer deleted successfully", null));
    }
>>>>>>> 54ac894e9c24c5857ad6736606c5e3f39b001e8d
}