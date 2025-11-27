package com.example.evm.service.contract;

import com.example.evm.dto.contract.VehicleContractRequest;
import com.example.evm.dto.contract.VehicleContractResponse;
import com.example.evm.entity.contract.VehicleContract;

import java.util.List;

public interface VehicleContractService {

    /**
     *  Tạo hợp đồng mua bán xe mới giữa khách hàng và đại lý.
     */
    VehicleContractResponse createContract(VehicleContractRequest request);

    /**
     *  Lấy danh sách tất cả hợp đồng mua bán xe.
     */
    List<VehicleContractResponse> getAllContracts();

    /**
     *  Lấy chi tiết 1 hợp đồng mua bán xe theo ID.
     */
    VehicleContractResponse getContractById(Long id);

    /**
     * Lấy tất cả hợp đồng theo dealer 
     */
    List<VehicleContractResponse> getContractsByDealerId(Long dealerId);

    /**
     * 📄 Lấy entity gốc của hợp đồng (phục vụ khi sinh file Word hoặc truy xuất dữ liệu nội bộ).
     */
    VehicleContract getContractEntityById(Long id);

    /**
     * THÊM MỚI: Ký hợp đồng.
     * Chuyển status từ "DRAFT" sang "SIGNED".
     */
    VehicleContractResponse signContract(Long id);

    /**
     * THÊM MỚI: Xóa hợp đồng.
     * Chỉ xóa vĩnh viễn (hard delete) nếu status là "DRAFT".
     */
    void deleteDraftContract(Long id);
}
