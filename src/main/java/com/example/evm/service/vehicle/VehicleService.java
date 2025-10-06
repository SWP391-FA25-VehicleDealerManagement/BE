package com.example.evm.service.vehicle;

<<<<<<< HEAD
import com.example.evm.dto.vehicle.VehicleComparisonDTO;
import com.example.evm.dto.vehicle.VehicleResponse;
=======
import com.example.evm.dto.vehicle.VehicleRequest;
import com.example.evm.dto.vehicle.VehicleResponse;

>>>>>>> 54ac894e9c24c5857ad6736606c5e3f39b001e8d
import java.util.List;

public interface VehicleService {
    List<VehicleResponse> getAllVehicles();
<<<<<<< HEAD
    List<VehicleComparisonDTO> compareVariants(List<Long> variantIds);
=======

    VehicleResponse addVehicle(VehicleRequest request);

    VehicleResponse updateVehicle(Integer id, VehicleRequest request);

    void deleteVehicle(Integer id);
>>>>>>> 54ac894e9c24c5857ad6736606c5e3f39b001e8d
}
