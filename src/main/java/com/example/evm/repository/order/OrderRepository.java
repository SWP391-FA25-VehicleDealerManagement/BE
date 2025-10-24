package com.example.evm.repository.order;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

import com.example.evm.entity.order.Order;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    
    List<Order> findByDealerDealerId(Long dealerId);
    List<Order> findByCustomerCustomerId(Long customerId);
    List<Order> findByStatus(String status);
    
    @Query("SELECT o FROM Order o WHERE o.dealer.dealerId = :dealerId AND o.status = :status")
    List<Order> findByDealerAndStatus(@Param("dealerId") Long dealerId, @Param("status") String status);
    
    @Query("SELECT SUM(o.totalPrice) FROM Order o WHERE o.dealer.dealerId = :dealerId AND o.status = 'DELIVERED'")
    Double getTotalSalesByDealer(@Param("dealerId") Long dealerId);
    
    @Query("SELECT COUNT(o) FROM Order o WHERE o.dealer.dealerId = :dealerId AND o.status = :status")
    Long countByDealerAndStatus(@Param("dealerId") Long dealerId, @Param("status") String status);
}
