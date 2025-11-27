package com.example.evm.service.vehicle;

import com.example.evm.dto.vehicle.VehicleDetailRequest;
import com.example.evm.dto.vehicle.VehicleDetailResponse;
import com.example.evm.dto.vehicle.VehicleVariantRequest;
import com.example.evm.dto.vehicle.VehicleVariantResponse;
import com.example.evm.entity.salePrice.SalePrice;
import com.example.evm.entity.vehicle.VehicleDetail;
import com.example.evm.entity.vehicle.VehicleModel;
import com.example.evm.entity.vehicle.VehicleVariant;
import com.example.evm.exception.ResourceNotFoundException;
import com.example.evm.repository.salePrice.SalePriceRepository;
import com.example.evm.repository.vehicle.VehicleRepository;
import com.example.evm.repository.vehicle.VehicleDetailRepository;
import com.example.evm.repository.vehicle.VehicleModelRepository;
import com.example.evm.repository.vehicle.VehicleVariantRepository;
import com.example.evm.exception.ForeignKeyConstraintException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class VehicleVariantServiceImpl implements VehicleVariantService {

    private final VehicleVariantRepository variantRepository;
    private final VehicleModelRepository modelRepository;
    private final VehicleDetailRepository detailRepository;
    private final VehicleRepository vehicleRepository;
    private final SalePriceRepository salePriceRepository;

    @Override
    public VehicleVariantResponse createVariant(VehicleVariantRequest request) {

        // 1. TÌM MODEL (DÒNG XE) TƯƠNG ỨNG
        VehicleModel model = modelRepository.findById(request.getModelId())
                .orElseThrow(() -> new ResourceNotFoundException("Model not found with id: " + request.getModelId()));

        // 2. TẠO ĐỐI TƯỢNG VARIANT MỚI
        VehicleVariant variant = new VehicleVariant();
        variant.setName(request.getName());
        variant.setModel(model);
        variant.setStatus("ACTIVE");
        variant.setMsrp(request.getMsrp());

        // 3. LƯU VÀ TRẢ VỀ
        VehicleVariant savedVariant = variantRepository.save(variant);
        return new VehicleVariantResponse(savedVariant);
    }

    @Override
    public List<VehicleVariantResponse> getAllVariants(Long dealerId) {
        return variantRepository.findAll().stream()
                .filter(variant -> "ACTIVE".equalsIgnoreCase(variant.getStatus()))
                .map(variant -> {
                    VehicleVariantResponse response = new VehicleVariantResponse(variant);
                    // Nếu có dealerId, lấy giá dealer từ SalePrice
                    if (dealerId != null) {
                        enrichWithDealerPrice(response, variant.getVariantId(), dealerId);
                    }
                    return response;
                })
                .collect(Collectors.toList());
    }

    @Override
    public VehicleVariantResponse getVariantById(Long id, Long dealerId) {
        VehicleVariant variant = variantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Variant not found with id: " + id));
        VehicleVariantResponse response = new VehicleVariantResponse(variant);
        
        // Nếu có dealerId, lấy giá dealer từ SalePrice
        if (dealerId != null) {
            enrichWithDealerPrice(response, variant.getVariantId(), dealerId);
        }
        
        return response;
    }
    
    /**
     * Helper method: Lấy giá dealer từ SalePrice và gán vào response
     */
    private void enrichWithDealerPrice(VehicleVariantResponse response, Long variantId, Long dealerId) {
        try {
            List<SalePrice> prices = salePriceRepository.findByDealerIdAndVariantId(dealerId, variantId);
            
            if (!prices.isEmpty()) {
                // Lấy giá mới nhất (theo effectiveDate)
                SalePrice latestPrice = prices.stream()
                        .filter(p -> p.getEffectiveDate().isBefore(LocalDate.now()) || p.getEffectiveDate().isEqual(LocalDate.now()))
                        .max((p1, p2) -> p1.getEffectiveDate().compareTo(p2.getEffectiveDate()))
                        .orElse(prices.get(0));
                
                response.setBasePrice(latestPrice.getBasePrice());
                response.setDealerPrice(latestPrice.getPrice());
                
                log.debug(" Found dealer price for variant {}, dealer {}: base={}, selling={}", 
                    variantId, dealerId, latestPrice.getBasePrice(), latestPrice.getPrice());
            } else {
                log.debug(" No price found for variant {}, dealer {}", variantId, dealerId);
            }
        } catch (Exception e) {
            log.warn(" Error fetching dealer price for variant {}, dealer {}: {}", 
                variantId, dealerId, e.getMessage());
        }
    }

    @Override
    @Transactional
    public VehicleVariantResponse updateVariant(Long id, VehicleVariantRequest request) {

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

        // 4. Lưu entity đã được cập nhật vào DB
        VehicleVariant savedVariant = variantRepository.save(existingVariant);

        // 5. Trả về response
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
    public VehicleDetailResponse createDetails(Long variantId, VehicleDetailRequest request) {
        // 1. Kiểm tra xem Detail đã tồn tại cho variantId này chưa
        boolean detailExists = detailRepository.existsById(variantId);
        if (detailExists) {
            throw new DataIntegrityViolationException(
                    "VehicleDetail already exists for Variant ID: " + variantId + ". Use PUT to update."
            );
        }

        // 2. Tìm variant cha (bắt buộc)
        VehicleVariant variant = variantRepository.findById(variantId)
                .orElseThrow(() -> new ResourceNotFoundException("Variant not found: " + variantId));

        // 3. TẠO MỚI VehicleDetail
        VehicleDetail detail = new VehicleDetail();
        detail.setVariant(variant);

        // --- 4. GÁN GIÁ TRỊ TỪ REQUEST (Gán toàn bộ, không cần check null) ---
        detail.setDimensionsMm(request.getDimensionsMm());
        detail.setWheelbaseMm(request.getWheelbaseMm());
        detail.setGroundClearanceMm(request.getGroundClearanceMm());
        detail.setCurbWeightKg(request.getCurbWeightKg());
        detail.setSeatingCapacity(request.getSeatingCapacity());
        detail.setTrunkCapacityLiters(request.getTrunkCapacityLiters());
        detail.setEngineType(request.getEngineType());
        detail.setMaxPower(request.getMaxPower());
        detail.setTopSpeedKmh(request.getTopSpeedKmh());
        detail.setDrivetrain(request.getDrivetrain());
        detail.setDriveModes(request.getDriveModes());
        detail.setBatteryCapacityKwh(request.getBatteryCapacityKwh());
        detail.setRangePerChargeKm(request.getRangePerChargeKm());
        detail.setChargingTime(request.getChargingTime());
        detail.setExteriorFeatures(request.getExteriorFeatures());
        detail.setInteriorFeatures(request.getInteriorFeatures());
        detail.setAirbags(request.getAirbags());
        detail.setBrakingSystem(request.getBrakingSystem());
        detail.setHasEsc(request.getHasEsc() != null ? request.getHasEsc() : false);
        detail.setHasTpms(request.getHasTpms() != null ? request.getHasTpms() : false);
        detail.setHasRearCamera(request.getHasRearCamera() != null ? request.getHasRearCamera() : false);
        detail.setHasChildLock(request.getHasChildLock() != null ? request.getHasChildLock() : false);
        // --- HẾT GÁN GIÁ TRỊ ---

        // 5. Lưu lại detail MỚI
        VehicleDetail savedDetail = detailRepository.save(detail);

        // 6. Trả về Response DTO
        return new VehicleDetailResponse(savedDetail);
    }

    @Override
    @Transactional
    public VehicleDetailResponse updateDetails(Long variantId, VehicleDetailRequest request) {
        // 1. Tìm Detail hiện có
        VehicleDetail detail = detailRepository.findByVariant_VariantId(variantId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "VehicleDetail not found for Variant ID: " + variantId + ". Cannot update."
                ));

        VehicleVariant variant = variantRepository.findById(variantId)
                .orElseThrow(() -> new ResourceNotFoundException("Variant not found: " + variantId)); // Should not happen if detail exists
        detail.setVariant(variant); // Đảm bảo liên kết đúng

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

    @Override
    @Transactional
    public void deleteVariant(Long id) {
        // 1. Kiểm tra xem Variant có đang được Vehicle nào sử dụng không
        if (vehicleRepository.existsByVariantVariantId(id)) {
            // Nếu đang được sử dụng -> Báo lỗi, không cho xóa
            throw new ForeignKeyConstraintException(
                "Không thể xóa. Phiên bản (Variant) này đang được liên kết với ít nhất một xe (Vehicle) cụ thể."
            );
        }

        log.info("Deleting VehicleVariant with ID: {}", id);

        if (detailRepository.existsById(id)) {
            detailRepository.deleteById(id);
            log.info("Deleted associated VehicleDetail for Variant ID: {}", id);
        }
        variantRepository.deleteById(id);
    }
}