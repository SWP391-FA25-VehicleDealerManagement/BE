package com.example.evm.service.vehicle;

import com.example.evm.dto.vehicle.VehicleDetailRequest;
import com.example.evm.dto.vehicle.VehicleDetailResponse;
import com.example.evm.dto.vehicle.VehicleVariantRequest;
import com.example.evm.dto.vehicle.VehicleVariantResponse;
import com.example.evm.entity.vehicle.VehicleDetail;
import com.example.evm.entity.vehicle.VehicleModel;
import com.example.evm.entity.vehicle.VehicleVariant;
import com.example.evm.exception.ResourceNotFoundException;
import com.example.evm.repository.vehicle.VehicleDetailRepository;
import com.example.evm.repository.vehicle.VehicleModelRepository;
import com.example.evm.repository.vehicle.VehicleVariantRepository;
import com.example.evm.service.storage.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.transaction.annotation.Transactional;


import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VehicleVariantServiceImpl implements VehicleVariantService {

    private final VehicleVariantRepository variantRepository;
    private final VehicleModelRepository modelRepository;
    private final FileStorageService fileStorageService;
    private final VehicleDetailRepository detailRepository;

    @Override
    public VehicleVariantResponse createVariant(VehicleVariantRequest request, MultipartFile file) {
    // 1. GỌI FILESTORAGESERVICE ĐỂ LƯU FILE VÀ LẤY VỀ TÊN FILE
    String filename = fileStorageService.save(file);

    // 2. TẠO ĐƯỜNG DẪN URL ĐỂ LƯU VÀO DATABASE
    String imageUrl = "/api/variants/images/" + filename;

    // 3. TÌM MODEL (DÒNG XE) TƯƠNG ỨNG
    VehicleModel model = modelRepository.findById(request.getModelId())
            .orElseThrow(() -> new ResourceNotFoundException("Model not found with id: " + request.getModelId()));

    // 4. TẠO ĐỐI TƯỢNG VARIANT MỚI
    VehicleVariant variant = new VehicleVariant();
    variant.setName(request.getName());
    variant.setImage(imageUrl);
    variant.setModel(model);
    variant.setStatus("ACTIVE");

    // 5. LƯU VÀO DATABASE VÀ TRẢ VỀ KẾT QUẢ
    VehicleVariant savedVariant = variantRepository.save(variant);
    return new VehicleVariantResponse(savedVariant);
    }

    @Override
    public List<VehicleVariantResponse> getAllVariants() {
        return variantRepository.findAll().stream()
                .filter(variant -> "ACTIVE".equalsIgnoreCase(variant.getStatus()))
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
    public VehicleVariantResponse updateVariant(Long id, VehicleVariantRequest request, MultipartFile file) {
        VehicleVariant existingVariant = variantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Variant not found with id: " + id));

        // Xử lý file ảnh NẾU người dùng có gửi file mới để cập nhật
        if (file != null && !file.isEmpty()) {
            String filename = fileStorageService.save(file);
            String imageUrl = "/api/variants/images/" + filename;
            existingVariant.setImage(imageUrl); // Cập nhật đường dẫn ảnh mới
        }

        // Cập nhật các thông tin khác
        VehicleModel model = modelRepository.findById(request.getModelId())
                .orElseThrow(() -> new ResourceNotFoundException("Model not found with id: " + request.getModelId()));

        existingVariant.setName(request.getName());
        existingVariant.setModel(model);

        VehicleVariant updatedVariant = variantRepository.save(existingVariant);
        return new VehicleVariantResponse(updatedVariant);
    }

    @Override
    public void deactivateVariant(Long id) {
        VehicleVariant variant = variantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Variant not found with id: " + id));
        variant.setStatus("INACTIVE");
        variantRepository.save(variant);
    }

    @Override
    public void activateVariant(Long id) {
        VehicleVariant variant = variantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Variant not found with id: " + id));
        variant.setStatus("ACTIVE");
        variantRepository.save(variant);
    }

    @Override
    @Transactional
    public VehicleDetailResponse addOrUpdateDetails(Long variantId, VehicleDetailRequest request) {
        // Tìm variant tương ứng
        VehicleVariant variant = variantRepository.findById(variantId)
                .orElseThrow(() -> new ResourceNotFoundException("Variant not found with id: " + variantId));

        // Kiểm tra xem detail đã tồn tại chưa, nếu chưa thì tạo mới
        VehicleDetail detail = detailRepository.findByVariant_VariantId(variantId)
                .orElse(new VehicleDetail());

        // Map dữ liệu từ request vào entity
        detail.setVariant(variant);
        detail.setDimensionsMm(request.getDimensionsMm());
        detail.setWheelbaseMm(request.getWheelbaseMm());
        // ... Set tất cả các trường còn lại ...
    
        VehicleDetail savedDetail = detailRepository.save(detail);
        return new VehicleDetailResponse(savedDetail);
    }   

    @Override
    @Transactional(readOnly = true)
    public VehicleDetailResponse getDetailsByVariantId(Long variantId) {
        VehicleDetail detail = detailRepository.findByVariant_VariantId(variantId)
                .orElseThrow(() -> new ResourceNotFoundException("Details not found for variant id: " + variantId));
        return new VehicleDetailResponse(detail);
    }
}