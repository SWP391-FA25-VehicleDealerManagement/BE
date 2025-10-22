package com.example.evm.dto.inventory;

// Import Entity (đường dẫn này phải đúng)
import com.example.evm.entity.inventory.InventoryStock;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.text.NumberFormat;
import java.util.Locale;

@JsonPropertyOrder({ "inventoryId", "modelName", "variantName", "color", "listingPrice","quantity", "status", "dealerName" })
@Data
@NoArgsConstructor
public class InventoryResponse {

    @JsonProperty("stockId")
    private Long inventoryId;
    private Integer quantity;
    private String status;
    private String color;
    private String listingPrice;

    private String modelName;
    private String variantName;
    private String dealerName;


    public InventoryResponse(InventoryStock stock) {
        // 1. Map các trường trực tiếp
        this.inventoryId = stock.getStockId();
        this.quantity = stock.getQuantity();
        this.status = stock.getStatus();
        this.color = stock.getColor();

        // 2. Map các trường từ liên kết (giả định Entity đã được fetch)
        if (stock.getVariant() != null) {
            this.variantName = stock.getVariant().getName();
            
            // Lấy cả tên Model
            if (stock.getVariant().getModel() != null) {
                this.modelName = stock.getVariant().getModel().getName();
            }
        }

        // 3. Lấy tên Dealer
        if (stock.getDealer() != null) {
            this.dealerName = stock.getDealer().getDealerName(); 
        }

        // 4. Định dạng giá bán cho đẹp
        if (stock.getListingPrice() != null) {
            NumberFormat formatter = NumberFormat.getNumberInstance(Locale.US);
            String formattedPrice = formatter.format(stock.getListingPrice().doubleValue());
            this.listingPrice = formattedPrice + " VND";
        } else {
            this.listingPrice = "N/A";
        }
    }
}