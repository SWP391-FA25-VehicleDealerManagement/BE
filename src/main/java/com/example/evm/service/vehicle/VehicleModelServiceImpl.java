package com.example.evm.service.vehicle;

import com.example.evm.dto.vehicle.VehicleModelRequest;
import com.example.evm.dto.vehicle.VehicleModelResponse;
import com.example.evm.entity.vehicle.VehicleModel;
import com.example.evm.exception.ResourceNotFoundException;
import com.example.evm.repository.vehicle.VehicleModelRepository;
import com.example.evm.repository.vehicle.VehicleVariantRepository;
import com.example.evm.exception.ForeignKeyConstraintException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class VehicleModelServiceImpl implements VehicleModelService {

    private final VehicleModelRepository modelRepository;
    private final VehicleVariantRepository variantRepository;

    @Override
    public VehicleModelResponse createModel(VehicleModelRequest request) {
        VehicleModel model = new VehicleModel();
        model.setName(request.getName());
        model.setManufacturer(request.getManufacturer());
        model.setYear(request.getYear()); 
        model.setBodyType(request.getBody_type()); 
        model.setDescription(request.getDescription());
        model.setStatus("ACTIVE");
        VehicleModel savedModel = modelRepository.save(model);
        return new VehicleModelResponse(savedModel);
    }

    @Override
    public List<VehicleModelResponse> getAllModels() {
        return modelRepository.findAll().stream()
                .filter(model -> "ACTIVE".equalsIgnoreCase(model.getStatus()))
                .map(VehicleModelResponse::new)
                .collect(Collectors.toList());
    }

    @Override
    public VehicleModelResponse getModelById(Long id) {
        VehicleModel model = modelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Model not found with id: " + id));
        return new VehicleModelResponse(model);
    }

    @Override
    public VehicleModelResponse updateModel(Long id, VehicleModelRequest request) {
        VehicleModel existingModel = modelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Model not found with id: " + id));

        existingModel.setName(request.getName());
        existingModel.setDescription(request.getDescription());

        VehicleModel updatedModel = modelRepository.save(existingModel);
        return new VehicleModelResponse(updatedModel);
    }

    @Override
    @Transactional
    public void deleteModel(Long id) {
        // 1. Kiểm tra xem Model có tồn tại không
        if (!modelRepository.existsById(id)) {
            throw new ResourceNotFoundException("Model not found with id: " + id);
        }

        // 2. Kiểm tra bảng con (VehicleVariant)
        if (variantRepository.existsByModelModelId(id)) {
            // Nếu đang được sử dụng -> Báo lỗi, không cho xóa
            throw new ForeignKeyConstraintException(
                "Không thể xóa. Dòng xe (Model) này đang được liên kết với ít nhất một phiên bản (Variant)."
            );
        }

        // 3. Nếu không bị ràng buộc, tiến hành xóa
        log.info("Deleting VehicleModel permanently with ID: {}", id);
        modelRepository.deleteById(id);
    }

    @Override
    public void deactivateModel(Long id) {
        VehicleModel model = modelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Model not found with id: " + id));
        model.setStatus("INACTIVE");
        modelRepository.save(model);
    }

    @Override
    public void activateModel(Long id) {
        VehicleModel model = modelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Model not found with id: " + id));
        model.setStatus("ACTIVE");
        modelRepository.save(model);
    }
    
}