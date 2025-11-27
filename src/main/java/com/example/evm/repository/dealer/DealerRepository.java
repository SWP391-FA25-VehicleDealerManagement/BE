package com.example.evm.repository.dealer;

import com.example.evm.entity.dealer.Dealer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DealerRepository extends JpaRepository<Dealer, Long> {

    // 🔹 Tìm dealer theo tên (bất kể trạng thái)
    Optional<Dealer> findByDealerName(String dealerName);

    // 🔹 Chỉ lấy các dealer đang hoạt động, sắp xếp theo ID
    @Query("SELECT d FROM Dealer d WHERE d.status = 'ACTIVE' ORDER BY d.dealerId")
    List<Dealer> findAllActiveDealers();

    // 🔹 Lấy tất cả dealer đã bị vô hiệu hoá
    @Query("SELECT d FROM Dealer d WHERE d.status = 'INACTIVE' ORDER BY d.dealerId")
    List<Dealer> findAllInactiveDealers();

    //  Tìm ID lớn nhất hiện tại
    @Query("SELECT MAX(d.dealerId) FROM Dealer d WHERE d.status = 'ACTIVE'")
    Optional<Long> findMaxActiveId();

    //  Tìm tất cả dealer có ID > deletedId để shift xuống
    @Query("SELECT d FROM Dealer d WHERE d.status = 'ACTIVE' AND d.dealerId > :deletedId ORDER BY d.dealerId")
    List<Dealer> findDealersToShift(@Param("deletedId") Long deletedId);

    // 🔹 Cập nhật trạng thái dealer (xóa mềm)
    @Modifying
    @Query("UPDATE Dealer d SET d.status = :status WHERE d.dealerId = :id")
    void updateStatus(Long id, String status);
}