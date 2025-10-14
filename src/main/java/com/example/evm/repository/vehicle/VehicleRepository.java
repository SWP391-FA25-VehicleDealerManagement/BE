package com.example.evm.repository.vehicle;

import com.example.evm.entity.vehicle.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface VehicleRepository extends JpaRepository<Vehicle, Long> {

    // 🔹 Lấy tất cả xe ACTIVE
    List<Vehicle> findByStatusIgnoreCase(String status);

    // 🏢 Lấy tất cả xe ACTIVE theo Dealer ID
    List<Vehicle> findByDealer_DealerIdAndStatusIgnoreCase(Long dealerId, String status);

    // 🔹 Hoặc có thể giữ thêm query riêng cho rõ ràng
    @Query("SELECT v FROM Vehicle v WHERE v.status = 'ACTIVE'")
    List<Vehicle> findAllActiveVehicles();

    @Query("SELECT v FROM Vehicle v WHERE v.status = 'INACTIVE'")
    List<Vehicle> findAllInactiveVehicles();

    // 🔍 Tìm theo tên (bất kể hoa thường)
    @Query("SELECT v FROM Vehicle v WHERE LOWER(v.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<Vehicle> searchByName(@Param("name") String name);

    // 🔍 Nếu bạn muốn tìm trong cả ACTIVE vehicles thôi:
    @Query("SELECT v FROM Vehicle v WHERE v.status = 'ACTIVE' AND LOWER(v.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<Vehicle> searchActiveByName(@Param("name") String name);
}
