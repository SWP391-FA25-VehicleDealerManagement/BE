package com.example.evm.service.vehicle;

import com.example.evm.dto.vehicle.VehicleVariantRequest;
import com.example.evm.dto.vehicle.VehicleVariantResponse;
import com.example.evm.entity.vehicle.VehicleModel;
import com.example.evm.entity.vehicle.VehicleVariant;
import com.example.evm.exception.ResourceNotFoundException;
import com.example.evm.repository.vehicle.VehicleModelRepository;
import com.example.evm.repository.vehicle.VehicleVariantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VehicleVariantServiceImpl implements VehicleVariantService {

    private final VehicleVariantRepository variantRepository;
    private final VehicleModelRepository modelRepository; // Cần có Repository này

    @Override
    public VehicleVariantResponse createVariant(VehicleVariantRequest request) {
        // Tìm Model (dòng xe) tương ứng
        VehicleModel model = modelRepository.findById(request.getModelId())
                .orElseThrow(() -> new ResourceNotFoundException("Model not found with id: " + request.getModelId()));

        VehicleVariant variant = new VehicleVariant();
        variant.setName(request.getName());
        variant.setImage(request.getImage());
        variant.setModel(model);

        VehicleVariant savedVariant = variantRepository.save(variant);
        return new VehicleVariantResponse(savedVariant);
    }

    @Override
    public List<VehicleVariantResponse> getAllVariants() {
        return variantRepository.findAll().stream()
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
    public VehicleVariantResponse updateVariant(Long id, VehicleVariantRequest request) {
        VehicleVariant existingVariant = variantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Variant not found with id: " + id));

        VehicleModel model = modelRepository.findById(request.getModelId())
                .orElseThrow(() -> new ResourceNotFoundException("Model not found with id: " + request.getModelId()));

        existingVariant.setName(request.getName());
        existingVariant.setImage(request.getImage());
        existingVariant.setModel(model);

        VehicleVariant updatedVariant = variantRepository.save(existingVariant);
        return new VehicleVariantResponse(updatedVariant);
    }

    @Override
    public void deleteVariant(Long id) {
        if (!variantRepository.existsById(id)) {
            throw new ResourceNotFoundException("Variant not found with id: " + id);
        }
        variantRepository.deleteById(id);
    }
}