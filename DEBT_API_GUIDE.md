# 🏦 **DEBT API GUIDE - HƯỚNG DẪN SỬ DỤNG API NỢ**

## 📋 **MỤC LỤC**
1. [Tổng quan hệ thống](#tổng-quan-hệ-thống)
2. [Authentication](#authentication)
3. [API Endpoints](#api-endpoints)
4. [Workflow chi tiết](#workflow-chi-tiết)
5. [Ví dụ thực tế](#ví-dụ-thực-tế)
6. [Troubleshooting](#troubleshooting)

---

## 🎯 **TỔNG QUAN HỆ THỐNG**

### **2 Loại Nợ:**
- **DEALER_DEBT**: Dealer nợ hãng xe (từ DealerRequest)
- **CUSTOMER_DEBT**: Customer nợ dealer (từ Payment INSTALLMENT)

### **2 Cách Thanh Toán:**
- **Payment + Confirm**: Cần EVM xác nhận
- **Direct Pay**: Thanh toán trực tiếp, không cần xác nhận

---

## 🔐 **AUTHENTICATION**

### **1. Login để lấy JWT Token:**
```http
POST http://localhost:8080/api/auth/login
Content-Type: application/json

{
    "username": "evmStaff",
    "password": "123456123456"
}
```

**Response:**
```json
{
    "success": true,
    "message": "Login successful",
    "data": {
        "token": "eyJhbGciOiJIUzI1NiJ9...",
        "user": {
            "userId": 1,
            "username": "evmStaff",
            "role": "EVM_STAFF"
        }
    }
}
```

### **2. Sử dụng Token:**
```http
Authorization: Bearer <your_jwt_token>
```

---

## 🚀 **API ENDPOINTS**

### **1. QUẢN LÝ DEBT**

#### **1.1 Lấy danh sách tất cả debt:**
```http
GET http://localhost:8080/api/debts
Authorization: Bearer <token>
```

#### **1.2 Lấy danh sách DEALER_DEBT:**
```http
GET http://localhost:8080/api/debts/dealer-debts
Authorization: Bearer <token>
```

#### **1.3 Lấy danh sách CUSTOMER_DEBT:**
```http
GET http://localhost:8080/api/debts/customer-debts/{dealerId}
Authorization: Bearer <token>
```

#### **1.4 Lấy thông tin debt theo ID:**
```http
GET http://localhost:8080/api/debts/{debtId}
Authorization: Bearer <token>
```

#### **1.5 Cập nhật trạng thái debt:**
```http
PUT http://localhost:8080/api/debts/{debtId}/update-status
Authorization: Bearer <token>
```

### **2. QUẢN LÝ DEBT SCHEDULE**

#### **2.1 Lấy danh sách lịch trả nợ:**
```http
GET http://localhost:8080/api/debts/{debtId}/schedules
Authorization: Bearer <token>
```

#### **2.2 Lấy chi tiết lịch trả nợ:**
```http
GET http://localhost:8080/api/debts/{debtId}/schedule-details
Authorization: Bearer <token>
```

#### **2.3 Thanh toán trực tiếp lịch trả nợ:**
```http
POST http://localhost:8080/api/debts/schedules/{scheduleId}/direct-pay?amount={amount}
Authorization: Bearer <token>
```

### **3. QUẢN LÝ DEBT PAYMENT**

#### **3.1 Tạo payment cho debt:**
```http
POST http://localhost:8080/api/debts/{debtId}/payments
Authorization: Bearer <token>
Content-Type: application/json

{
    "amount": 450000000,
    "paymentMethod": "BANK_TRANSFER",
    "scheduleId": 1,
    "referenceNumber": "PAY-001",
    "notes": "Payment for debt",
    "createdBy": "EVM_Staff"
}
```

#### **3.2 Xác nhận payment:**
```http
PUT http://localhost:8080/api/debts/{debtId}/payments/{paymentId}/confirm?confirmedBy=EVM_Staff
Authorization: Bearer <token>
```

#### **3.3 Từ chối payment:**
```http
PUT http://localhost:8080/api/debts/{debtId}/payments/{paymentId}/reject?rejectedBy=EVM_Staff&reason=Invalid payment
Authorization: Bearer <token>
```

#### **3.4 Lấy danh sách payments của debt:**
```http
GET http://localhost:8080/api/debts/{debtId}/payments
Authorization: Bearer <token>
```

### **4. TẠO DEBT TỪ PAYMENT**

#### **4.1 Tạo CUSTOMER_DEBT từ Payment:**
```http
POST http://localhost:8080/api/debts/create-from-payment/{paymentId}
Authorization: Bearer <token>
```

---

## 🔄 **WORKFLOW CHI TIẾT**

### **A. DEALER_DEBT WORKFLOW**

#### **Bước 1: Tạo Order (Dealer)**
```http
POST http://localhost:8080/api/orders
Authorization: Bearer <token>
Content-Type: application/json

{
    "dealerId": 1,
    "totalAmount": 1000000000,
    "status": "CONFIRMED"
}
```

#### **Bước 2: Tạo DealerRequest**
```http
POST http://localhost:8080/api/dealer-requests
Authorization: Bearer <token>
Content-Type: application/json

{
    "dealerId": 1,
    "orderId": <order_id_from_step_1>,
    "priority": "HIGH",
    "notes": "Request for vehicles"
}
```

#### **Bước 3: Cập nhật status thành DELIVERED**
```http
PUT http://localhost:8080/api/dealer-requests/{requestId}/status
Authorization: Bearer <token>
Content-Type: application/json

{
    "status": "DELIVERED"
}
```

#### **Bước 4: Tự động tạo DEALER_DEBT + DebtSchedule**

### **B. CUSTOMER_DEBT WORKFLOW**

#### **Bước 1: Tạo Order (Dealer cho Customer)**
```http
POST http://localhost:8080/api/orders
Authorization: Bearer <token>
Content-Type: application/json

{
    "dealerId": 1,
    "customerId": 1,
    "totalAmount": 1000000000,
    "status": "CONFIRMED"
}
```

#### **Bước 2: Tạo Payment INSTALLMENT**
```http
POST http://localhost:8080/api/payments
Authorization: Bearer <token>
Content-Type: application/json

{
    "orderId": <order_id_from_step_1>,
    "amount": 1000000000,
    "paymentMethod": "BANK_TRANSFER",
    "paymentType": "INSTALLMENT",
    "status": "COMPLETED"
}
```

#### **Bước 3: Tự động tạo CUSTOMER_DEBT + DebtSchedule**

### **C. THANH TOÁN DEBT**

#### **Cách 1: Payment + Confirm (Cần EVM xác nhận)**
1. Tạo payment → Status: PENDING
2. EVM xác nhận → Status: CONFIRMED
3. Tự động cập nhật Debt + DebtSchedule

#### **Cách 2: Direct Pay (Không cần xác nhận)**
1. Gọi API direct-pay
2. Tự động cập nhật Debt + DebtSchedule

---

## 💡 **VÍ DỤ THỰC TẾ**

### **Ví dụ 1: Tạo DEALER_DEBT**

```bash
# 1. Login
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"evmStaff","password":"password123"}'

# 2. Tạo Order
curl -X POST http://localhost:8080/api/orders \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{"dealerId":1,"totalAmount":1000000000,"status":"CONFIRMED"}'

# 3. Tạo DealerRequest
curl -X POST http://localhost:8080/api/dealer-requests \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{"dealerId":1,"orderId":1,"priority":"HIGH","notes":"Request for vehicles"}'

# 4. Cập nhật status DELIVERED
curl -X PUT http://localhost:8080/api/dealer-requests/1/status \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{"status":"DELIVERED"}'

# 5. Kiểm tra DEALER_DEBT được tạo
curl -X GET http://localhost:8080/api/debts/dealer-debts \
  -H "Authorization: Bearer <token>"
```

### **Ví dụ 2: Thanh toán Direct Pay**

```bash
# 1. Lấy danh sách debt schedules
curl -X GET http://localhost:8080/api/debts/1/schedules \
  -H "Authorization: Bearer <token>"

# 2. Thanh toán trực tiếp schedule đầu tiên
curl -X POST "http://localhost:8080/api/debts/schedules/1/direct-pay?amount=450000000" \
  -H "Authorization: Bearer <token>"

# 3. Kiểm tra debt đã được cập nhật
curl -X GET http://localhost:8080/api/debts/1 \
  -H "Authorization: Bearer <token>"
```

### **Ví dụ 3: Payment + Confirm**

```bash
# 1. Tạo payment
curl -X POST http://localhost:8080/api/debts/1/payments \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{"amount":450000000,"paymentMethod":"BANK_TRANSFER","scheduleId":1,"referenceNumber":"PAY-001","notes":"Payment for debt","createdBy":"EVM_Staff"}'

# 2. Xác nhận payment
curl -X PUT "http://localhost:8080/api/debts/1/payments/1/confirm?confirmedBy=EVM_Staff" \
  -H "Authorization: Bearer <token>"

# 3. Kiểm tra payment status
curl -X GET http://localhost:8080/api/debts/1/payments \
  -H "Authorization: Bearer <token>"
```

---

## 🔧 **TROUBLESHOOTING**

### **Lỗi thường gặp:**

#### **1. 401 Unauthorized**
```
Error: No JWT token supplied
Solution: Thêm Authorization header với Bearer token
```

#### **2. 403 Forbidden**
```
Error: Access denied
Solution: Kiểm tra quyền user (ADMIN, EVM_STAFF, DEALER_STAFF, DEALER_MANAGER)
```

#### **3. 404 Not Found**
```
Error: Payment not found with id: 
Solution: Kiểm tra Payment ID có tồn tại trong database
```

#### **4. 400 Bad Request**
```
Error: Only INSTALLMENT payments can create debt
Solution: Đảm bảo payment_type = "INSTALLMENT"
```



## 🎯 **QUICK REFERENCE**

| API | Method | Endpoint | Auth Required |
|-----|--------|----------|---------------|
| Get all debts | GET | `/api/debts` | ✅ |
| Get dealer debts | GET | `/api/debts/dealer-debts` | ✅ |
| Get customer debts | GET | `/api/debts/customer-debts/{dealerId}` | ✅ |
| Get debt by ID | GET | `/api/debts/{debtId}` | ✅ |
| Update debt status | PUT | `/api/debts/{debtId}/update-status` | ✅ |
| Get debt schedules | GET | `/api/debts/{debtId}/schedules` | ✅ |
| Get schedule details | GET | `/api/debts/{debtId}/schedule-details` | ✅ |
| Direct pay schedule | POST | `/api/debts/schedules/{scheduleId}/direct-pay` | ✅ |
| Create payment | POST | `/api/debts/{debtId}/payments` | ✅ |
| Confirm payment | PUT | `/api/debts/{debtId}/payments/{paymentId}/confirm` | ✅ |
| Reject payment | PUT | `/api/debts/{debtId}/payments/{paymentId}/reject` | ✅ |
| Get debt payments | GET | `/api/debts/{debtId}/payments` | ✅ |
| Create debt from payment | POST | `/api/debts/create-from-payment/{paymentId}` | ✅ |

---

## 🚀 **GETTING STARTED**

1. **Start server**: `mvn spring-boot:run`
2. **Login**: Lấy JWT token
3. **Test basic APIs**: Get debts, schedules
4. **Create test data**: Order, Payment, DealerRequest
5. **Test workflows**: DEALER_DEBT, CUSTOMER_DEBT
6. **Test payments**: Direct pay, Payment + Confirm

**Happy coding! 🎉**
