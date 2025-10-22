package com.example.evm.service.vehicle;

import com.example.evm.dto.vehicle.VehicleComparisonDTO;
import com.example.evm.dto.vehicle.VehicleRequest;
import com.example.evm.dto.vehicle.VehicleResponse;
import com.example.evm.entity.inventory.InventoryStock;
import com.example.evm.entity.vehicle.SalePrice; 
import com.example.evm.entity.vehicle.Vehicle;
import com.example.evm.entity.vehicle.VehicleDetail;
import com.example.evm.entity.vehicle.VehicleVariant;
import com.example.evm.exception.ResourceNotFoundException;
import com.example.evm.repository.inventory.InventoryStockRepository;
import com.example.evm.repository.vehicle.VehicleRepository;
import com.example.evm.repository.vehicle.VehicleVariantRepository;
import com.example.evm.repository.vehicle.SalePriceRepository;
import com.example.evm.repository.vehicle.VehicleDetailRepository;

import org.springframework.web.multipart.MultipartFile;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class VehicleServiceImpl implements VehicleService {

    private final VehicleRepository vehicleRepository;
    private final VehicleVariantRepository variantRepository;
    private final SalePriceRepository salePriceRepository; 
    private final VehicleDetailRepository detailRepository;
    private final InventoryStockRepository inventoryStockRepository;

    // 🧩 Convert Entity → Response DTO (Lấy Detail qua Stock)
    private VehicleResponse convertToResponse(Vehicle vehicle) {
        // Lấy VehicleDetail tương ứng
        VehicleDetail detail = null;
        if (vehicle.getInventoryStock() != null && vehicle.getInventoryStock().getVariant() != null) {
            // Lấy Detail dựa trên Variant ID trong Stock
            detail = detailRepository.findByVariant_VariantId(vehicle.getInventoryStock().getVariant().getVariantId())
                                     .orElse(null);
        }
        return new VehicleResponse(vehicle, detail);
    }

    // 🟢 Lấy danh sách xe còn hoạt động (ACTIVE/AVAILABLE)
    @Override
    @Transactional(readOnly = true)
    public List<VehicleResponse> getAllVehicles() {
        return vehicleRepository.findAvailableVehicles().stream() 
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    // 🔴 Lấy danh sách xe INACTIVE (NGỪNG BÁN)
    @Override
    @Transactional(readOnly = true)
    public List<VehicleResponse> getAllInactiveVehicles() {
        return vehicleRepository.findInactiveVehicles().stream() 
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<VehicleResponse> getVehiclesByDealerId(Long dealerId) {
        List<Vehicle> vehicles = vehicleRepository.findAvailableVehiclesByDealerId(dealerId);

        return vehicles.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    //  Lấy xe theo ID
    @Override
    @Transactional(readOnly = true)
    public VehicleResponse getVehicleById(Long id) {
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found with id: " + id));

        // Logic lấy detail đã được chuyển sang convertToResponse
        VehicleDetail detail = null;
        if (vehicle.getInventoryStock() != null && vehicle.getInventoryStock().getVariant() != null) {
            detail = detailRepository.findByVariant_VariantId(vehicle.getInventoryStock().getVariant().getVariantId())
                                     .orElse(null);
        }
        return new VehicleResponse(vehicle, detail);
    }

    // 🟠 Tìm kiếm theo tên xe
    @Override
    @Transactional(readOnly = true)
    public List<VehicleResponse> searchVehiclesByName(String name) {
        return vehicleRepository.searchActiveByModelOrVariantName(name)
                .stream()
                .map(this::convertToResponse)
                .toList();
    }

    // 🔹 Thêm mới Vehicle (Logic đã đơn giản hóa)
    @Override
    @Transactional
    public VehicleResponse addVehicle(VehicleRequest request, MultipartFile file) {

        // --- 1. KIỂM TRA ĐẦU VÀO & TÌM LIÊN KẾT ---
        if (vehicleRepository.existsByVinNumber(request.getVinNumber())) {
            throw new IllegalArgumentException("VIN already exists: " + request.getVinNumber());
        }
        
        // Cần Stock ID để thêm xe
        InventoryStock stock = inventoryStockRepository.findById(request.getStockId())
             .orElseThrow(() -> new ResourceNotFoundException("Inventory Stock not found with id: " + request.getStockId()));

        // TÌM VARIANT (Bước quan trọng bị thiếu)
        VehicleVariant variant = variantRepository.findById(request.getVariantId())
             .orElseThrow(() -> new ResourceNotFoundException("Variant not found: " + request.getVariantId()));

        // Optional: Kiểm tra stockId có khớp variantId không
        if (stock.getVariant() == null || !stock.getVariant().getVariantId().equals(request.getVariantId())) {
             throw new IllegalArgumentException("Stock ID " + request.getStockId() + " does not match Variant ID " + request.getVariantId());
        }     

        Vehicle vehicle = new Vehicle();
        vehicle.setVinNumber(request.getVinNumber());
        vehicle.setInventoryStock(stock); 
        vehicle.setVariant(variant);
        vehicle.setManufactureDate(request.getManufactureDate()); 
        vehicle.setWarrantyExpiryDate(request.getWarrantyExpiryDate()); 

        Vehicle saved = vehicleRepository.save(vehicle);
        log.info("Added vehicle with VIN: {}", saved.getVinNumber());
        return convertToResponse(saved);
    }

    // 🔹 Cập nhật Vehicle (Logic đã đơn giản hóa)
    @Override
    @Transactional
    // Bỏ MultipartFile nếu không dùng
    public VehicleResponse updateVehicle(Long id, VehicleRequest request , MultipartFile file) {
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found with id: " + id));

        // --- CẬP NHẬT CÁC TRƯỜNG TỪ REQUEST (PARTIAL UPDATE) ---

        // 1. Cập nhật Variant (Nếu có và khác)
        if (request.getVariantId() != null && !request.getVariantId().equals(vehicle.getVariant().getVariantId())) {
             VehicleVariant newVariant = variantRepository.findById(request.getVariantId())
                .orElseThrow(() -> new ResourceNotFoundException("Variant not found: " + request.getVariantId()));
            vehicle.setVariant(newVariant);
        }

        // 2. Cập nhật Stock ID (Nếu có và khác)
        InventoryStock currentStock = vehicle.getInventoryStock(); // Lấy stock hiện tại
        Long currentStockId = (currentStock != null) ? currentStock.getStockId() : null; // Lấy ID (có thể null)

        if (request.getStockId() != null && !request.getStockId().equals(currentStockId)) {
            InventoryStock newStock = inventoryStockRepository.findById(request.getStockId())
                    .orElseThrow(() -> new ResourceNotFoundException("Inventory Stock not found: " + request.getStockId()));
            
            // Optional: Kiểm tra stock mới có khớp variant không
            if (newStock.getVariant() == null || !newStock.getVariant().getVariantId().equals(vehicle.getVariant().getVariantId())) {
                throw new IllegalArgumentException("New Stock ID " + request.getStockId() + " does not match current Variant ID " + vehicle.getVariant().getVariantId());
            }
            vehicle.setInventoryStock(newStock);

        } else if (request.getStockId() == null && currentStockId != null) {
            vehicle.setInventoryStock(null);
        }


        // 3. Cập nhật VIN (Nếu có và khác) - Kiểm tra trùng lặp
        if (request.getVinNumber() != null && !request.getVinNumber().equals(vehicle.getVinNumber())) {
             if (vehicleRepository.existsByVinNumber(request.getVinNumber())) {
                 throw new IllegalArgumentException("New VIN already exists: " + request.getVinNumber());
             }
             vehicle.setVinNumber(request.getVinNumber());
        }

        // 4. Cập nhật các trường Date (Nếu có)
        if (request.getManufactureDate() != null) {
            vehicle.setManufactureDate(request.getManufactureDate());
        }
        if (request.getWarrantyExpiryDate() != null) {
            vehicle.setWarrantyExpiryDate(request.getWarrantyExpiryDate());
        }


        // --- LƯU VÀ TRẢ VỀ ---
        Vehicle updated = vehicleRepository.save(vehicle);
        return convertToResponse(updated); // Dùng hàm map của bạn
    }

    // 🔴 Xóa mềm Vehicle (status đã chuyển sang Stock)
    @Override
    @Transactional
    public void deactivateVehicle(Long id) {
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found with id: " + id));

        InventoryStock stock = vehicle.getInventoryStock();
        if (stock != null) {
            stock.setStatus("INACTIVE"); 
            inventoryStockRepository.save(stock);
            log.info("🚫 Vehicle {} (Stock ID {}) set to INACTIVE", id, stock.getStockId());
        }
    }

    // 🟢 Kích hoạt lại xe (status đã chuyển sang Stock)
    @Override
    @Transactional
    public void activateVehicle(Long id) {
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found with id: " + id));

        InventoryStock stock = vehicle.getInventoryStock();
        if (stock != null) {
            stock.setStatus("ACTIVE");
            inventoryStockRepository.save(stock);
            log.info("✅ Vehicle {} (Stock ID {}) set to ACTIVE", id, stock.getStockId());
        }
    }
    
    @Override
@Transactional(readOnly = true)
public List<VehicleComparisonDTO> compareVariants(List<Long> variantIds) {
    if (variantIds == null || variantIds.isEmpty()) {
        return new ArrayList<>();
    }
    Long currentDealerId = 1L; 

    List<VehicleVariant> variants = variantRepository.findAllById(variantIds); 

    return variants.stream().map(variant -> {
        SalePrice latestPrice = salePriceRepository
                .findTopByDealerDealerIdAndVariantVariantIdOrderByEffectiveDateDesc(currentDealerId, variant.getVariantId())
                .orElse(null);

        return VehicleComparisonDTO.builder()
                .variantId(variant.getVariantId())
                .variantName(variant.getName())
                .modelId(variant.getModel().getModelId())
                .modelName(variant.getModel().getName())
                .modelDescription(variant.getModel().getDescription())
                .price(latestPrice != null ? latestPrice.getPrice() : null)
                .dealerId(latestPrice != null ? latestPrice.getDealer().getDealerId() : null)
                .effectiveDate(latestPrice != null ? latestPrice.getEffectiveDate().toString() : "N/A")
                .variantImage(variant.getImageUrl())
                .build();
    }).collect(Collectors.toList());
    }
}