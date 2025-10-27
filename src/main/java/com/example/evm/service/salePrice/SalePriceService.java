package com.example.evm.service.salePrice;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.evm.entity.dealer.Dealer;
import com.example.evm.entity.salePrice.SalePrice;
import com.example.evm.entity.vehicle.VehicleVariant;
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
        // ✅ Để DB tự động tạo ID
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
        log.info("✅ Created sale price with ID: {}", savedPrice.getSalepriceId());
        return savedPrice;
    }

    /**
     * Lấy tất cả giá bán
     */
    public List<SalePrice> getAllPrices() {
        return salePriceRepository.findAll();
    }

    /**
     * Lấy giá bán theo ID
     */
    public SalePrice getPriceById(Long id) {
        return salePriceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sale price not found with ID: " + id));
    }

    /**
     * Lấy giá bán theo dealer
     */
    public List<SalePrice> getPricesByDealer(Long dealerId) {
        return salePriceRepository.findByDealerId(dealerId);
    }

    /**
     * Lấy giá bán theo variant
     */
    public List<SalePrice> getPricesByVariant(Long variantId) {
        return salePriceRepository.findByVariantId(variantId);
    }

    /**
     * Lấy giá bán của dealer cho variant cụ thể
     */
    public List<SalePrice> getPricesByDealerAndVariant(Long dealerId, Long variantId) {
        return salePriceRepository.findByDealerIdAndVariantId(dealerId, variantId);
    }

    /**
     * Lấy giá đang hiệu lực cho variant (effectiveDate <= today)
     */
    public List<SalePrice> getActivePricesByVariant(Long variantId) {
        return salePriceRepository.findActivePricesByVariant(variantId, LocalDate.now());
    }

    /**
     * Lấy giá mới nhất của dealer cho variant
     */
    public SalePrice getLatestPriceByDealerAndVariant(Long dealerId, Long variantId) {
        return salePriceRepository.findLatestPriceByDealerAndVariant(dealerId, variantId)
                .orElseThrow(() -> new ResourceNotFoundException(
                    String.format("No sale price found for dealer %d and variant %d", dealerId, variantId)));
    }

    /**
     * Lấy giá trong khoảng
     */
    public List<SalePrice> getPricesByRange(BigDecimal minPrice, BigDecimal maxPrice) {
        return salePriceRepository.findByPriceRange(minPrice, maxPrice);
    }

    /**
     * Cập nhật giá bán
     */
    @Transactional
    public SalePrice updatePrice(Long id, SalePrice priceDetails) {
        SalePrice existingPrice = getPriceById(id);
        
        // Update fields
        if (priceDetails.getPrice() != null) {
            existingPrice.setPrice(priceDetails.getPrice());
        }
        if (priceDetails.getEffectiveDate() != null) {
            existingPrice.setEffectiveDate(priceDetails.getEffectiveDate());
        }
        
        SalePrice updatedPrice = salePriceRepository.save(existingPrice);
        log.info("✅ Updated sale price ID: {}", id);
        return updatedPrice;
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
        log.info("🗑️ Deleted sale price ID: {}", id);
    }
}
