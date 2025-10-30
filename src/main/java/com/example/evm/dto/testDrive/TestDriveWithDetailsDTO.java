package com.example.evm.dto.testDrive;

import com.example.evm.entity.customer.Customer;
import com.example.evm.entity.dealer.Dealer;
import com.example.evm.entity.testDrive.TestDrive;
import com.example.evm.entity.vehicle.Vehicle;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * DTO để trả về TestDrive kèm thông tin cơ bản của Dealer, Customer, Vehicle
 * (không include detail phức tạp như VehicleDetail)
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TestDriveWithDetailsDTO {
    
    // ===== THÔNG TIN TEST DRIVE =====
    private Long testDriveId;
    private LocalDateTime scheduledDate;
    private String status;
    private String notes;
    private String assignedBy;
    private LocalDateTime createdDate;
    
    // ===== THÔNG TIN CƠ BẢN CỦA DEALER =====
    private DealerInfo dealer;
    
    // ===== THÔNG TIN CƠ BẢN CỦA CUSTOMER =====
    private CustomerInfo customer;
    
    // ===== THÔNG TIN CƠ BẢN CỦA VEHICLE =====
    private VehicleInfo vehicle;
    
    // ===== INNER CLASSES =====
    
    @Data
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class DealerInfo {
        private Long dealerId;
        private String dealerName;
        private String phone;
        private String address;
        
        public static DealerInfo fromDealer(Dealer dealer) {
            if (dealer == null) return null;
            DealerInfo info = new DealerInfo();
            info.setDealerId(dealer.getDealerId());
            info.setDealerName(dealer.getDealerName());
            info.setPhone(dealer.getPhone());
            info.setAddress(dealer.getAddress());
            return info;
        }
    }
    
    @Data
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class CustomerInfo {
        private Long customerId;
        private String customerName;
        private String email;
        private String phone;
        
        public static CustomerInfo fromCustomer(Customer customer) {
            if (customer == null) return null;
            CustomerInfo info = new CustomerInfo();
            info.setCustomerId(customer.getCustomerId());
            info.setCustomerName(customer.getCustomerName());
            info.setEmail(customer.getEmail());
            info.setPhone(customer.getPhone());
            return info;
        }
    }
    
    @Data
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class VehicleInfo {
        private Long vehicleId;
        private String vinNumber;
        private String color;
        private String imageUrl;
        private LocalDateTime manufactureDate;
        private LocalDateTime warrantyExpiryDate;
        
        // Thông tin variant cơ bản (không có detail)
        private VehicleVariantInfo variant;
        
        @Data
        @JsonInclude(JsonInclude.Include.NON_NULL)
        public static class VehicleVariantInfo {
            private Long variantId;
            private String name;
            private VehicleModelInfo model;
            
            @Data
            @JsonInclude(JsonInclude.Include.NON_NULL)
            public static class VehicleModelInfo {
                private Long modelId;
                private String name;
                private String manufacturer;
                private Integer year;
                private String bodyType;
                
                public static VehicleModelInfo fromModel(com.example.evm.entity.vehicle.VehicleModel model) {
                    if (model == null) return null;
                    VehicleModelInfo info = new VehicleModelInfo();
                    info.setModelId(model.getModelId());
                    info.setName(model.getName());
                    info.setManufacturer(model.getManufacturer());
                    info.setYear(model.getYear());
                    info.setBodyType(model.getBodyType());
                    return info;
                }
            }
            
            public static VehicleVariantInfo fromVariant(com.example.evm.entity.vehicle.VehicleVariant variant) {
                if (variant == null) return null;
                VehicleVariantInfo info = new VehicleVariantInfo();
                info.setVariantId(variant.getVariantId());
                info.setName(variant.getName());
                // Load model thông tin
                if (variant.getModel() != null) {
                    info.setModel(VehicleModelInfo.fromModel(variant.getModel()));
                }
                return info;
            }
        }
        
        public static VehicleInfo fromVehicle(Vehicle vehicle) {
            if (vehicle == null) return null;
            VehicleInfo info = new VehicleInfo();
            info.setVehicleId(vehicle.getVehicleId());
            info.setVinNumber(vehicle.getVinNumber());
            info.setColor(vehicle.getColor());
            info.setImageUrl(vehicle.getImageUrl());
            if (vehicle.getManufactureDate() != null) {
                info.setManufactureDate(vehicle.getManufactureDate().atStartOfDay());
            }
            if (vehicle.getWarrantyExpiryDate() != null) {
                info.setWarrantyExpiryDate(vehicle.getWarrantyExpiryDate().atStartOfDay());
            }
            
            // Nếu có variant, trả về info cơ bản
            if (vehicle.getVariant() != null) {
                info.setVariant(VehicleVariantInfo.fromVariant(vehicle.getVariant()));
            }
            
            return info;
        }
    }
    
    // ===== CONVERTER METHOD =====
    
    public static TestDriveWithDetailsDTO fromTestDrive(TestDrive testDrive) {
        TestDriveWithDetailsDTO dto = new TestDriveWithDetailsDTO();
        
        // Copy thông tin TestDrive
        dto.setTestDriveId(testDrive.getTestDriveId());
        dto.setScheduledDate(testDrive.getScheduledDate());
        dto.setStatus(testDrive.getStatus());
        dto.setNotes(testDrive.getNotes());
        dto.setAssignedBy(testDrive.getAssignedBy());
        dto.setCreatedDate(testDrive.getCreatedDate());
        
        // Convert relationships (chỉ khi không null)
        if (testDrive.getDealer() != null) {
            dto.setDealer(DealerInfo.fromDealer(testDrive.getDealer()));
        }
        
        if (testDrive.getCustomer() != null) {
            dto.setCustomer(CustomerInfo.fromCustomer(testDrive.getCustomer()));
        }
        
        if (testDrive.getVehicle() != null) {
            dto.setVehicle(VehicleInfo.fromVehicle(testDrive.getVehicle()));
        }
        
        return dto;
    }
}

