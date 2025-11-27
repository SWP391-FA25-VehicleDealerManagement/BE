package com.example.evm.service.vehicle;

import com.example.evm.dto.vehicle.DealerVehicleResponse;
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
import com.example.evm.repository.salePrice.SalePriceRepository;
import com.example.evm.service.storage.FileStorageService;
import com.example.evm.exception.ForeignKeyConstraintException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;
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
    private final com.example.evm.repository.order.OrderDetailRepository orderDetailRepository;
    private final SalePriceRepository salePriceRepository;
    private final FileStorageService fileStorageService;

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
    public VehicleFullResponse createVehicle(VehicleRequest request, MultipartFile file) {
        log.info("Creating new vehicle - VIN: {}, variantId: {}, color: {}, isTestDrive: {}",
            request.getVinNumber(), request.getVariantId(), request.getColor(), request.getTestDrive());
        
        // 1. Lấy kho tổng mặc định
        ManufacturerStock defaultWarehouse = manufacturerStockRepository
            .findByStatus("ACTIVE")
            .stream()   
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("No active warehouse found"));

        // 2. Kiểm tra VIN không trùng
        if (vehicleRepository.existsByVinNumber(request.getVinNumber())) {
            throw new IllegalArgumentException(
                "Số VIN " + request.getVinNumber() + " đã tồn tại."
            );
        }   

        // 3. Kiểm tra variant tồn tại
        VehicleVariant variant = variantRepository.findById(request.getVariantId())
            .orElseThrow(() -> new ResourceNotFoundException("Variant not found with id: " + request.getVariantId()));

        // 4. Tạo Vehicle
        Vehicle vehicle = new Vehicle();
        vehicle.setVinNumber(request.getVinNumber());
        vehicle.setVariant(variant);
        vehicle.setColor(request.getColor());
        vehicle.setManufactureDate(LocalDate.now());
        vehicle.setWarrantyExpiryDate(LocalDate.now().plusYears(5)); // 5 năm bảo hành
        vehicle.setManufacturerStock(defaultWarehouse);
        vehicle.setInventoryStock(null);

        // Nếu được chọn là xe lái thử
        if (Boolean.TRUE.equals(request.getTestDrive())) {
            vehicle.setStatus("TEST_DRIVE");
            log.info("Vehicle marked as TEST DRIVE - VIN: {}", vehicle.getVinNumber());
        } else {
            vehicle.setStatus("IN_MANUFACTURER_STOCK");
        }

        // 5. Lưu file ảnh nếu có
        if (file != null && !file.isEmpty()) {
            String filename = fileStorageService.saveToSubFolder(file, "vehicles");
            vehicle.setImageUrl("/api/vehicles/images/" + filename);
        }

        Vehicle saved = vehicleRepository.save(vehicle);

        log.info("Created vehicle {} in warehouse {}", saved.getVinNumber(), defaultWarehouse.getWarehouseName());

        // 6. Reload với full info
        return getVehicleById(saved.getVehicleId());
    }

    @Override
    @Transactional
    public VehicleFullResponse updateVehicle(Long id, String vinNumber, String color, MultipartFile file) {
        
        // 1. Tìm xe (Vehicle) hiện có
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found with id: " + id));

        // 2. Cập nhật VIN nếu có và kiểm tra trùng
        if (vinNumber != null && !vinNumber.isBlank() && !vinNumber.equals(vehicle.getVinNumber())) {
            if (vehicleRepository.existsByVinNumber(vinNumber)) {
                throw new IllegalArgumentException("Số VIN " + vinNumber + " đã tồn tại.");
            }
            vehicle.setVinNumber(vinNumber);
            log.info("Updated VIN for Vehicle ID: {}", id);
        }

        // 3. Cập nhật Color (nếu có)
        if (color != null && !color.isBlank()) {
            vehicle.setColor(color);
            log.info("Updated Color for Vehicle ID: {}", id);
        }

        // 4. Cập nhật Ảnh (nếu có file mới)
        if (file != null && !file.isEmpty()) {
            
            String filename = fileStorageService.saveToSubFolder(file, "vehicles");
            vehicle.setImageUrl("/api/vehicles/images/" + filename);
            log.info("Updated Image for Vehicle ID: {}", id);
        }

        // 5. Lưu và trả về
        Vehicle updatedVehicle = vehicleRepository.save(vehicle);
        return getVehicleById(updatedVehicle.getVehicleId());
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
    public List<DealerVehicleResponse> getDealerVehicles(Long dealerId) {
        // Lấy danh sách xe có đầy đủ join
        List<Vehicle> vehicles = vehicleRepository.findByDealerIdWithFullInfo(dealerId);

        // Map từng vehicle sang DealerVehicleResponse
        return vehicles.stream().map(v -> {
            DealerVehicleResponse.DealerVehicleResponseBuilder builder = DealerVehicleResponse.builder()
                .vehicleId(v.getVehicleId())
                .vinNumber(v.getVinNumber())
                .color(v.getColor())
                .imageUrl(v.getImageUrl())
                .status(v.getStatus())
                .manufactureDate(v.getManufactureDate())
                .warrantyExpiryDate(v.getWarrantyExpiryDate());

            if (v.getVariant() != null) {
                builder.variantId(v.getVariant().getVariantId())
                    .variantName(v.getVariant().getName())
                    .msrp(v.getVariant().getMsrp());

                if (v.getVariant().getModel() != null) {
                    builder.modelId(v.getVariant().getModel().getModelId())
                        .modelName(v.getVariant().getModel().getName())
                        .manufacturer(v.getVariant().getModel().getManufacturer())
                        .year(v.getVariant().getModel().getYear())
                        .bodyType(v.getVariant().getModel().getBodyType());
                }

                if (v.getVariant().getDetail() != null) {
                    builder.detail(new VehicleDetailResponse(v.getVariant().getDetail()));
                }
            }

            // Gắn giá bán (chỉ riêng API này)
            salePriceRepository.findLatestPriceByDealerAndVariant(dealerId, v.getVariant().getVariantId())
                .ifPresent(sp -> builder.price(sp.getPrice().doubleValue()));

            return builder.build();
        }).collect(Collectors.toList());
    }

    /**
     * Lấy danh sách xe có status = TEST_DRIVE
     */
    @Override
    @Transactional(readOnly = true)
    public List<VehicleFullResponse> getTestDriveVehicles() {
        List<Vehicle> vehicles = vehicleRepository.findByStatusTestDriveWithFullInfo();
        
        return vehicles.stream()
            .map(this::buildFullResponse)
            .collect(Collectors.toList());
    }

    /**
     * Lấy danh sách xe có status = TEST_DRIVE theo dealer
     */
    @Override
    @Transactional(readOnly = true)
    public List<VehicleFullResponse> getTestDriveVehiclesByDealer(Long dealerId) {
        List<Vehicle> vehicles = vehicleRepository.findByStatusTestDriveAndDealerIdWithFullInfo(dealerId);
        
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

    @Override
    @Transactional
    public void setVehicleAsTestDrive(Long vehicleId) {
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found with id: " + vehicleId));

        if ("SOLD".equalsIgnoreCase(vehicle.getStatus())) {
            throw new IllegalStateException("Cannot mark a sold vehicle as test drive.");
        }

        vehicle.setStatus("TEST_DRIVE");
        vehicleRepository.save(vehicle);

        log.info("Vehicle {} marked as TEST_DRIVE", vehicle.getVehicleId());
    }

    @Override
    @Transactional
    public void returnVehicleFromTestDrive(Long vehicleId) {
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy xe với ID: " + vehicleId));

        // Kiểm tra trạng thái hiện tại
        if (!"TEST_DRIVE".equalsIgnoreCase(vehicle.getStatus())) {
            throw new IllegalStateException("Xe này không ở trạng thái TEST_DRIVE, không thể chuyển về kho.");
        }

        // Cập nhật trạng thái
        vehicle.setStatus("IN_DEALER_STOCK");
        vehicleRepository.save(vehicle);

        log.info("Vehicle {} đã được chuyển từ TEST_DRIVE về IN_DEALER_STOCK", vehicle.getVehicleId());
    }


    @Override
    @Transactional
    public void deleteVehicle(Long vehicleId) {
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found with ID: " + vehicleId));

        // Kiểm tra liên kết với OrderDetail
        boolean hasOrder = orderDetailRepository.existsByVehicle_VehicleId(vehicleId);
        if (hasOrder) {
            throw new ForeignKeyConstraintException("Xe này đang liên kết với đơn hàng, không thể xóa.");
        }

        // Xóa
        vehicleRepository.delete(vehicle);
        log.info("Vehicle ID {} deleted successfully.", vehicleId);
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
            .imageUrl(v.getImageUrl())
            .status(v.getStatus())
            .manufactureDate(v.getManufactureDate())
            .warrantyExpiryDate(v.getWarrantyExpiryDate());

        // Variant info
        if (v.getVariant() != null) {
            builder
                .variantId(v.getVariant().getVariantId())
                .variantName(v.getVariant().getName())
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
}

