package com.example.evm.service.vehicle;

import com.example.evm.dto.vehicle.StockSummaryResponse;
import com.example.evm.dto.vehicle.VehicleFullResponse;
import com.example.evm.dto.vehicle.VehicleRequest;
import com.example.evm.dto.vehicle.VehicleDetailResponse;
import com.example.evm.entity.inventory.ManufacturerStock;
import com.example.evm.entity.vehicle.Vehicle;
import com.example.evm.entity.vehicle.VehicleVariant;
import com.example.evm.exception.ResourceNotFoundException;
import com.example.evm.repository.inventory.ManufacturerStockRepository;
import com.example.evm.repository.vehicle.VehicleRepository;
import com.example.evm.repository.vehicle.VehicleVariantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service VehicleServiceNew - Logic nghiệp vụ quản lý xe
 * 
 * Cấu trúc mới:
 * - Tạo xe → tự động vào ManufacturerStock
 * - JOIN để lấy Variant → Model → VehicleDetail
 * - Query tổng hợp kho (GROUP BY variant + color)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class VehicleServiceImpl implements VehicleService {

    private final VehicleRepository vehicleRepository;
    private final VehicleVariantRepository variantRepository;
    private final ManufacturerStockRepository manufacturerStockRepository;

    // Get all vehicles
    @Override
    @Transactional(readOnly = true)
    public List<VehicleFullResponse> getAllVehicles() {
        return vehicleRepository.findAll() // 1. Lấy tất cả
                .stream()
                .map(this::buildFullResponse) // 2. Map sang DTO
                .collect(Collectors.toList());
    }
    
    /**
     * Tạo xe mới và tự động lưu vào kho tổng (ManufacturerStock)
     */
    @Override
    @Transactional
    public VehicleFullResponse createVehicle(VehicleRequest request) {
        log.info("Creating new vehicle - variantId: {}, color: {}", request.getVariantId(), request.getColor());
        
        // 1. Lấy kho tổng mặc định
        ManufacturerStock defaultWarehouse = manufacturerStockRepository
            .findByStatus("ACTIVE")
            .stream()
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("No active warehouse found"));

        // 2. Kiểm tra variant tồn tại
        VehicleVariant variant = variantRepository.findById(request.getVariantId())
            .orElseThrow(() -> new ResourceNotFoundException("Variant not found with id: " + request.getVariantId()));

        // 3. Tạo Vehicle
        Vehicle vehicle = new Vehicle();
        vehicle.setVinNumber(generateVIN(variant));
        vehicle.setVariant(variant);
        vehicle.setColor(request.getColor());
        vehicle.setManufactureDate(LocalDate.now());
        vehicle.setWarrantyExpiryDate(LocalDate.now().plusYears(5)); // 5 năm bảo hành
        vehicle.setManufacturerStock(defaultWarehouse);
        vehicle.setInventoryStock(null);
        vehicle.setStatus("IN_MANUFACTURER_STOCK");

        Vehicle saved = vehicleRepository.save(vehicle);

        log.info("✅ Created vehicle {} in warehouse {}", saved.getVinNumber(), defaultWarehouse.getWarehouseName());

        // 4. Reload với full info
        return getVehicleById(saved.getVehicleId());
    }

    /**
     * Lấy thông tin chi tiết xe (kèm full info)
     */
    @Override
    @Transactional(readOnly = true)
    public VehicleFullResponse getVehicleById(Long id) {
        Vehicle vehicle = vehicleRepository.findByIdWithFullInfo(id)
            .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found with id: " + id));

        return buildFullResponse(vehicle);
    }

    /**
     * Lấy danh sách xe trong kho tổng (hiển thị từng xe với VIN)
     */
    @Override
    @Transactional(readOnly = true)
    public List<VehicleFullResponse> getAllManufacturerVehicles() {
        List<Vehicle> vehicles = vehicleRepository.findAllInManufacturerStockWithFullInfo();
        
        return vehicles.stream()
            .map(this::buildFullResponse)
            .collect(Collectors.toList());
    }

    /**
     * Lấy tổng hợp kho tổng (GROUP BY variant + color)
     */
    @Override
    @Transactional(readOnly = true)
    public List<StockSummaryResponse> getManufacturerStockSummary() {
        List<Object[]> results = vehicleRepository.countManufacturerStockByVariantColor();
        
        return results.stream()
            .map(row -> StockSummaryResponse.builder()
                .variantId((Long) row[0])
                .variantName((String) row[1])
                .modelName((String) row[2])
                .color((String) row[3])
                .quantity(((Number) row[4]).intValue())
                .build())
            .collect(Collectors.toList());
    }

    /**
     * Lấy danh sách xe của dealer (hiển thị từng xe)
     */
    @Override
    @Transactional(readOnly = true)
    public List<VehicleFullResponse> getDealerVehicles(Long dealerId) {
        List<Vehicle> vehicles = vehicleRepository.findByDealerIdWithFullInfo(dealerId);
        
        return vehicles.stream()
            .map(this::buildFullResponse)
            .collect(Collectors.toList());
    }

    /**
     * Lấy tổng hợp kho dealer (GROUP BY variant + color)
     */
    @Override
    @Transactional(readOnly = true)
    public List<StockSummaryResponse> getDealerStockSummary(Long dealerId) {
        List<Object[]> results = vehicleRepository.countDealerStockByVariantColor(dealerId);
        
        return results.stream()
            .map(row -> StockSummaryResponse.builder()
                .variantId((Long) row[0])
                .variantName((String) row[1])
                .modelName((String) row[2])
                .color((String) row[3])
                .quantity(((Number) row[4]).intValue())
                .build())
            .collect(Collectors.toList());
    }

    // ===== HELPER METHODS =====

    /**
     * Build VehicleFullResponse từ Vehicle entity
     */
    private VehicleFullResponse buildFullResponse(Vehicle v) {
        VehicleFullResponse.VehicleFullResponseBuilder builder = VehicleFullResponse.builder()
            // Vehicle info
            .vehicleId(v.getVehicleId())
            .vinNumber(v.getVinNumber())
            .color(v.getColor())
            .status(v.getStatus())
            .manufactureDate(v.getManufactureDate())
            .warrantyExpiryDate(v.getWarrantyExpiryDate());

        // Variant info
        if (v.getVariant() != null) {
            builder
                .variantId(v.getVariant().getVariantId())
                .variantName(v.getVariant().getName())
                .variantImage(v.getVariant().getImageUrl())
                .msrp(v.getVariant().getMsrp());

            // Model info
            if (v.getVariant().getModel() != null) {
                builder
                    .modelId(v.getVariant().getModel().getModelId())
                    .modelName(v.getVariant().getModel().getName())
                    .manufacturer(v.getVariant().getModel().getManufacturer())
                    .year(v.getVariant().getModel().getYear())
                    .bodyType(v.getVariant().getModel().getBodyType());
            }

            // VehicleDetail info (từ variant.detail)
            if (v.getVariant().getDetail() != null) {
                builder.detail(new VehicleDetailResponse(v.getVariant().getDetail()));
            }
        }

        return builder.build();
    }

    /**
     * Tạo VIN number tự động
     * Format: VIN + Model Code (5 chars) + Random (8 chars)
     */
    private String generateVIN(VehicleVariant variant) {
        String modelCode = variant.getModel().getName()
            .replaceAll("\\s+", "")
            .toUpperCase()
            .substring(0, Math.min(5, variant.getModel().getName().length()));
        
        String randomPart = UUID.randomUUID().toString()
            .replaceAll("-", "")
            .substring(0, 8)
            .toUpperCase();
        
        return "VIN" + modelCode + randomPart;
    }
}

