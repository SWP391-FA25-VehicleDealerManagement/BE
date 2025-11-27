package com.example.evm.service.dealer;

import com.example.evm.dto.auth.DealerInfo;
import com.example.evm.dto.dealer.CreateDealerAccountRequest;
import com.example.evm.dto.dealer.CreateDealerAccountResponse;
import com.example.evm.entity.dealer.Dealer;
import com.example.evm.entity.user.User;
import com.example.evm.repository.auth.UserRepository;
import com.example.evm.repository.dealer.DealerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class DealerAccountService {

    private static final Logger log = LoggerFactory.getLogger(DealerAccountService.class);

    private final UserRepository userRepository;
    private final DealerRepository dealerRepository;
    private final PasswordEncoder passwordEncoder;

    public DealerAccountService(UserRepository userRepository, 
                               DealerRepository dealerRepository,
                               PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.dealerRepository = dealerRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Tạo tài khoản DEALER_MANAGER cho dealer
     *  Chỉ được tạo 1 DEALER_MANAGER cho mỗi dealer
     *  ADMIN và EVM_STAFF có thể tạo
     */
    @Transactional
    public CreateDealerAccountResponse createDealerAccount(CreateDealerAccountRequest request) {
        CreateDealerAccountResponse response = new CreateDealerAccountResponse();

        try {
            // 1. Kiểm tra dealer có tồn tại không
            Dealer dealer = dealerRepository.findById(request.getDealerId())
                    .orElseThrow(() -> new IllegalArgumentException("Dealer not found with ID: " + request.getDealerId()));

            //  2. Kiểm tra dealer đã có DEALER_MANAGER chưa
            long managerCount = userRepository.countByDealerIdAndRole(request.getDealerId(), "DEALER_MANAGER");
            if (managerCount > 0) {
                response.setSuccess(false);
                response.setMessage("Dealer already has a DEALER_MANAGER account. Each dealer can only have one manager.");
                return response;
            }

            // 3. Kiểm tra username đã tồn tại chưa
            if (userRepository.findByUserName(request.getUsername()).isPresent()) {
                response.setSuccess(false);
                response.setMessage("Username already exists: " + request.getUsername());
                return response;
            }

            // 4. Tạo User account với role DEALER_MANAGER (không có prefix ROLE_)
            User user = new User();
            user.setUserName(request.getUsername());
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            user.setEmail(request.getEmail());
            user.setRole("DEALER_MANAGER"); //  Không có prefix ROLE_
            user.setDealer(dealer);
            user.setCreatedDate(LocalDateTime.now());

            User savedUser = userRepository.save(user);
            log.info(" Created DEALER_MANAGER account - username: {}, dealerId: {}", 
                    savedUser.getUserName(), dealer.getDealerId());

            // 5. Tạo DealerInfo (bao gồm createdBy và createdDate)
            DealerInfo dealerInfo = new DealerInfo();
            dealerInfo.setDealerId(dealer.getDealerId());
            dealerInfo.setDealerName(dealer.getDealerName());
            dealerInfo.setPhone(dealer.getPhone());
            dealerInfo.setAddress(dealer.getAddress());
            dealerInfo.setCreatedBy(dealer.getCreatedBy()); //  Có createdBy
            dealerInfo.setCreatedDate(dealer.getCreatedDate()); //  Có createdDate

            // 6. Tạo response
            response.setSuccess(true);
            response.setMessage("Dealer manager account created successfully");
            response.setUserId(savedUser.getUserId());
            response.setUsername(savedUser.getUserName());
            response.setRole(savedUser.getRole());
            response.setUserCreatedDate(savedUser.getCreatedDate()); //  Có userCreatedDate
            response.setDealerInfo(dealerInfo);

            return response;

        } catch (IllegalArgumentException e) {
            log.error("Validation error: {}", e.getMessage());
            response.setSuccess(false);
            response.setMessage(e.getMessage());
            return response;

        } catch (Exception e) {
            log.error("Failed to create dealer account", e);
            response.setSuccess(false);
            response.setMessage("Failed to create dealer account: " + e.getMessage());
            return response;
        }
    }
}