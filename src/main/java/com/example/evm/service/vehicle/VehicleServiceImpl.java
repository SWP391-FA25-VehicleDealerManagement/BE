package com.example.evm.service.vehicle;

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

@Service
@RequiredArgsConstructor
public class VehicleServiceImpl implements VehicleService {

    private final VehicleRepository vehicleRepository;
    private final DealerRepository dealerRepository;
    private final VehicleVariantRepository vehicleVariantRepository;

    @Override
    @Transactional(readOnly = true)
    public List<VehicleResponse> getAllVehicles() {
        return vehicleRepository.findAll().stream().map(vehicle -> {
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
        }).toList();
    }

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
