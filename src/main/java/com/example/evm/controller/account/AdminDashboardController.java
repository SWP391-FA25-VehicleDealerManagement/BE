package com.example.evm.controller.account;

import com.example.evm.dto.auth.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/admin")
public class AdminDashboardController {

    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<String>> dashboard() {
        log.info("Admin dashboard accessed");
        return ResponseEntity.ok(new ApiResponse<>(true, "Welcome to Admin Dashboard!", "OK"));
    }
}
