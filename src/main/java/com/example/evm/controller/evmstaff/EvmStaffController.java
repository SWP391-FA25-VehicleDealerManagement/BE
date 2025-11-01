package com.example.evm.controller.evmstaff;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import com.example.evm.service.evmstaff.EvmStaffService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import com.example.evm.dto.auth.ApiResponse;
import com.example.evm.entity.user.User;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import java.util.List;
import com.example.evm.dto.evmstaff.EvmStaffRequest;
@RestController
@RequestMapping("/api/evmstaff")
@RequiredArgsConstructor
public class EvmStaffController {
@Autowired
private  EvmStaffService evmStaffService;
@GetMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<ApiResponse<List<User>>> getAllEvmStaff() {
        List<User> evmStaff = evmStaffService.getAllEvmStaff();
        return ResponseEntity.ok(new ApiResponse<>(true, "EVM Staff retrieved successfully", evmStaff));
    }
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<ApiResponse<User>> getEvmStaffById(@PathVariable Long id) {
        User evmStaff = evmStaffService.getEvmStaffById(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "EVM Staff retrieved successfully", evmStaff));
    }
    @PostMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<ApiResponse<User>> createEvmStaff(@RequestBody EvmStaffRequest evmStaffrequest) {
        User evmStaff = new User();
        evmStaff.setUserName(evmStaffrequest.getUserName());
        evmStaff.setFullName(evmStaffrequest.getFullName());
        evmStaff.setPassword(evmStaffrequest.getPassword());
        evmStaff.setEmail(evmStaffrequest.getEmail());
        evmStaff.setPhone(evmStaffrequest.getPhone());
        User createdEvmStaff = evmStaffService.createEvmStaff(evmStaff);
        return ResponseEntity.ok(new ApiResponse<>(true, "EVM Staff created successfully", createdEvmStaff));
    }
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<ApiResponse<User>> updateEvmStaff(@PathVariable Long id, @RequestBody User evmStaff) {
        User updatedEvmStaff = evmStaffService.updateEvmStaff(id, evmStaff);
        return ResponseEntity.ok(new ApiResponse<>(true, "EVM Staff updated successfully", updatedEvmStaff));
    }
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteEvmStaff(@PathVariable Long id) {
        evmStaffService.deleteEvmStaff(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "EVM Staff deleted successfully", null));
    }
}
