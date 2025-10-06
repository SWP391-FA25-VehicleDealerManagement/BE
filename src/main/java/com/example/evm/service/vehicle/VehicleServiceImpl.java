package com.example.evm.service.vehicle;

import com.example.evm.dto.vehicle.VehicleComparisonDTO;
import com.example.evm.dto.vehicle.VehicleResponse;
import com.example.evm.entity.vehicle.SalePrice;
import com.example.evm.entity.vehicle.VehicleVariant;
import com.example.evm.repository.vehicle.VehicleRepository;
import com.example.evm.repository.vehicle.VehicleVariantRepository;
import com.example.evm.repository.vehicle.SalePriceRepository; 
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.text.NumberFormat; 
import java.util.Locale;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class VehicleServiceImpl implements VehicleService {

    private final VehicleRepository vehicleRepository;
    private final SalePriceRepository salePriceRepository;
    private final VehicleVariantRepository variantRepository;

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
    
         
    @Override
    @Transactional(readOnly = true)
    public List<VehicleResponse> getAllVehicles() {

        // Khởi tạo đối tượng định dạng tiền tệ
        NumberFormat currencyFormatter = NumberFormat.getNumberInstance(Locale.US);
        currencyFormatter.setMinimumFractionDigits(0);
        currencyFormatter.setMaximumFractionDigits(0);

        return vehicleRepository.findAll().stream().map(vehicle -> {
            VehicleResponse dto = new VehicleResponse();
            dto.setVehicleId(vehicle.getVehicleId());
            dto.setName(vehicle.getName());
            dto.setColor(vehicle.getColor());
            dto.setVehicleImage(vehicle.getImage()); 

            Long variantId = null; 
            Long dealerId = null;

            if (vehicle.getVariant() != null) {
                dto.setVariantName(vehicle.getVariant().getName());
                dto.setVariantImage(vehicle.getVariant().getImage());
                
                variantId = vehicle.getVariant().getVariantId(); 

                if (vehicle.getVariant().getModel() != null) {
                    dto.setModelName(vehicle.getVariant().getModel().getName());
                    dto.setModelDescription(vehicle.getVariant().getModel().getDescription());
                }
            }

            if (vehicle.getDealer() != null) {
                dto.setDealerName(vehicle.getDealer().getDealerName());
                
                dealerId = vehicle.getDealer().getDealerId(); 
            }
            
            // LOGIC LẤY GIÁ BÁN
            if (variantId != null && dealerId != null) {
                Optional<SalePrice> latest = salePriceRepository.findTopByVariantIdAndDealerIdOrderByEffectiveDateDesc(variantId, dealerId);
                latest.ifPresent(sp -> dto.setPrice(sp.getPrice() != null ? currencyFormatter.format(sp.getPrice()) : null));
            }

            return dto;
        }).toList();
    }
}