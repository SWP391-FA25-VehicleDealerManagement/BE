package com.example.evm.service.vehicle;

import com.example.evm.dto.vehicle.VehicleModelRequest;
import com.example.evm.dto.vehicle.VehicleModelResponse;
import com.example.evm.entity.vehicle.VehicleModel;
import com.example.evm.exception.ResourceNotFoundException;
import com.example.evm.repository.vehicle.VehicleModelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VehicleModelServiceImpl implements VehicleModelService {

    private final VehicleModelRepository modelRepository;

    @Override
    public VehicleModelResponse createModel(VehicleModelRequest request) {
        VehicleModel model = new VehicleModel();
        model.setName(request.getName());
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