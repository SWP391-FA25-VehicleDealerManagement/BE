package com.example.evm.service.dealer;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.example.evm.entity.dealer.DealerRequest;
import com.example.evm.entity.dealer.Dealer;
import com.example.evm.entity.user.User;
import com.example.evm.entity.vehicle.VehicleVariant;
import com.example.evm.exception.ResourceNotFoundException;

import com.example.evm.repository.dealer.DealerRequestRepository;
import com.example.evm.entity.dealer.DealerRequestDetail;

import com.example.evm.repository.dealer.DealerRepository;
import com.example.evm.repository.auth.UserRepository;
import com.example.evm.repository.vehicle.VehicleVariantRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class DealerRequestService {
    private final DealerRequestRepository dealerRequestRepository;  
    private final DealerRepository dealerRepository;
    private final UserRepository userRepository;
    private final VehicleVariantRepository vehicleVariantRepository;

    public List<DealerRequest> getAllRequests() {
        return dealerRequestRepository.findAll();
    }

    public List<DealerRequest> getRequestsByDealer(Long dealerId) {
        return dealerRequestRepository.findByDealerDealerId(dealerId);
    }

    public List<DealerRequest> getRequestsByUser(Long userId) {
        return dealerRequestRepository.findByCreatedByUserId(userId);
    }

    public List<DealerRequest> getRequestsByStatus(String status) {
        return dealerRequestRepository.findByStatus(status);
    }

    public List<DealerRequest> getPendingRequests() {
        return dealerRequestRepository.findPendingRequests();
    }

    public DealerRequest getRequestById(Long id) {
        return dealerRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Dealer request not found with id: " + id));
    }

    public List<DealerRequestDetail> getRequestDetails(Long requestId) {
        DealerRequest request = getRequestById(requestId);
        return request.getRequestDetails();
    }

    @Transactional
    public DealerRequest createRequest(DealerRequest request) {
        // Validate entities
        Dealer dealer = dealerRepository.findById(request.getDealer().getDealerId())
                .orElseThrow(() -> new ResourceNotFoundException("Dealer not found"));
        
        User user = userRepository.findById(request.getCreatedBy().getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        request.setDealer(dealer);
        request.setCreatedBy(user);
        request.setRequestDate(LocalDateTime.now());

        // ✅ Process request details and calculate total using BigDecimal
        BigDecimal totalAmount = BigDecimal.ZERO;
        
        for (DealerRequestDetail detail : request.getRequestDetails()) {
            VehicleVariant variant = vehicleVariantRepository.findById(detail.getVehicleVariant().getVariantId())
                    .orElseThrow(() -> new ResourceNotFoundException("Vehicle variant not found"));
            
            detail.setVehicleVariant(variant);
            detail.setDealerRequest(request);
            
            // ✅ Calculate line total using BigDecimal
            BigDecimal lineTotal = detail.getUnitPrice()
                    .multiply(BigDecimal.valueOf(detail.getQuantity()));
            totalAmount = totalAmount.add(lineTotal);
        }

        // ✅ Set total amount
        request.setTotalAmount(totalAmount);
        DealerRequest savedRequest = dealerRequestRepository.save(request);
        
        log.info("Dealer request created: ID {} - Dealer: {} - Total: {}", 
                savedRequest.getRequestId(), dealer.getDealerName(), totalAmount);
        
        return savedRequest;
    }

    @Transactional
    public DealerRequest updateRequestStatus(Long id, String status, String approvedBy) {
        DealerRequest request = getRequestById(id);
        request.setStatus(status);
        
        if ("APPROVED".equals(status)) {
            request.setApprovedDate(LocalDateTime.now());
            request.setApprovedBy(approvedBy);
        } else if ("DELIVERED".equals(status)) {
            request.setDeliveryDate(LocalDateTime.now());
        }
        
        DealerRequest updatedRequest = dealerRequestRepository.save(request);
        log.info("Dealer request {} status updated to: {}", id, status);
        
        return updatedRequest;
    }

    @Transactional
    public DealerRequest updateRequest(DealerRequest requestDetails) {
        DealerRequest request = getRequestById(requestDetails.getRequestId());
        
        if (requestDetails.getRequiredDate() != null) {
            request.setRequiredDate(requestDetails.getRequiredDate());
        }
        if (requestDetails.getPriority() != null) {
            request.setPriority(requestDetails.getPriority());
        }
        if (requestDetails.getNotes() != null) {
            request.setNotes(requestDetails.getNotes());
        }
        
        return dealerRequestRepository.save(request);
    }

    public Long countRequestsByDealerAndStatus(Long dealerId, String status) {
        return dealerRequestRepository.countByDealerAndStatus(dealerId, status);
    }

    public Double getTotalSpentByDealer(Long dealerId) {
        Double total = dealerRequestRepository.getTotalSpentByDealer(dealerId);
        return total != null ? total : 0.0;
    }

    public Map<String, Object> getRequestStats(Long dealerId) {
        Map<String, Object> stats = new HashMap<>();
        
        List<Object[]> statusCounts = dealerRequestRepository.getRequestStatsByDealer(dealerId);
        Map<String, Long> statusMap = new HashMap<>();
        
        for (Object[] data : statusCounts) {
            statusMap.put((String) data[0], (Long) data[1]);
        }
        
        stats.put("byStatus", statusMap);
        stats.put("totalRequests", dealerRequestRepository.countByDealerAndStatus(dealerId, "APPROVED"));
        stats.put("totalSpent", getTotalSpentByDealer(dealerId));
        
        return stats;
    }

    @Transactional
    public void deleteRequest(Long id) {
        DealerRequest request = getRequestById(id);
        
        // ✅ KHÔNG xóa VehicleVariant! 
        // Request details sẽ tự động xóa bởi CascadeType.ALL trong DealerRequest entity
        dealerRequestRepository.delete(request);
        
        log.info("Dealer request deleted: {} with {} details", id, request.getRequestDetails().size());
    }
}
