package com.example.evm.service.vehicle;

<<<<<<< HEAD
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

=======
import com.example.evm.dto.vehicle.VehicleRequest;
import com.example.evm.dto.vehicle.VehicleResponse;
import com.example.evm.entity.dealer.Dealer;
import com.example.evm.entity.vehicle.Vehicle;
import com.example.evm.entity.vehicle.VehicleVariant;
import com.example.evm.repository.dealer.DealerRepository;
import com.example.evm.repository.vehicle.VehicleRepository;
import com.example.evm.repository.vehicle.VehicleVariantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
>>>>>>> 54ac894e9c24c5857ad6736606c5e3f39b001e8d

@Service
@RequiredArgsConstructor
public class VehicleServiceImpl implements VehicleService {

    private final VehicleRepository vehicleRepository;
<<<<<<< HEAD
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

=======
    private final DealerRepository dealerRepository;
    private final VehicleVariantRepository vehicleVariantRepository;

    @Override
    @Transactional(readOnly = true)
    public List<VehicleResponse> getAllVehicles() {
>>>>>>> 54ac894e9c24c5857ad6736606c5e3f39b001e8d
        return vehicleRepository.findAll().stream().map(vehicle -> {
            VehicleResponse dto = new VehicleResponse();
            dto.setVehicleId(vehicle.getVehicleId());
            dto.setName(vehicle.getName());
            dto.setColor(vehicle.getColor());
<<<<<<< HEAD
            dto.setVehicleImage(vehicle.getImage()); 

            Long variantId = null; 
            Long dealerId = null;
=======
            dto.setImage(vehicle.getImage());
>>>>>>> 54ac894e9c24c5857ad6736606c5e3f39b001e8d

            if (vehicle.getVariant() != null) {
                dto.setVariantName(vehicle.getVariant().getName());
                dto.setVariantImage(vehicle.getVariant().getImage());
<<<<<<< HEAD
                
                variantId = vehicle.getVariant().getVariantId(); 

=======
>>>>>>> 54ac894e9c24c5857ad6736606c5e3f39b001e8d
                if (vehicle.getVariant().getModel() != null) {
                    dto.setModelName(vehicle.getVariant().getModel().getName());
                    dto.setModelDescription(vehicle.getVariant().getModel().getDescription());
                }
            }

            if (vehicle.getDealer() != null) {
                dto.setDealerName(vehicle.getDealer().getDealerName());
<<<<<<< HEAD
                
                dealerId = vehicle.getDealer().getDealerId(); 
            }
            
            // LOGIC LẤY GIÁ BÁN
            if (variantId != null && dealerId != null) {
                Optional<SalePrice> latest = salePriceRepository.findTopByVariantIdAndDealerIdOrderByEffectiveDateDesc(variantId, dealerId);
                latest.ifPresent(sp -> dto.setPrice(sp.getPrice() != null ? currencyFormatter.format(sp.getPrice()) : null));
=======
>>>>>>> 54ac894e9c24c5857ad6736606c5e3f39b001e8d
            }

            return dto;
        }).toList();
    }
<<<<<<< HEAD
}
=======

    @Override
    @Transactional
    public VehicleResponse addVehicle(VehicleRequest request) {
        Vehicle vehicle = new Vehicle();
        vehicle.setName(request.getName());
        vehicle.setColor(request.getColor());
        vehicle.setImage(request.getImage());

        if (request.getDealerId() != null) {
            Dealer dealer = dealerRepository.findById(request.getDealerId())
                    .orElseThrow(() -> new RuntimeException("Dealer not found"));
            vehicle.setDealer(dealer);
        }

        if (request.getVariantId() != null) {
            VehicleVariant variant = vehicleVariantRepository.findById(request.getVariantId())
                    .orElseThrow(() -> new RuntimeException("Variant not found"));
            vehicle.setVariant(variant);
        }

        Vehicle saved = vehicleRepository.save(vehicle);
        return convertToResponse(saved);
    }

    @Override
    @Transactional
    public VehicleResponse updateVehicle(Integer id, VehicleRequest request) {
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Vehicle not found"));

        Optional.ofNullable(request.getName()).ifPresent(vehicle::setName);
        Optional.ofNullable(request.getColor()).ifPresent(vehicle::setColor);
        Optional.ofNullable(request.getImage()).ifPresent(vehicle::setImage);

        if (request.getDealerId() != null) {
            Dealer dealer = dealerRepository.findById(request.getDealerId())
                    .orElseThrow(() -> new RuntimeException("Dealer not found"));
            vehicle.setDealer(dealer);
        }

        if (request.getVariantId() != null) {
            VehicleVariant variant = vehicleVariantRepository.findById(request.getVariantId())
                    .orElseThrow(() -> new RuntimeException("Variant not found"));
            vehicle.setVariant(variant);
        }

        Vehicle updated = vehicleRepository.save(vehicle);
        return convertToResponse(updated);
    }

    @Override
    @Transactional
    public void deleteVehicle(Integer id) {
        if (!vehicleRepository.existsById(id)) {
            throw new RuntimeException("Vehicle not found");
        }
        vehicleRepository.deleteById(id);
    }

    private VehicleResponse convertToResponse(Vehicle vehicle) {
        VehicleResponse dto = new VehicleResponse();
        dto.setVehicleId(vehicle.getVehicleId());
        dto.setName(vehicle.getName());
        dto.setColor(vehicle.getColor());
        dto.setImage(vehicle.getImage());

        if (vehicle.getVariant() != null) {
            dto.setVariantName(vehicle.getVariant().getName());
            dto.setVariantImage(vehicle.getVariant().getImage());
            if (vehicle.getVariant().getModel() != null) {
                dto.setModelName(vehicle.getVariant().getModel().getName());
                dto.setModelDescription(vehicle.getVariant().getModel().getDescription());
            }
        }

        if (vehicle.getDealer() != null) {
            dto.setDealerName(vehicle.getDealer().getDealerName());
        }

        return dto;
    }
}
>>>>>>> 54ac894e9c24c5857ad6736606c5e3f39b001e8d
