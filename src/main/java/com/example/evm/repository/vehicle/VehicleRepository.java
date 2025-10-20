package com.example.evm.repository.vehicle;

import com.example.evm.entity.vehicle.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface VehicleRepository extends JpaRepository<Vehicle, Long> {

    String NOT_SOLD_CONDITION = "v.vehicleId NOT IN (SELECT od.vehicle.vehicleId FROM OrderDetail od)";

    // 🟢 Lấy tất cả xe ACTIVE (ĐANG TỒN KHO)
    // Active = (Status='IN STOCK') VÀ (Chưa Bán)
    @Query("SELECT v FROM Vehicle v JOIN v.stock s " +
           "WHERE s.status IN ('IN STOCK') AND " + NOT_SOLD_CONDITION)
    List<Vehicle> findAvailableVehicles();

    // 🔴 Lấy tất cả xe INACTIVE (NGỪNG BÁN)
    // Inactive = (Status KHÔNG phải 'IN STOCK') VÀ (Chưa Bán)
    @Query("SELECT v FROM Vehicle v JOIN v.stock s " +
           "WHERE s.status NOT IN ('IN STOCK') AND " + NOT_SOLD_CONDITION)
    List<Vehicle> findInactiveVehicles();


    // 🏢 Lấy tất cả xe ACTIVE theo Dealer ID
    @Query("SELECT v FROM Vehicle v JOIN v.stock s " +
           "WHERE s.dealer.dealerId = :dealerId AND s.status IN ('IN STOCK') AND " + NOT_SOLD_CONDITION)
    List<Vehicle> findAvailableVehiclesByDealerId(@Param("dealerId") Long dealerId);


    // 🔍 Tìm theo tên Model hoặc Variant (Bỏ qua Active/Inactive)
    @Query("SELECT v FROM Vehicle v JOIN v.stock s JOIN s.variant va JOIN va.model m " +
           "WHERE (LOWER(va.name) LIKE LOWER(CONCAT('%', :name, '%')) OR LOWER(m.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND " + NOT_SOLD_CONDITION)
    List<Vehicle> searchAvailableByModelOrVariantName(@Param("name") String name);


    // 🔍 Tìm theo tên VÀ chỉ lấy xe ACTIVE (Tồn kho)
    @Query("SELECT v FROM Vehicle v JOIN v.stock s JOIN s.variant va JOIN va.model m " +
           "WHERE s.status IN ('IN STOCK') AND " + 
           "(LOWER(va.name) LIKE LOWER(CONCAT('%', :name, '%')) OR LOWER(m.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND " + NOT_SOLD_CONDITION)
    List<Vehicle> searchActiveByModelOrVariantName(@Param("name") String name);
}