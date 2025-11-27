package com.example.evm.service.salePrice;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import com.example.evm.dto.salePrice.SalePriceResponse;
import com.example.evm.entity.salePrice.SalePrice;

import com.example.evm.exception.ResourceNotFoundException;
import com.example.evm.repository.dealer.DealerRepository;
import com.example.evm.repository.salePrice.SalePriceRepository;
import com.example.evm.repository.vehicle.VehicleVariantRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service để quản lý giá bán của dealer cho từng variant
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SalePriceService {
    
    private final SalePriceRepository salePriceRepository;
    private final DealerRepository dealerRepository;
    private final VehicleVariantRepository vehicleVariantRepository;

    /**
     * Tạo mới giá bán
     */
    @Transactional
    public SalePrice createPrice(SalePrice salePrice) {
        // Để DB tự động tạo ID
        salePrice.setSalepriceId(null);
        
        // Validate dealer exists
        if (!dealerRepository.existsById(salePrice.getDealerId())) {
            throw new ResourceNotFoundException("Dealer not found with ID: " + salePrice.getDealerId());
        }
        
        // Validate variant exists
        if (!vehicleVariantRepository.existsById(salePrice.getVariantId())) {
            throw new ResourceNotFoundException("Variant not found with ID: " + salePrice.getVariantId());
        }
        
        SalePrice savedPrice = salePriceRepository.save(salePrice);
        log.info("Created sale price with ID: {}", savedPrice.getSalepriceId());
        
        // Fetch lại với variant và model để đảm bảo có đầy đủ thông tin
        return salePriceRepository.findByIdWithVariantAndModel(savedPrice.getSalepriceId())
                .orElse(savedPrice);
    }

    /**
     * Convert SalePrice entity to SalePriceResponse DTO
     */
    private SalePriceResponse toResponse(SalePrice salePrice) {
        return new SalePriceResponse(salePrice);
    }

    /**
     * Convert list of SalePrice entities to SalePriceResponse DTOs
     */
    private List<SalePriceResponse> toResponseList(List<SalePrice> salePrices) {
        return salePrices.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Lấy tất cả giá bán
     */
    public List<SalePriceResponse> getAllPrices() {
        List<SalePrice> prices = salePriceRepository.findAllWithVariantAndModel();
        return toResponseList(prices);
    }

    /**
     * Lấy giá bán entity theo ID (dùng cho internal operations)
     */
    private SalePrice getPriceEntityById(Long id) {
        // Fetch với variant và model để đảm bảo có đầy đủ thông tin
        return salePriceRepository.findByIdWithVariantAndModel(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sale price not found with ID: " + id));
    }

    /**
     * Lấy giá bán theo ID (trả về DTO)
     */
    public SalePriceResponse getPriceById(Long id) {
        SalePrice price = getPriceEntityById(id);
        return toResponse(price);
    }

    /**
     * Lấy giá bán theo dealer
     */
    public List<SalePriceResponse> getPricesByDealer(Long dealerId) {
        List<SalePrice> prices = salePriceRepository.findByDealerId(dealerId);
        return toResponseList(prices);
    }

    /**
     * Lấy giá bán theo variant
     */
    public List<SalePriceResponse> getPricesByVariant(Long variantId) {
        List<SalePrice> prices = salePriceRepository.findByVariantId(variantId);
        return toResponseList(prices);
    }

    /**
     * Lấy giá bán của dealer cho variant cụ thể
     */
    public List<SalePriceResponse> getPricesByDealerAndVariant(Long dealerId, Long variantId) {
        List<SalePrice> prices = salePriceRepository.findByDealerIdAndVariantId(dealerId, variantId);
        return toResponseList(prices);
    }

    /**
     * Lấy giá đang hiệu lực cho variant (effectiveDate <= today)
     */
    public List<SalePriceResponse> getActivePricesByVariant(Long variantId) {
        List<SalePrice> prices = salePriceRepository.findActivePricesByVariant(variantId, LocalDate.now());
        return toResponseList(prices);
    }

    /**
     * Lấy giá mới nhất của dealer cho variant
     */
    public SalePriceResponse getLatestPriceByDealerAndVariant(Long dealerId, Long variantId) {
        SalePrice price = salePriceRepository.findLatestPriceByDealerAndVariant(dealerId, variantId)
                .orElseThrow(() -> new ResourceNotFoundException(
                    String.format("No sale price found for dealer %d and variant %d", dealerId, variantId)));
        return toResponse(price);
    }

    /**
     * Lấy giá trong khoảng
     */
    public List<SalePriceResponse> getPricesByRange(BigDecimal minPrice, BigDecimal maxPrice) {
        List<SalePrice> prices = salePriceRepository.findByPriceRange(minPrice, maxPrice);
        return toResponseList(prices);
    }

    /**
     * Cập nhật giá bán
     */
    @Transactional
    public SalePrice updatePrice(Long id, SalePrice priceDetails) {
        SalePrice existingPrice = getPriceEntityById(id);
        
        // Update fields
        if (priceDetails.getBasePrice() != null) {
            existingPrice.setBasePrice(priceDetails.getBasePrice());
        }
        if (priceDetails.getPrice() != null) {
            existingPrice.setPrice(priceDetails.getPrice());
        }
        if (priceDetails.getEffectiveDate() != null) {
            existingPrice.setEffectiveDate(priceDetails.getEffectiveDate());
        }
        
        salePriceRepository.save(existingPrice);
        log.info("Updated sale price ID: {}", id);
        
        // Fetch lại với variant và model để đảm bảo có đầy đủ thông tin
        return getPriceEntityById(id);
    }

    /**
     * Xóa giá bán
     */
    @Transactional
    public void deletePrice(Long id) {
        if (!salePriceRepository.existsById(id)) {
            throw new ResourceNotFoundException("Sale price not found with ID: " + id);
        }
        
        salePriceRepository.deleteById(id);
        log.info("Deleted sale price ID: {}", id);
    }
}
