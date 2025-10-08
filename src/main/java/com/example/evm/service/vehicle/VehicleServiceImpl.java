package com.example.evm.service.vehicle;

import com.example.evm.dto.vehicle.VehicleComparisonDTO;
import com.example.evm.dto.vehicle.VehicleRequest;
import com.example.evm.dto.vehicle.VehicleResponse;
import com.example.evm.entity.dealer.Dealer;
import com.example.evm.entity.vehicle.SalePrice;
import com.example.evm.entity.vehicle.Vehicle;
import com.example.evm.entity.vehicle.VehicleVariant;
import com.example.evm.repository.dealer.DealerRepository;
import com.example.evm.repository.vehicle.VehicleRepository;
import com.example.evm.repository.vehicle.VehicleVariantRepository;
import com.example.evm.repository.vehicle.SalePriceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.text.NumberFormat;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class VehicleServiceImpl implements VehicleService {

    private final VehicleRepository vehicleRepository;
    private final VehicleVariantRepository variantRepository;
    private final SalePriceRepository salePriceRepository;
    private final DealerRepository dealerRepository;

    @Override
    @Transactional(readOnly = true)
    public List<VehicleResponse> getAllVehicles() {
        return vehicleRepository.findAll().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    private VehicleResponse convertToResponse(Vehicle vehicle) {
    Long dealerId = vehicle.getDealer() != null ? vehicle.getDealer().getDealerId() : null;
    Long variantId = vehicle.getVariant() != null ? vehicle.getVariant().getVariantId() : null;

    if (dealerId != null && variantId != null) {
        SalePrice latestPrice = salePriceRepository
            .findTopByDealerIdAndVariantIdOrderByEffectiveDateDesc(dealerId, variantId);

        if (latestPrice != null) {
            // Lấy giá trị Double/BigDecimal thô và set vào Entity
            Double rawPrice = latestPrice.getPrice().doubleValue();
            vehicle.setPrice(rawPrice);
            
        } else {
             vehicle.setPrice(null); 
        }
    }
    return new VehicleResponse(vehicle); 
}
    

    @Override
    @Transactional
    public VehicleResponse addVehicle(VehicleRequest request) {
        Vehicle vehicle = new Vehicle();
        vehicle.setName(request.getName() != null ? request.getName() : request.getVehicleName());
        vehicle.setColor(request.getColor());
        vehicle.setImage(request.getImage());
        vehicle.setPrice(request.getPrice());
        vehicle.setStock(request.getStock());
        
        // Set dealer if provided
        if (request.getDealerId() != null) {
            Optional<Dealer> dealer = dealerRepository.findById(request.getDealerId());
            dealer.ifPresent(vehicle::setDealer);
        }
        
        Vehicle saved = vehicleRepository.save(vehicle);
        return convertToResponse(saved);
    }

    @Override
    @Transactional
    public VehicleResponse updateVehicle(Long id, VehicleRequest request) {
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Vehicle not found with id: " + id));
        
        vehicle.setName(request.getName() != null ? request.getName() : request.getVehicleName());
        vehicle.setColor(request.getColor());
        vehicle.setImage(request.getImage());
        vehicle.setPrice(request.getPrice());
        vehicle.setStock(request.getStock());
        
        // Update dealer if provided
        if (request.getDealerId() != null) {
            Optional<Dealer> dealer = dealerRepository.findById(request.getDealerId());
            dealer.ifPresent(vehicle::setDealer);
        }
        
        Vehicle updated = vehicleRepository.save(vehicle);
        return convertToResponse(updated);
    }

    @Override
    @Transactional
    public void deleteVehicle(Long id) {
        if (!vehicleRepository.existsById(id)) {
            throw new RuntimeException("Vehicle not found with id: " + id);
        }
        vehicleRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<VehicleComparisonDTO> compareVariants(List<Long> variantIds) {
        if (variantIds == null || variantIds.isEmpty()) {
            return new ArrayList<>();
        }

            Long currentDealerId = 1L; // TODO: THAY THẾ '1' BẰNG Dealer ID CỦA USER ĐANG ĐĂNG NHẬP

            List<VehicleVariant> variants = variantRepository.findAllById(variantIds);

            List<VehicleComparisonDTO> comparisonList = variants.stream()
                .map(variant -> {
                // Lấy giá bán mới nhất tương ứng với Dealer của người dùng
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
                })
                .collect(Collectors.toList());

            return comparisonList;
    }
}