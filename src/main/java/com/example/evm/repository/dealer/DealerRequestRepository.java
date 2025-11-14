package com.example.evm.repository.dealer;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.evm.entity.dealer.DealerRequest;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface DealerRequestRepository extends JpaRepository<DealerRequest, Long> {
    List<DealerRequest> findByDealerDealerId(Long dealerId);

    List<DealerRequest> findByCreatedByUserId(Long userId);

    List<DealerRequest> findByStatus(String status);

    List<DealerRequest> findByDealerDealerIdAndStatus(Long dealerId, String status);

    List<DealerRequest> findByDealerDealerIdAndPriority(Long dealerId, String priority);

    @Query("SELECT dr FROM DealerRequest dr WHERE dr.dealer.dealerId = :dealerId AND dr.requestDate BETWEEN :startDate AND :endDate")
    List<DealerRequest> findByDealerAndDateRange(@Param("dealerId") Long dealerId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    @Query("SELECT dr FROM DealerRequest dr WHERE dr.status = 'PENDING' ORDER BY dr.requestDate ASC")
    List<DealerRequest> findPendingRequests();

    @Query("SELECT dr FROM DealerRequest dr WHERE dr.status = 'APPROVED' AND dr.requiredDate <= :dueDate ORDER BY dr.requiredDate ASC")
    List<DealerRequest> findDueRequests(@Param("dueDate") LocalDateTime dueDate);

    @Query("SELECT COUNT(dr) FROM DealerRequest dr WHERE dr.dealer.dealerId = :dealerId AND dr.status = :status")
    Long countByDealerAndStatus(@Param("dealerId") Long dealerId, @Param("status") String status);

    @Query("SELECT SUM(dr.totalAmount) FROM DealerRequest dr WHERE dr.dealer.dealerId = :dealerId AND dr.status = 'DELIVERED'")
    Double getTotalSpentByDealer(@Param("dealerId") Long dealerId);

    @Query("SELECT dr.status, COUNT(dr) FROM DealerRequest dr WHERE dr.dealer.dealerId = :dealerId GROUP BY dr.status")
    List<Object[]> getRequestStatsByDealer(@Param("dealerId") Long dealerId);

}
