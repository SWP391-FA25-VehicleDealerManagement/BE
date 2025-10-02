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
import java.util.Locale;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.security.core.GrantedAuthority;

@Service
@RequiredArgsConstructor
public class VehicleServiceImpl implements VehicleService {

    private final VehicleRepository vehicleRepository;
    private final SalePriceRepository salePriceRepository;
    private final VehicleVariantRepository variantRepository;
    private final DealerRepository dealerRepository;

    @Override
    @Transactional(readOnly = true)
    public List<VehicleComparisonDTO> compareVariants(List<Integer> variantIds) {
        if (variantIds == null || variantIds.isEmpty()) {
            return new ArrayList<>();
        }

            Integer currentDealerId = 1; // TODO: THAY THẾ '1' BẰNG Dealer ID CỦA USER ĐANG ĐĂNG NHẬP

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

    @Override
    @Transactional
    public VehicleResponse createVehicle(VehicleRequest request) {
        // 1. Tìm Entity tham chiếu
        VehicleVariant variant = variantRepository.findById(request.getVariantId())
                .orElseThrow(() -> new IllegalArgumentException("Variant ID không hợp lệ: " + request.getVariantId()));
        
        Dealer dealer = dealerRepository.findById(request.getDealerId())
                .orElseThrow(() -> new IllegalArgumentException("Dealer ID không hợp lệ: " + request.getDealerId()));

        // 2. Tạo Entity Vehicle
        Vehicle vehicle = new Vehicle();
        vehicle.setName(request.getName());
        vehicle.setColor(request.getColor());
        vehicle.setImage(request.getImage());
        
        // 3. Gán Entity tham chiếu
        vehicle.setVariant(variant);
        vehicle.setDealer(dealer);

        // 4. Lưu và trả về DTO Response (Giả sử bạn có Mapper hoặc Constructor)
        Vehicle savedVehicle = vehicleRepository.save(vehicle);
        
        // Cần ánh xạ Vehicle Entity trở lại VehicleResponse DTO
        // Tạm thời trả về đối tượng ánh xạ đơn giản (bạn cần tự implement Mapper/Logic)
        return mapVehicleToResponse(savedVehicle); 
    }

    @Override
    @Transactional
    public VehicleResponse updateVehicle(Integer vehicleId, VehicleRequest request) {
        // 1. Tìm Vehicle cần cập nhật
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new IllegalArgumentException("Vehicle ID không tìm thấy: " + vehicleId));
        
        // 2. Tìm Entity tham chiếu mới
        VehicleVariant variant = variantRepository.findById(request.getVariantId())
                .orElseThrow(() -> new IllegalArgumentException("Variant ID không hợp lệ: " + request.getVariantId()));
        
        Dealer dealer = dealerRepository.findById(request.getDealerId())
                .orElseThrow(() -> new IllegalArgumentException("Dealer ID không hợp lệ: " + request.getDealerId()));

        // 3. Cập nhật các trường
        vehicle.setName(request.getName());
        vehicle.setColor(request.getColor());
        vehicle.setImage(request.getImage());
        vehicle.setVariant(variant);
        vehicle.setDealer(dealer);

        // 4. Lưu và trả về
        Vehicle updatedVehicle = vehicleRepository.save(vehicle);
        return mapVehicleToResponse(updatedVehicle);
    }

    @Override
    @Transactional
    public void deleteVehicle(Integer vehicleId) {
        if (!vehicleRepository.existsById(vehicleId)) {
            throw new IllegalArgumentException("Vehicle ID không tìm thấy: " + vehicleId);
        }
        vehicleRepository.deleteById(vehicleId);
    }
    
    // Phương thức ánh xạ tạm thời (Bạn cần đảm bảo nó tồn tại và hoạt động)
    private VehicleResponse mapVehicleToResponse(Vehicle vehicle) {
        // Đây là một placeholder, bạn cần logic ánh xạ thực tế từ Vehicle -> VehicleResponse
        VehicleResponse response = new VehicleResponse();
        response.setVehicleId(vehicle.getVehicleId());
        response.setName(vehicle.getName());
        response.setColor(vehicle.getColor());
        response.setVehicleImage(vehicle.getImage());
        
        // ... Ánh xạ các trường từ Variant và Dealer (tương tự như trong getAllVehicles)
        
        return response;
    }

    @Override
    public VehicleResponse getVehicleById(Integer vehicleId) {
        // Tìm kiếm xe theo ID. Nếu không tìm thấy, ném ngoại lệ.
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
            .orElseThrow(() -> new IllegalArgumentException("Vehicle ID không tìm thấy: " + vehicleId));

        // Ánh xạ Entity sang DTO và trả về
        return mapVehicleToResponse(vehicle);

    }
}