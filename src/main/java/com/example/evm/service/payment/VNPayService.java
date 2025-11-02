package com.example.evm.service.payment;

import com.example.evm.config.VNPayConfig;
import com.example.evm.entity.payment.Payment;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class VNPayService {

    @Autowired
    private VNPayConfig vnPayConfig;

    /**
     * Tạo URL thanh toán VNPay
     */
    public String createVNPayUrl(Payment payment) throws Exception {
        String vnp_TmnCode = vnPayConfig.getTmnCode();
        String vnp_HashSecret = vnPayConfig.getHashSecret();
        String vnp_Url = vnPayConfig.getPayUrl();
        String vnp_ReturnUrl = vnPayConfig.getReturnUrl();

        String txnRef = String.valueOf(payment.getPaymentId());
        
        // ✅ FIX: Chuyển đổi số tiền đúng cách
        BigDecimal amountBD = payment.getAmount();
        if (amountBD == null) {
            throw new IllegalArgumentException("Payment amount cannot be null");
        }
        
        // Làm tròn về số nguyên và nhân 100
        long amount = amountBD.longValue() * 100L;
        
        // ✅ Kiểm tra số tiền tối thiểu (10,000 VND = 1,000,000 sau khi nhân 100)
        if (amount < 1000000L) {
            throw new IllegalArgumentException("Payment amount must be at least 10,000 VND");
        }
        
        String orderInfo = "Thanh toan don hang #" + payment.getOrderId();

        Map<String, String> vnp_Params = new HashMap<>();
        vnp_Params.put("vnp_Version", "2.1.0");
        vnp_Params.put("vnp_Command", "pay");
        vnp_Params.put("vnp_TmnCode", vnp_TmnCode);
        vnp_Params.put("vnp_Amount", String.valueOf(amount));
        vnp_Params.put("vnp_CurrCode", "VND");
        vnp_Params.put("vnp_TxnRef", txnRef);
        vnp_Params.put("vnp_OrderInfo", orderInfo);
        vnp_Params.put("vnp_OrderType", "other");
        vnp_Params.put("vnp_Locale", "vn");
        vnp_Params.put("vnp_ReturnUrl", vnp_ReturnUrl);
        vnp_Params.put("vnp_IpAddr", "127.0.0.1");

        // Thời gian tạo và hết hạn
        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        String createDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_CreateDate", createDate);

        cld.add(Calendar.MINUTE, 15);
        String expireDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_ExpireDate", expireDate);

        // Sắp xếp params theo tên
        List<String> fieldNames = new ArrayList<>(vnp_Params.keySet());
        Collections.sort(fieldNames);

        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();
        Iterator<String> itr = fieldNames.iterator();

        while (itr.hasNext()) {
            String fieldName = itr.next();
            String fieldValue = vnp_Params.get(fieldName);
            if ((fieldValue != null) && (fieldValue.length() > 0)) {
                // Build hash data
                hashData.append(fieldName).append('=').append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));
                query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII))
                     .append('=')
                     .append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));
if (itr.hasNext()) {
                    hashData.append('&');
                    query.append('&');
                }
            }
        }

        String vnp_SecureHash = hmacSHA512(vnp_HashSecret, hashData.toString());
        query.append("&vnp_SecureHash=").append(vnp_SecureHash);
        return vnp_Url + "?" + query.toString();
    }

    /**
     * Xác minh phản hồi từ VNPay
     */
public boolean validateVNPayResponse(Map<String, String> params) throws Exception {
    
    String vnp_HashSecret = vnPayConfig.getHashSecret();
    String vnp_SecureHash = params.get("vnp_SecureHash");
 

    if (vnp_SecureHash == null || vnp_SecureHash.trim().isEmpty()) {
        System.out.println("❌ NO SECURE HASH");
        return false;
    }

    // ❌ REMOVE 2 FIELDS
    Map<String, String> signedData = new HashMap<>(params);
    signedData.remove("vnp_SecureHash");
    signedData.remove("vnp_SecureHashType");

    // ✅ SORT KEYS
    List<String> fieldNames = new ArrayList<>(signedData.keySet());
    Collections.sort(fieldNames);

    // 🔥 FIX CUỐI: DÙNG URL ENCODE NHƯ CREATE URL
    StringBuilder hashData = new StringBuilder();
    for (int i = 0; i < fieldNames.size(); i++) {
        String fieldName = fieldNames.get(i);
        String fieldValue = signedData.get(fieldName);
        
        if (fieldValue != null && !fieldValue.trim().isEmpty()) {
            // ✅ URL ENCODE GIỐNG createVNPayUrl
            hashData.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII))
                    .append('=')
                    .append(URLEncoder.encode(fieldValue.trim(), StandardCharsets.US_ASCII));
            
            if (i < fieldNames.size() - 1) {
                hashData.append('&');
            }
        }
    }

    System.out.println("HASH_DATA: " + hashData.toString());
    
    String calculatedHash = hmacSHA512(vnp_HashSecret, hashData.toString());
    System.out.println("CALCULATED: " + calculatedHash);
    System.out.println("EXPECTED:   " + vnp_SecureHash);
    
    boolean isValid = calculatedHash.equalsIgnoreCase(vnp_SecureHash);
    System.out.println("VALID: " + isValid);
    
    return isValid;
}

private String hmacSHA512(String key, String data) throws Exception {
    try {
        Mac hmac = Mac.getInstance("HmacSHA512");
        SecretKeySpec secretKey = new SecretKeySpec(
            key.getBytes(StandardCharsets.UTF_8), 
            "HmacSHA512"
        );
        hmac.init(secretKey);
        
        byte[] bytes = hmac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        
        // ✅ FIX 7: Đảm bảo lowercase hex
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b & 0xff));
        }
        return sb.toString().toLowerCase();
    } catch (Exception e) {
        throw new Exception("HMAC SHA512 error: " + e.getMessage(), e);
    }
}
}
