package com.example.evm.service.inventory;

import com.example.evm.dto.inventory.InventoryResponse;
import com.example.evm.dto.inventory.ManufacturerStockResponse;
import com.example.evm.dto.inventory.AllocationRequest; 
import com.example.evm.dto.inventory.StockRequest; 
import com.example.evm.entity.dealer.Dealer;
import com.example.evm.entity.inventory.InventoryStock;
import com.example.evm.entity.inventory.ManufacturerStock; 
import com.example.evm.entity.vehicle.VehicleVariant;
import com.example.evm.exception.ResourceNotFoundException; 
import com.example.evm.repository.dealer.DealerRepository;
import com.example.evm.repository.inventory.InventoryStockRepository;
import com.example.evm.repository.inventory.ManufacturerStockRepository; 
import com.example.evm.repository.vehicle.VehicleVariantRepository; 
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InventoryServiceImpl implements InventoryService {

    private final InventoryStockRepository inventoryRepository;
    private final ManufacturerStockRepository manufacturerStockRepo;
    private final VehicleVariantRepository variantRepository;
    private final DealerRepository dealerRepository;

    // --- 1. KHO ĐẠI LÝ ---

    @Override
    @Transactional(readOnly = true)
    public List<InventoryResponse> getAllDealerStock() {
        return inventoryRepository.findAllWithRelations()
                .stream()
                .map(this::mapToDealerResponse) 
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public InventoryResponse addOrUpdateDealerStock(StockRequest request) {
        VehicleVariant variant = variantRepository.findById(request.getVariantId())
                .orElseThrow(() -> new ResourceNotFoundException("Variant not found"));
        Dealer dealer = dealerRepository.findById(request.getDealerId())
                .orElseThrow(() -> new ResourceNotFoundException("Dealer not found"));

        InventoryStock stock = inventoryRepository
                .findByVariantVariantIdAndColorAndDealerDealerId(
                        request.getVariantId(),
                        request.getColor(),
                        request.getDealerId()
                )
                .map(existingStock -> {
                    existingStock.setQuantity(request.getQuantity()); 
                    
                    existingStock.setListingPrice(request.getListingPrice());
                    existingStock.setStatus(request.getStatus());
                    return existingStock;
                })
                .orElseGet(() -> {
                    InventoryStock newStock = new InventoryStock();
                    newStock.setVariant(variant);
                    newStock.setDealer(dealer);
                    newStock.setColor(request.getColor());
                    newStock.setQuantity(request.getQuantity());
                    newStock.setListingPrice(request.getListingPrice());
                    newStock.setStatus(request.getStatus());
                    return newStock;
                });

        InventoryStock savedStock = inventoryRepository.save(stock);
        return mapToDealerResponse(savedStock);
    }
    
    @Override
    public InventoryResponse updateStockStatus(Long id, String status) {
        InventoryStock existing = inventoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Dealer stock not found"));
        existing.setStatus(status);
        return mapToDealerResponse(inventoryRepository.save(existing));
    }

    @Override
    public void deleteStock(Long id) {
        inventoryRepository.deleteById(id);
    }

    // --- 2. KHO TỔNG ---

    @Override
    @Transactional(readOnly = true)
    public List<ManufacturerStockResponse> getAllManufacturerStock() {
        return manufacturerStockRepo.findAllWithRelations()
                .stream()
                .map(this::mapToManufacturerResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ManufacturerStockResponse addOrUpdateManufacturerStock(StockRequest request) {
        VehicleVariant variant = variantRepository.findById(request.getVariantId())
                .orElseThrow(() -> new ResourceNotFoundException("Variant not found"));

        ManufacturerStock stock = manufacturerStockRepo
                .findByVariantVariantIdAndColor(request.getVariantId(), request.getColor())
                .map(existingStock -> {
                    existingStock.setQuantity(request.getQuantity()); 
                    
                    existingStock.setStatus(request.getStatus());
                    return existingStock;
                })
                .orElseGet(() -> {
                    ManufacturerStock newStock = new ManufacturerStock();
                    newStock.setVariant(variant);
                    newStock.setColor(request.getColor());
                    newStock.setQuantity(request.getQuantity());
                    newStock.setStatus(request.getStatus()); 
                    return newStock;
                });
        
        ManufacturerStock savedStock = manufacturerStockRepo.save(stock);
        return mapToManufacturerResponse(savedStock); 
    }

    @Override
    @Transactional
    public ManufacturerStockResponse updateManufacturerStockStatus(Long id, String status) {
        ManufacturerStock existing = manufacturerStockRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Manufacturer stock item not found"));
        existing.setStatus(status);
        ManufacturerStock savedStock = manufacturerStockRepo.save(existing);
        return mapToManufacturerResponse(savedStock);
    }

    // --- 3. ĐIỀU PHỐI ---

    @Override
    @Transactional
    public InventoryResponse allocateStockToDealer(AllocationRequest request) {
        
        Dealer dealer = dealerRepository.findById(request.getDealerId())
                .orElseThrow(() -> new ResourceNotFoundException("Dealer not found"));
        VehicleVariant variant = variantRepository.findById(request.getVariantId())
                .orElseThrow(() -> new ResourceNotFoundException("Variant not found"));

        // 1. TRỪ KHO TỔNG
        ManufacturerStock centralStock = manufacturerStockRepo
                .findByVariantVariantIdAndColor(request.getVariantId(), request.getColor())
                .orElseThrow(() -> new ResourceNotFoundException("Item not found in central stock"));

        if (centralStock.getQuantity() < request.getQuantity()) {
            throw new RuntimeException("Not enough stock in central warehouse");
        }
        centralStock.setQuantity(centralStock.getQuantity() - request.getQuantity());
        manufacturerStockRepo.save(centralStock);

        // 2. CỘNG KHO ĐẠI LÝ
        InventoryStock dealerStock = inventoryRepository
                .findByVariantVariantIdAndColorAndDealerDealerId(
                        request.getVariantId(),
                        request.getColor(),
                        request.getDealerId()
                )
                .orElseGet(() -> {
                    InventoryStock newStock = new InventoryStock();
                    newStock.setVariant(variant);
                    newStock.setDealer(dealer);
                    newStock.setColor(request.getColor());
                    newStock.setQuantity(0); 
                    newStock.setStatus("In Stock");
                    newStock.setListingPrice(variant.getMsrp()); 
                    return newStock;
                });
        
        dealerStock.setQuantity(dealerStock.getQuantity() + request.getQuantity());
        InventoryStock savedDealerStock = inventoryRepository.save(dealerStock);
        
        return mapToDealerResponse(savedDealerStock);
    }

    @Override
    @Transactional
    public String recallStockFromDealer(AllocationRequest request) {

        // 1. TRỪ KHO ĐẠI LÝ
        InventoryStock dealerStock = inventoryRepository
                .findByVariantVariantIdAndColorAndDealerDealerId(
                        request.getVariantId(),
                        request.getColor(),
                        request.getDealerId()
                )
                .orElseThrow(() -> new ResourceNotFoundException("Item not found in dealer stock"));

        if (dealerStock.getQuantity() < request.getQuantity()) {
            throw new RuntimeException("Not enough stock at dealer to recall");
        }
        dealerStock.setQuantity(dealerStock.getQuantity() - request.getQuantity());
        inventoryRepository.save(dealerStock);

        // 2. CỘNG KHO TỔNG
        ManufacturerStock centralStock = manufacturerStockRepo
                .findByVariantVariantIdAndColor(request.getVariantId(), request.getColor())
                .orElseThrow(() -> new ResourceNotFoundException("Item not found in central stock (cannot recall)"));

        centralStock.setQuantity(centralStock.getQuantity() + request.getQuantity());
        manufacturerStockRepo.save(centralStock);

        return "Recalled " + request.getQuantity() + " items to central warehouse.";
    }


    // --- HÀM PRIVATE MAPPER ---

    private InventoryResponse mapToDealerResponse(InventoryStock stock) {
        return new InventoryResponse(stock); 
    }

    private ManufacturerStockResponse mapToManufacturerResponse(ManufacturerStock stock) { 
        ManufacturerStockResponse res = new ManufacturerStockResponse();
        
        res.setId(stock.getManufacturerStockId());
        res.setColor(stock.getColor());
        res.setQuantity(stock.getQuantity());
        res.setStatus(stock.getStatus());
        
        if (stock.getVariant() != null) {
            res.setVariantId(stock.getVariant().getVariantId());  // ✅ Thêm variant_id
            res.setVariantName(stock.getVariant().getName());
            if (stock.getVariant().getModel() != null) {
                res.setModelName(stock.getVariant().getModel().getName());
            }
        }
        
        return res;
    }
}