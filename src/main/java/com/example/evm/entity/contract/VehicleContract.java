package com.example.evm.entity.contract;

import com.example.evm.entity.customer.Customer;
import com.example.evm.entity.dealer.Dealer;
import com.example.evm.entity.order.Order;
import com.example.evm.entity.order.OrderDetail;
import com.example.evm.entity.vehicle.Vehicle;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "VehicleContract")
@Data
@NoArgsConstructor
public class VehicleContract {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "contract_id")
    private Long contractId;

    @Column(name = "contract_number", nullable = false, unique = true, length = 50)
    private String contractNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_detail_id", nullable = false)
    private OrderDetail orderDetail;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dealer_id", nullable = false)
    private Dealer dealer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id", nullable = false)
    private Vehicle vehicle;

    @Column(name = "sale_price", nullable = false)
    private BigDecimal salePrice;

    @Column(name = "payment_method", length = 100)
    private String paymentMethod;

    @Column(name = "contract_date", nullable = false)
    private LocalDate contractDate = LocalDate.now();

    @Column(name = "status", length = 50)
    private String status = "ACTIVE";

    @Column(name = "notes", length = 500)
    private String notes;

    @Column(name = "file_url", length = 500)
    private String fileUrl;
}
