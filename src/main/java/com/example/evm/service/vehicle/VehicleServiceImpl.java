package com.example.evm.service.vehicle;

import com.example.evm.dto.vehicle.VehicleRequest;
import com.example.evm.dto.vehicle.VehicleResponse;
import com.example.evm.entity.dealer.Dealer;
import com.example.evm.entity.vehicle.Vehicle;
import com.example.evm.repository.dealer.DealerRepository;
import com.example.evm.repository.vehicle.VehicleRepository;
import com.example.evm.repository.vehicle.VehicleVariantRepository;
import com.example.evm.repository.vehicle.SalePriceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
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

    private VehicleResponse convertToResponse(Vehicle vehicle) {
        return new VehicleResponse(vehicle);
    }
}