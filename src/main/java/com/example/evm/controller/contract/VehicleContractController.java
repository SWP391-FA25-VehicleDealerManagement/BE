package com.example.evm.controller.contract;

import com.example.evm.dto.auth.ApiResponse;
import com.example.evm.dto.contract.VehicleContractRequest;
import com.example.evm.dto.contract.VehicleContractResponse;
import com.example.evm.entity.contract.VehicleContract;
import com.example.evm.service.contract.VehicleContractService;
import com.example.evm.service.storage.FileStorageService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.core.io.Resource;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/contracts")
@RequiredArgsConstructor
public class VehicleContractController {

    private final VehicleContractService vehicleContractService;
    private final FileStorageService fileStorageService;

    /**
     * Tạo hợp đồng mới
     */
    @PostMapping
    @PreAuthorize("hasAnyAuthority('ADMIN', 'DEALER_MANAGER', 'DEALER_STAFF')")
    public ResponseEntity<ApiResponse<VehicleContractResponse>> createContract(
            @Valid @RequestBody VehicleContractRequest request) {
        VehicleContractResponse response = vehicleContractService.createContract(request);
        return ResponseEntity.ok(new ApiResponse<>(true, "Contract created successfully", response));
    }

    @PutMapping("/{id}/sign")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'EVM_STAFF', 'DEALER_MANAGER', 'DEALER_STAFF')")
    public ResponseEntity<ApiResponse<VehicleContractResponse>> signContract(
            @PathVariable Long id) {
        
        VehicleContractResponse signedContract = vehicleContractService.signContract(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Hợp đồng đã được ký thành công.", signedContract));
    }

    /**
     * Lấy danh sách hợp đồng
     */
    @GetMapping
    @PreAuthorize("hasAnyAuthority('ADMIN', 'EVM_STAFF', 'DEALER_MANAGER', 'DEALER_STAFF')")
    public ResponseEntity<ApiResponse<List<VehicleContractResponse>>> getAllContracts() {
        List<VehicleContractResponse> response = vehicleContractService.getAllContracts();
        return ResponseEntity.ok(new ApiResponse<>(true, "All contracts retrieved successfully", response));
    }

    /**
     * Lấy hợp đồng theo ID
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'EVM_STAFF', 'DEALER_MANAGER', 'DEALER_STAFF')")
    public ResponseEntity<ApiResponse<VehicleContractResponse>> getContractById(@PathVariable Long id) {
        VehicleContractResponse response = vehicleContractService.getContractById(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Contract retrieved successfully", response));
    }

    /**
     * Lấy tất cả hợp đồng theo dealerId
     */
    @GetMapping("/dealer/{dealerId}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'EVM_STAFF', 'DEALER_MANAGER', 'DEALER_STAFF')")
    public ResponseEntity<ApiResponse<List<VehicleContractResponse>>> getContractsByDealerId(@PathVariable Long dealerId) {
        List<VehicleContractResponse> response = vehicleContractService.getContractsByDealerId(dealerId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Danh sách hợp đồng của đại lý", response));
    }

    /**
     * Tải file hợp đồng
     */
    @GetMapping("/files/{id}")
    public ResponseEntity<Resource> getContractFile(@PathVariable Long id) {
        //  gọi đúng tên hàm trong service
        VehicleContract contract = vehicleContractService.getContractEntityById(id); 

        //  tạo tên file
        String filename = "Contract_" + contract.getContractId() + ".docx";

        //  tải file từ thư mục uploads/contracts
        Resource file = fileStorageService.load("contracts", filename);

        //  trả về với content-type Word đúng chuẩn
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.wordprocessingml.document"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(file);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'EVM_STAFF', 'DEALER_MANAGER')")
    public ResponseEntity<ApiResponse<Void>> deleteContract(
            @PathVariable Long id) {
        
        vehicleContractService.deleteDraftContract(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Hợp đồng nháp đã được xóa thành công.", null));
    }


}
