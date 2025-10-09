package com.example.evm.service.inventory;

import com.example.evm.dto.inventory.InventoryResponse;
import com.example.evm.entity.dealer.Dealer;
import com.example.evm.entity.inventory.InventoryStock;
import com.example.evm.entity.vehicle.Vehicle;
import com.example.evm.repository.dealer.DealerRepository;
import com.example.evm.repository.inventory.InventoryRepository;
import com.example.evm.repository.vehicle.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InventoryServiceImpl implements InventoryService {

    private final InventoryRepository inventoryRepository;
    private final VehicleRepository vehicleRepository;
    private final DealerRepository dealerRepository;

    @Override
    @Transactional(readOnly = true)
    public List<InventoryResponse> getAll() {
        return inventoryRepository.findAllWithRelations()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public InventoryResponse addStock(InventoryStock stock) {
        Vehicle vehicle = vehicleRepository.findById(stock.getVehicle().getVehicleId())
                .orElseThrow(() -> new RuntimeException("Vehicle not found"));
        stock.setVehicle(vehicle);

        if (stock.getDealer() != null && stock.getDealer().getDealerId() != null) {
            Dealer dealer = dealerRepository.findById(stock.getDealer().getDealerId().longValue())
                    .orElseThrow(() -> new RuntimeException("Dealer not found"));
            stock.setDealer(dealer);
        }

        InventoryStock saved = inventoryRepository.save(stock);
        return mapToResponse(saved);
    }

    @Override
    public InventoryResponse updateStock(Integer id, InventoryStock stock) {
        InventoryStock existing = inventoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Stock not found"));

        existing.setQuantity(stock.getQuantity());
        existing.setStatus(stock.getStatus());
        return mapToResponse(inventoryRepository.save(existing));
    }

    @Override
    public void deleteStock(Integer id) {
        inventoryRepository.deleteById(id);
    }

    // 🚗 Điều phối xe từ kho tổng xuống đại lý
    @Transactional
    @Override
    public String allocateVehicleToDealer(Integer vehicleId, Integer dealerId, Integer quantity) {
        // ép kiểu Integer → Long
        Long vId = vehicleId.longValue();
        Long dId = dealerId.longValue();

        var centralStock = inventoryRepository.findByDealerIsNullAndVehicle_VehicleId(vId)
                .orElseThrow(() -> new RuntimeException("Vehicle not available in central stock"));

        if (centralStock.getQuantity() < quantity) {
            throw new RuntimeException("Not enough stock in central warehouse");
        }

        Dealer dealer = dealerRepository.findById(dId)
                .orElseThrow(() -> new RuntimeException("Dealer not found"));
        Vehicle vehicle = centralStock.getVehicle();

        var dealerStock = inventoryRepository.findByDealerAndVehicle(dealer, vehicle)
                .orElseGet(() -> {
                    InventoryStock newStock = new InventoryStock();
                    newStock.setDealer(dealer);
                    newStock.setVehicle(vehicle);
                    newStock.setQuantity(0);
                    newStock.setStatus("AVAILABLE");
                    return inventoryRepository.save(newStock);
                });

        // cập nhật số lượng
        centralStock.setQuantity(centralStock.getQuantity() - quantity);
        dealerStock.setQuantity(dealerStock.getQuantity() + quantity);

        inventoryRepository.save(centralStock);
        inventoryRepository.save(dealerStock);
        return "Allocated " + quantity + " vehicles to dealer " + dealer.getDealerName();
    }

    // 🔁 Thu hồi xe từ đại lý về kho tổng
    @Transactional
    @Override
    public String recallVehicleFromDealer(Integer vehicleId, Integer dealerId, Integer quantity) {
        Long vId = vehicleId.longValue();
        Long dId = dealerId.longValue();

        Dealer dealer = dealerRepository.findById(dId)
                .orElseThrow(() -> new RuntimeException("Dealer not found"));
        Vehicle vehicle = vehicleRepository.findById(vId)
                .orElseThrow(() -> new RuntimeException("Vehicle not found"));

        var dealerStock = inventoryRepository.findByDealerAndVehicle(dealer, vehicle)
                .orElseThrow(() -> new RuntimeException("Vehicle not found in dealer inventory"));

        if (dealerStock.getQuantity() < quantity) {
            throw new RuntimeException("Not enough stock at dealer to recall");
        }

        var centralStock = inventoryRepository.findByDealerIsNullAndVehicle_VehicleId(vId)
                .orElseGet(() -> {
                    InventoryStock newStock = new InventoryStock();
                    newStock.setDealer(null);
                    newStock.setVehicle(vehicle);
                    newStock.setQuantity(0);
                    newStock.setStatus("AVAILABLE");
                    return inventoryRepository.save(newStock);
                });

        dealerStock.setQuantity(dealerStock.getQuantity() - quantity);
        centralStock.setQuantity(centralStock.getQuantity() + quantity);

        inventoryRepository.save(dealerStock);
        inventoryRepository.save(centralStock);
        return "Recalled " + quantity + " vehicles from dealer " + dealer.getDealerName();
    }

    // ✅ Map entity → DTO
    private InventoryResponse mapToResponse(InventoryStock stock) {
        Vehicle v = stock.getVehicle();
        return InventoryResponse.builder()
                .stockId(stock.getStockId())
                .quantity(stock.getQuantity())
                .status(stock.getStatus())
                .vehicleName(v != null ? v.getName() : null)
                .variantName(v != null && v.getVariant() != null ? v.getVariant().getName() : "N/A")
                .color(v != null ? v.getColor() : null)
                .dealerName(stock.getDealer() != null ? stock.getDealer().getDealerName() : "Central Warehouse")
                .build();
    }
}
