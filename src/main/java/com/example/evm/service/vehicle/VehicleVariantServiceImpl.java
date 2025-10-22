package com.example.evm.service.vehicle;

import com.example.evm.dto.vehicle.VehicleDetailRequest;
import com.example.evm.dto.vehicle.VehicleDetailResponse;
import com.example.evm.dto.vehicle.VehicleVariantRequest;
import com.example.evm.dto.vehicle.VehicleVariantResponse;
import com.example.evm.entity.vehicle.VehicleDetail;
import com.example.evm.entity.vehicle.VehicleModel;
import com.example.evm.entity.vehicle.VehicleVariant;
import com.example.evm.exception.ResourceNotFoundException;
import com.example.evm.repository.vehicle.VehicleDetailRepository;
import com.example.evm.repository.vehicle.VehicleModelRepository;
import com.example.evm.repository.vehicle.VehicleVariantRepository;
import com.example.evm.service.storage.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VehicleVariantServiceImpl implements VehicleVariantService {

    private final VehicleVariantRepository variantRepository;
    private final VehicleModelRepository modelRepository;
    private final FileStorageService fileStorageService;
    private final VehicleDetailRepository detailRepository;

    @Override
    public VehicleVariantResponse createVariant(VehicleVariantRequest request, MultipartFile file) {
        // 1 & 2. LƯU FILE VÀ TẠO URL
        String filename = fileStorageService.save(file);
        String imageUrl = "/api/variants/images/" + filename;

        // 3. TÌM MODEL (DÒNG XE) TƯƠNG ỨNG
        VehicleModel model = modelRepository.findById(request.getModelId())
                .orElseThrow(() -> new ResourceNotFoundException("Model not found with id: " + request.getModelId()));

        // 4. TẠO ĐỐI TƯỢNG VARIANT MỚI
        VehicleVariant variant = new VehicleVariant();
        variant.setName(request.getName());
        variant.setImageUrl(imageUrl);
        variant.setModel(model);
        variant.setStatus("ACTIVE");
        variant.setMsrp(request.getMsrp());

        // 5. LƯU VÀ TRẢ VỀ
        VehicleVariant savedVariant = variantRepository.save(variant);
        return new VehicleVariantResponse(savedVariant);
    }

    @Override
    public List<VehicleVariantResponse> getAllVariants() {
        return variantRepository.findAll().stream()
                .filter(variant -> "ACTIVE".equalsIgnoreCase(variant.getStatus()))
                .map(VehicleVariantResponse::new)
                .collect(Collectors.toList());
    }

    @Override
    public VehicleVariantResponse getVariantById(Long id) {
        VehicleVariant variant = variantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Variant not found with id: " + id));
        return new VehicleVariantResponse(variant);
    }

    @Override
    @Transactional
    public VehicleVariantResponse updateVariant(Long id, VehicleVariantRequest request, MultipartFile file) {

        // 1. Tìm đối tượng (entity) đang có trong database
        VehicleVariant existingVariant = variantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Variant not found with id: " + id));

        // 2. Kiểm tra và cập nhật 'name'
        if (request.getName() != null && !request.getName().isBlank()) {
            existingVariant.setName(request.getName());
        }

        // 3. Kiểm tra và cập nhật 'modelId'
        if (request.getModelId() != null) {
            VehicleModel newModel = modelRepository.findById(request.getModelId())
                    .orElseThrow(() -> new ResourceNotFoundException("Model not found with id: " + request.getModelId()));
            existingVariant.setModel(newModel);
        }

        if (request.getMsrp() != null) {
            existingVariant.setMsrp(request.getMsrp());
        }

        // 4. Kiểm tra và cập nhật 'file' (ảnh)
        if (file != null && !file.isEmpty()) {

            String filename = fileStorageService.save(file);
            String newImageUrl = "/api/variants/images/" + filename;
            existingVariant.setImageUrl(newImageUrl);
        }

        // 5. Lưu entity đã được cập nhật vào DB
        VehicleVariant savedVariant = variantRepository.save(existingVariant);

        // 6. Trả về response
        return new VehicleVariantResponse(savedVariant);
    }

    @Override
    public void deactivateVariant(Long id) {
        VehicleVariant variant = variantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Variant not found with id: " + id));
        variant.setStatus("INACTIVE");
        variantRepository.save(variant);
    }

    @Override
    public void activateVariant(Long id) {
        VehicleVariant variant = variantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Variant not found with id: " + id));
        variant.setStatus("ACTIVE");
        variantRepository.save(variant);
    }

    @Override 
    @Transactional
    public VehicleDetailResponse addOrUpdateDetails(Long variantId, VehicleDetailRequest request) {
        // Tìm variant tương ứng
        VehicleVariant variant = variantRepository.findById(variantId)
                .orElseThrow(() -> new ResourceNotFoundException("Variant not found with id: " + variantId));

        // Kiểm tra xem detail đã tồn tại chưa, nếu chưa thì tạo mới
        VehicleDetail detail = detailRepository.findByVariant_VariantId(variantId)
                .orElse(new VehicleDetail());

        // --- BẮT ĐẦU GÁN GIÁ TRỊ ---
        detail.setVariant(variant);

        // Thông số
        if (request.getDimensionsMm() != null) {
            detail.setDimensionsMm(request.getDimensionsMm());
        }
        if (request.getWheelbaseMm() != null) {
            detail.setWheelbaseMm(request.getWheelbaseMm());
        }
        if (request.getGroundClearanceMm() != null) {
            detail.setGroundClearanceMm(request.getGroundClearanceMm());
        }   
        if (request.getCurbWeightKg() != null) {
            detail.setCurbWeightKg(request.getCurbWeightKg());
        }
        if (request.getSeatingCapacity() != null) {
            detail.setSeatingCapacity(request.getSeatingCapacity());
        }
        if (request.getTrunkCapacityLiters() != null) {
            detail.setTrunkCapacityLiters(request.getTrunkCapacityLiters());
        }

        // Động cơ & Vận Hành
        if (request.getEngineType() != null) {
            detail.setEngineType(request.getEngineType());
        }
        if (request.getMaxPower() != null) {
            detail.setMaxPower(request.getMaxPower());
        }
        if (request.getTopSpeedKmh() != null) {
            detail.setTopSpeedKmh(request.getTopSpeedKmh());
        }
        if (request.getDrivetrain() != null) {
            detail.setDrivetrain(request.getDrivetrain());
        }
        if (request.getDriveModes() != null) {
            detail.setDriveModes(request.getDriveModes());
        }

        // Pin & Khả năng di chuyển
        if (request.getBatteryCapacityKwh() != null) {
            detail.setBatteryCapacityKwh(request.getBatteryCapacityKwh());
        }
        if (request.getRangePerChargeKm() != null) {
            detail.setRangePerChargeKm(request.getRangePerChargeKm());
        }
        if (request.getChargingTime() != null) {
            detail.setChargingTime(request.getChargingTime());
        }

        // Thiết kế
        if (request.getExteriorFeatures() != null) {
            detail.setExteriorFeatures(request.getExteriorFeatures());
        }
        if (request.getInteriorFeatures() != null) {
            detail.setInteriorFeatures(request.getInteriorFeatures());
        }

        // Tính năng an toàn
        if (request.getAirbags() != null) {
            detail.setAirbags(request.getAirbags());
        }
        if (request.getBrakingSystem() != null) {
            detail.setBrakingSystem(request.getBrakingSystem());
        }
        if (request.getHasEsc() != null) {
            detail.setHasEsc(request.getHasEsc());
        }
        if (request.getHasTpms() != null) {
            detail.setHasTpms(request.getHasTpms());
        }   
        if (request.getHasRearCamera() != null) {
            detail.setHasRearCamera(request.getHasRearCamera());
        }
        if (request.getHasChildLock() != null) {
            detail.setHasChildLock(request.getHasChildLock());
        }

        // --- KẾT THÚC GÁN GIÁ TRỊ ---

        VehicleDetail savedDetail = detailRepository.save(detail);
        return new VehicleDetailResponse(savedDetail);
    } 

    @Override
    @Transactional(readOnly = true)
    public VehicleDetailResponse getDetailsByVariantId(Long variantId) {
        VehicleDetail detail = detailRepository.findByVariant_VariantId(variantId)
                .orElseThrow(() -> new ResourceNotFoundException("Details not found for variant id: " + variantId));
        return new VehicleDetailResponse(detail);
    }
}