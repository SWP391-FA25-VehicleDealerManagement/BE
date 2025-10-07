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

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VehicleServiceImpl implements VehicleService {

    private final VehicleRepository vehicleRepository;
    private final VehicleVariantRepository variantRepository;
    private final SalePriceRepository salePriceRepository;
    private final DealerRepository dealerRepository;

    @Override
    public List<VehicleResponse> getAllVehicles() {
        return vehicleRepository.findAll().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
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
    public List<VehicleComparisonDTO> compareVariants(List<Long> variantIds) {
        List<VehicleComparisonDTO> comparisonData = new ArrayList<>();
        
        for (Long variantId : variantIds) {
            Optional<VehicleVariant> variantOpt = variantRepository.findById(variantId);
            if (variantOpt.isPresent()) {
                VehicleVariant variant = variantOpt.get();
                VehicleComparisonDTO dto = VehicleComparisonDTO.builder()
                    .variantId(variant.getVariantId())
                    .variantName(variant.getVariantName())
                    .engineType(variant.getEngineType())
                    .transmission(variant.getTransmission())
                    .fuelType(variant.getFuelType())
                    .seatingCapacity(variant.getSeatingCapacity())
                    .build();
                
                // Get sale price - using a simple approach for now
                // Note: This would need proper implementation based on your business logic
                dto.setPrice(BigDecimal.ZERO);
                dto.setDiscountPercentage(0);
                
                comparisonData.add(dto);
            }
        }
        
        return comparisonData;
    }

    private VehicleResponse convertToResponse(Vehicle vehicle) {
        return new VehicleResponse(vehicle);
    }
}