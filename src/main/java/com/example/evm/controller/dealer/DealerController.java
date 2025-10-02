package com.example.evm.controller.dealer;

import com.example.evm.dto.auth.ApiResponse;
import com.example.evm.entity.dealer.Dealer;

import com.example.evm.service.dealer.DealerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/dealers")
@RequiredArgsConstructor
public class DealerController {

    private final DealerService dealerService;

    @GetMapping
    @PreAuthorize("hasAnyAuthority('Admin', 'EVM Staff', 'Dealer Staff', 'Dealer Manager')")
    public ResponseEntity<ApiResponse<List<Dealer>>> getAllDealers() {
        List<Dealer> dealers = dealerService.getAllDealers();
        return ResponseEntity.ok(new ApiResponse<>(true, "Dealers retrieved successfully", dealers));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('Admin', 'EVM Staff', 'Dealer Staff', 'Dealer Manager')")
    public ResponseEntity<ApiResponse<Dealer>> getDealerById(@PathVariable Integer id) {
        Dealer dealer = dealerService.getDealerById(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Dealer retrieved successfully", dealer));
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('Admin', 'EVM Staff')")
    public ResponseEntity<ApiResponse<Dealer>> createDealer(@RequestBody Dealer dealer) {
        Dealer createdDealer = dealerService.createDealer(dealer);
        return ResponseEntity.ok(new ApiResponse<>(true, "Dealer created successfully", createdDealer));
    }
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('Admin', 'EVM Staff')")
    public ResponseEntity<ApiResponse<Dealer>> updateDealer(@PathVariable Integer id, @RequestBody Dealer dealer) {
        dealer.setDealerId(id);
        Dealer updatedDealer = dealerService.updateDealer(dealer);
        return ResponseEntity.ok(new ApiResponse<>(true, "Dealer updated successfully", updatedDealer));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('Admin')")

    public ResponseEntity<ApiResponse<Void>> deleteDealer(@PathVariable Integer id) {
        dealerService.deleteDealer(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Dealer deleted successfully", null));
    }
}