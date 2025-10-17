package com.example.evm.service.vehicle;

import com.example.evm.dto.vehicle.VehicleComparisonDTO;
import com.example.evm.dto.vehicle.VehicleRequest;
import com.example.evm.dto.vehicle.VehicleResponse;
import com.example.evm.entity.dealer.Dealer;
import com.example.evm.entity.vehicle.SalePrice;
import com.example.evm.entity.vehicle.Vehicle;
import com.example.evm.entity.vehicle.VehicleDetail;
import com.example.evm.entity.vehicle.VehicleVariant;
import com.example.evm.exception.ResourceNotFoundException;
import com.example.evm.repository.dealer.DealerRepository;
import com.example.evm.repository.vehicle.VehicleRepository;
import com.example.evm.repository.vehicle.VehicleVariantRepository;
import com.example.evm.repository.vehicle.SalePriceRepository;
import com.example.evm.repository.vehicle.VehicleDetailRepository;

import org.springframework.web.multipart.MultipartFile;
import com.example.evm.service.storage.FileStorageService;
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
    private final DealerRepository dealerRepository;
    private final FileStorageService fileStorageService;
    private final VehicleDetailRepository detailRepository;

    // 🟢 Lấy danh sách xe còn hoạt động
    @Override
    @Transactional(readOnly = true)
    public List<VehicleResponse> getAllVehicles() {
        return vehicleRepository.findAll().stream()
                .filter(v -> "ACTIVE".equalsIgnoreCase(v.getStatus()))
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    // 🔴 Lấy danh sách xe INACTIVE
    @Override
    @Transactional(readOnly = true)
    public List<VehicleResponse> getAllInactiveVehicles() {
        return vehicleRepository.findAll().stream()
                .filter(v -> "INACTIVE".equalsIgnoreCase(v.getStatus()))
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    // 🏢 Lấy danh sách xe theo Dealer ID
    @Override
    @Transactional(readOnly = true)
    public List<VehicleResponse> getVehiclesByDealerId(Long dealerId) {
        // 1. Gọi Repository để tìm các xe đang active của dealer đó
        List<Vehicle> vehicles = vehicleRepository.findByDealer_DealerIdAndStatusIgnoreCase(dealerId, "ACTIVE");

        // 2. Chuyển đổi sang DTO và trả về
        return vehicles.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    //  Lấy xe theo ID
    @Override
    @Transactional(readOnly = true)
    public VehicleResponse getVehicleById(Long id) {
        // 1. Lấy thông tin Vehicle chính
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found with id: " + id));

        // 2. Lấy thông tin VehicleDetail tương ứng (nếu có)
        VehicleDetail detail = null;
        if (vehicle.getVariant() != null) {
            detail = detailRepository.findByVariant_VariantId(vehicle.getVariant().getVariantId())
                                     .orElse(null); // Trả về null nếu không tìm thấy detail
        }

        // 3. Gọi CONSTRUCTOR MỚI của VehicleResponse
        return new VehicleResponse(vehicle, detail);
    }

    // 🟠 Tìm kiếm theo tên xe
    @Override
    @Transactional(readOnly = true)
    public List<VehicleResponse> searchVehiclesByName(String name) {
        return vehicleRepository.searchByName(name)
                .stream()
                .map(this::convertToResponse)
                .toList();
    }

    // 🔹 Thêm mới Vehicle
    @Override
    @Transactional
    public VehicleResponse addVehicle(VehicleRequest request, MultipartFile file) {
        // 1. Lưu file ảnh và lấy về tên file duy nhất
        String filename = fileStorageService.save(file);
        // Tạo URL (giả sử endpoint xem ảnh của Vehicle sẽ là /api/vehicles/images/{filename})
        String imageUrl = "/api/vehicles/images/" + filename;

        // 2. Logic cũ của bạn
        Vehicle vehicle = new Vehicle();
        vehicle.setName(request.getName() != null ? request.getName() : request.getVehicleName());
        vehicle.setColor(request.getColor());
        vehicle.setImage(imageUrl); // <-- Lưu URL vào DB, KHÔNG dùng request.getImage()
        vehicle.setPrice(request.getPrice());
        vehicle.setStock(request.getStock());
        vehicle.setStatus("ACTIVE");

        if (request.getDealerId() != null) {
            dealerRepository.findById(request.getDealerId()).ifPresent(vehicle::setDealer);
        }

        if (request.getVariantId() != null) {
            VehicleVariant variant = variantRepository.findById(request.getVariantId())
                    .orElseThrow(() -> new RuntimeException("Variant not found with id: " + request.getVariantId()));
            vehicle.setVariant(variant);
        }

        Vehicle saved = vehicleRepository.save(vehicle);
        return convertToResponse(saved);
    }

    // 🔹 Cập nhật Vehicle
    @Override
    @Transactional
    public VehicleResponse updateVehicle(Long id, VehicleRequest request, MultipartFile file) {
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found with id: " + id));

        // Xử lý file ảnh nếu người dùng có gửi file mới
        if (file != null && !file.isEmpty()) {
            String filename = fileStorageService.save(file);
            String imageUrl = "/api/vehicles/images/" + filename;
            vehicle.setImage(imageUrl); // Cập nhật ảnh mới
        }

        // Cập nhật các thông tin khác
        String newName = request.getName() != null ? request.getName() : request.getVehicleName(); // Logic cũ của bạn
        if (newName != null) {
            vehicle.setName(newName);
        }

        // Kiểm tra color trước khi cập nhật
        if (request.getColor() != null) {
            vehicle.setColor(request.getColor());
        }

        // Kiểm tra price trước khi cập nhật
        if (request.getPrice() != null) {
            vehicle.setPrice(request.getPrice());
        }

        // Kiểm tra stock trước khi cập nhật
        if (request.getStock() != null) {
            vehicle.setStock(request.getStock());
        }

        // Kiểm tra dealerId trước khi cập nhật
        if (request.getDealerId() != null) {
            dealerRepository.findById(request.getDealerId()).ifPresent(vehicle::setDealer);
        }

        // Kiểm tra variantId trước khi cập nhật
        if (request.getVariantId() != null) {
            VehicleVariant variant = variantRepository.findById(request.getVariantId())
                    .orElseThrow(() -> new RuntimeException("Variant not found with id: " + request.getVariantId()));
            vehicle.setVariant(variant);
        }
        // --- KẾT THÚC PHẦN SỬA LỖI ---


        Vehicle updated = vehicleRepository.save(vehicle);

        // Lấy detail để trả về (như code trước)
        VehicleDetail detail = null;
        if (updated.getVariant() != null) {
            detail = detailRepository.findByVariant_VariantId(updated.getVariant().getVariantId()).orElse(null);
        }
        return new VehicleResponse(updated, detail);
    }

    // 🔴 Xóa mềm Vehicle (set status = INACTIVE)
    @Override
    @Transactional
    public void deactivateVehicle(Long id) {
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found with id: " + id));

        vehicle.setStatus("INACTIVE");
        vehicleRepository.save(vehicle);
        log.info("🚫 Vehicle {} set to INACTIVE", id);
    }

    // 🟢 Kích hoạt lại xe (status = ACTIVE)
    @Override
    @Transactional
    public void activateVehicle(Long id) {
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found with id: " + id));

        vehicle.setStatus("ACTIVE");
        vehicleRepository.save(vehicle);
        log.info("✅ Vehicle {} set to ACTIVE", id);
    }

    // 🔍 So sánh variant giữa các xe
    @Override
    @Transactional(readOnly = true)
    public List<VehicleComparisonDTO> compareVariants(List<Long> variantIds) {
        if (variantIds == null || variantIds.isEmpty()) {
            return new ArrayList<>();
        }

        Long currentDealerId = 1L; // TODO: thay bằng Dealer ID của user đăng nhập
        List<VehicleVariant> variants = variantRepository.findAllById(variantIds);

        return variants.stream().map(variant -> {
            SalePrice latestPrice = salePriceRepository
                    .findTopByDealerIdAndVariantIdOrderByEffectiveDateDesc(currentDealerId, variant.getVariantId());

            return VehicleComparisonDTO.builder()
                    .variantId(variant.getVariantId())
                    .variantName(variant.getName())
                    .modelId(variant.getModel().getModelId())
                    .modelName(variant.getModel().getName())
                    .modelDescription(variant.getModel().getDescription())
                    .price(latestPrice != null ? latestPrice.getPrice() : null)
                    .dealerId(latestPrice != null ? latestPrice.getDealerId() : null)
                    .effectiveDate(latestPrice != null ? latestPrice.getEffectiveDate().toString() : "N/A")
                    .variantImage(variant.getImage())
                    .build();
        }).collect(Collectors.toList());
    }

    // 🧩 Convert Entity → Response DTO
    private VehicleResponse convertToResponse(Vehicle vehicle) {
        Long dealerId = vehicle.getDealer() != null ? vehicle.getDealer().getDealerId() : null;
        Long variantId = vehicle.getVariant() != null ? vehicle.getVariant().getVariantId() : null;

        if (dealerId != null && variantId != null) {
            SalePrice latestPrice = salePriceRepository
                    .findTopByDealerIdAndVariantIdOrderByEffectiveDateDesc(dealerId, variantId);
            if (latestPrice != null) {
                vehicle.setPrice(latestPrice.getPrice().doubleValue());
            }
        }

        return new VehicleResponse(vehicle);
    }
}
