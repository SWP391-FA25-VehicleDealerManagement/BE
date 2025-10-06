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
        return inventoryRepository.findAll().stream().map(stock -> {
            InventoryResponse dto = new InventoryResponse();
            dto.setStockId(stock.getStockId());
            dto.setQuantity(stock.getQuantity());
            dto.setStatus(stock.getStatus());

            Vehicle vehicle = stock.getVehicle();
            if (vehicle != null) {
                dto.setVehicleName(vehicle.getName());
                dto.setColor(vehicle.getColor());
                if (vehicle.getVariant() != null) {
                    dto.setVariantName(vehicle.getVariant().getName());
                }
            }

            Dealer dealer = stock.getDealer();
            if (dealer != null) {
                dto.setDealerName(dealer.getDealerName());
            }

            return dto;
        }).toList();
    }

    @Override
    public InventoryResponse addStock(InventoryStock stock) {
        Vehicle vehicle = vehicleRepository.findById(stock.getVehicle().getVehicleId())
                .orElseThrow(() -> new RuntimeException("Vehicle not found"));
        Dealer dealer = dealerRepository.findById(stock.getDealer().getDealerId())
                .orElseThrow(() -> new RuntimeException("Dealer not found"));

        stock.setVehicle(vehicle);
        stock.setDealer(dealer);

        InventoryStock saved = inventoryRepository.save(stock);
        return mapToResponse(saved);
    }

    @Override
    public InventoryResponse updateStock(Integer id, InventoryStock stock) {
        InventoryStock existing = inventoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Stock not found"));

        existing.setQuantity(stock.getQuantity());
        existing.setStatus(stock.getStatus());

        if (stock.getVehicle() != null) {
            existing.setVehicle(stock.getVehicle());
        }
        if (stock.getDealer() != null) {
            existing.setDealer(stock.getDealer());
        }

        InventoryStock updated = inventoryRepository.save(existing);
        return mapToResponse(updated);
    }

    @Override
    public void deleteStock(Integer id) {
        inventoryRepository.deleteById(id);
    }

    private InventoryResponse mapToResponse(InventoryStock stock) {
        return InventoryResponse.builder()
                .stockId(stock.getStockId())
                .quantity(stock.getQuantity())
                .status(stock.getStatus())
                .vehicleName(stock.getVehicle() != null ? stock.getVehicle().getName() : null)
                .variantName(stock.getVehicle() != null && stock.getVehicle().getVariant() != null
                        ? stock.getVehicle().getVariant().getName() : null)
                .color(stock.getVehicle() != null ? stock.getVehicle().getColor() : null)
                .dealerName(stock.getDealer() != null ? stock.getDealer().getDealerName() : null)
                .build();
    }
}
