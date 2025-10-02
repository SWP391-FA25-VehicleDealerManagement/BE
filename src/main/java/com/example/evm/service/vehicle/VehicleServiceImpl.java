package com.example.evm.service.vehicle;

import com.example.evm.dto.vehicle.VehicleResponse;
import com.example.evm.repository.vehicle.VehicleRepository;
import com.example.evm.repository.vehicle.SalePriceRepository; 
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.text.NumberFormat; 
import java.util.Locale;     

import java.util.List;
import java.util.Optional; 
import org.springframework.security.core.GrantedAuthority;

@Service
@RequiredArgsConstructor
public class VehicleServiceImpl implements VehicleService {

    private final VehicleRepository vehicleRepository;
    private final SalePriceRepository salePriceRepository;

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

            Integer variantId = null; 
            Integer dealerId = null;

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
                Optional<BigDecimal> price = salePriceRepository.findActivePriceByVariantIdAndDealerId(variantId, dealerId);
                
                price.ifPresent(p -> {
                    // <<< CHUYỂN BIGDECIMAL THÀNH STRING SỐ ĐẦY ĐỦ TRƯỚC KHI GÁN >>>
                    dto.setPrice(currencyFormatter.format(p));
                });
            }

            return dto;
        }).toList();
    }
}