# 📚 DEBT API DOCUMENTATION

## Tổng quan hệ thống quản lý nợ

Hệ thống Debt được thiết kế để quản lý các khoản nợ của khách hàng khi mua xe, hỗ trợ cả **Trả thẳng** và **Trả góp**.

---

## 🔑 Các khái niệm cơ bản

### 1. **Debt (Khoản nợ)**
- Lưu thông tin tổng quan về khoản nợ
- Bao gồm: Tổng tiền, đã trả, còn lại, lãi suất, loại thanh toán

### 2. **DebtSchedule (Lịch trả nợ)**
- Chi tiết các kỳ trả nợ (chỉ có khi trả góp)
- **Tự động sinh** khi tạo Debt với `payment_type = INSTALLMENT`
- Mỗi kỳ bao gồm: Gốc, lãi, tổng phải trả

### 3. **DebtPayment (Lịch sử thanh toán)**
- Ghi lại các lần thanh toán thực tế
- Gắn với kỳ cụ thể (schedule_id)

---

## 🎯 Phân loại thanh toán

### **Payment Type (Loại thanh toán)**

| Giá trị | Tên gọi | Mô tả |
|---------|---------|-------|
| `FULL_PAYMENT` | Trả thẳng | Thanh toán toàn bộ 1 lần, không có lịch trả nợ |
| `INSTALLMENT` | Trả góp | Chia thành nhiều kỳ, có lịch trả nợ tự động |

### **Payment Method (Phương thức thanh toán)**

| Giá trị | Tên gọi |
|---------|---------|
| `CASH` | Tiền mặt |
| `BANK_TRANSFER` | Chuyển khoản |

### **Status (Trạng thái nợ)**

| Giá trị | Mô tả |
|---------|-------|
| `ACTIVE` | Đang hoạt động (còn nợ) |
| `PAID` | Đã trả hết |
| `OVERDUE` | Quá hạn |
| `CANCELLED` | Đã hủy |

---

## 📡 API Endpoints

### Base URL
```
http://localhost:8080/api/debts
```

---

## 1️⃣ TẠO MỚI KHOẢN NỢ

### **POST** `/api/debts`

**Mô tả:** Tạo mới khoản nợ. Nếu `payment_type = INSTALLMENT`, hệ thống **tự động tạo lịch trả nợ** (12 kỳ).

**Request Body:**

```json
{
  "dealer": {
    "dealerId": 1
  },
  "customer": {
    "customerId": 1
  },
  "user": {
    "userId": 4
  },
  "amountDue": 500000000,
  "amountPaid": 0,
  "interestRate": 2.0,
  "startDate": "2024-10-01T00:00:00",
  "dueDate": "2025-10-01T00:00:00",
  "paymentType": "INSTALLMENT",
  "paymentMethod": "BANK_TRANSFER",
  "notes": "Mua xe Honda Civic"
}
```

**Response:**

```json
{
  "success": true,
  "message": "Debt created successfully",
  "data": {
    "debtId": 1,
    "userId": 4,
    "dealerId": 1,
    "customerId": 1,
    "amountDue": 500000000,
    "amountPaid": 0,
    "interestRate": 2.0,
    "startDate": "2024-10-01T00:00:00",
    "dueDate": "2025-10-01T00:00:00",
    "status": "ACTIVE",
    "paymentType": "INSTALLMENT",
    "paymentMethod": "BANK_TRANSFER",
    "notes": "Mua xe Honda Civic",
    "createdDate": "2024-10-01T10:30:00"
  }
}
```

**Quyền:** `ADMIN`, `EVM_STAFF`, `DEALER_STAFF`, `DEALER_MANAGER`

---

## 2️⃣ LẤY DANH SÁCH TẤT CẢ NỢ

### **GET** `/api/debts`

**Mô tả:** Lấy danh sách tất cả các khoản nợ trong hệ thống.

**Response:**

```json
{
  "success": true,
  "message": "All debts retrieved successfully",
  "data": [
    {
      "debtId": 1,
      "userId": 4,
      "dealerId": 1,
      "customerId": 1,
      "amountDue": 500000000,
      "amountPaid": 100000000,
      "status": "ACTIVE",
      "paymentType": "INSTALLMENT",
      "paymentMethod": "BANK_TRANSFER"
    }
  ]
}
```

**Quyền:** `ADMIN`, `EVM_STAFF`

---

## 3️⃣ LẤY CHI TIẾT KHOẢN NỢ

### **GET** `/api/debts/{id}`

**Mô tả:** Lấy thông tin chi tiết của 1 khoản nợ theo ID.

**URL:** `/api/debts/1`

**Response:** Tương tự như khi tạo mới.

**Quyền:** `ADMIN`, `EVM_STAFF`, `DEALER_STAFF`, `DEALER_MANAGER`

---

## 4️⃣ LẤY NỢ THEO DEALER

### **GET** `/api/debts/dealer/{dealerId}`

**Mô tả:** Lấy danh sách tất cả các khoản nợ của 1 dealer.

**URL:** `/api/debts/dealer/1`

**Quyền:** `ADMIN`, `EVM_STAFF`, `DEALER_STAFF`, `DEALER_MANAGER`

---

## 5️⃣ LẤY NỢ THEO KHÁCH HÀNG

### **GET** `/api/debts/customer/{customerId}`

**Mô tả:** Lấy danh sách tất cả các khoản nợ của 1 khách hàng.

**URL:** `/api/debts/customer/1`

**Quyền:** `ADMIN`, `EVM_STAFF`, `DEALER_STAFF`, `DEALER_MANAGER`

---

## 6️⃣ LẤY NỢ THEO TRẠNG THÁI

### **GET** `/api/debts/status/{status}`

**Mô tả:** Lấy danh sách nợ theo trạng thái.

**URL:** `/api/debts/status/ACTIVE`

**Quyền:** `ADMIN`, `EVM_STAFF`

---

## 7️⃣ LẤY NỢ QUÁ HẠN

### **GET** `/api/debts/overdue/{dealerId}`

**Mô tả:** Lấy danh sách các khoản nợ quá hạn của dealer.

**URL:** `/api/debts/overdue/1`

**Quyền:** `ADMIN`, `EVM_STAFF`, `DEALER_STAFF`, `DEALER_MANAGER`

---

## 8️⃣ LẤY LỊCH TRẢ NỢ

### **GET** `/api/debts/{id}/schedules`

**Mô tả:** Lấy danh sách các kỳ trả nợ của 1 khoản nợ.

**URL:** `/api/debts/1/schedules`

**Response:**

```json
{
  "success": true,
  "message": "Debt schedules retrieved successfully",
  "data": [
    {
      "scheduleId": 1,
      "debtId": 1,
      "periodNo": 1,
      "startBalance": 500000000,
      "principal": 40000000,
      "interest": 8333333,
      "installment": 48333333,
      "endBalance": 460000000,
      "dueDate": "2024-11-01",
      "paidAmount": 48333333,
      "status": "PAID"
    },
    {
      "scheduleId": 2,
      "debtId": 1,
      "periodNo": 2,
      "startBalance": 460000000,
      "principal": 40000000,
      "interest": 7666667,
      "installment": 47666667,
      "endBalance": 420000000,
      "dueDate": "2024-12-01",
      "paidAmount": 0,
      "status": "PENDING"
    }
  ]
}
```

**Quyền:** `ADMIN`, `EVM_STAFF`, `DEALER_STAFF`, `DEALER_MANAGER`

---

## 9️⃣ LẤY LỊCH SỬ THANH TOÁN

### **GET** `/api/debts/{id}/payments`

**Mô tả:** Lấy danh sách các lần thanh toán của 1 khoản nợ.

**URL:** `/api/debts/1/payments`

**Response:**

```json
{
  "success": true,
  "message": "Debt payments retrieved successfully",
  "data": [
    {
      "paymentId": 1,
      "debtId": 1,
      "scheduleId": 1,
      "amount": 48333333,
      "paymentDate": "2024-10-25T10:00:00",
      "paymentMethod": "BANK_TRANSFER",
      "referenceNumber": "TXN123456",
      "notes": "Thanh toán kỳ 1",
      "createdBy": "dealerStaff1"
    }
  ]
}
```

**Quyền:** `ADMIN`, `EVM_STAFF`, `DEALER_STAFF`, `DEALER_MANAGER`

---

## 🔟 GHI NHẬN THANH TOÁN

### **POST** `/api/debts/{debtId}/payments`

**Mô tả:** Ghi nhận 1 lần thanh toán cho khoản nợ.

**URL:** `/api/debts/1/payments`

**Request Body:**

```json
{
  "debtSchedule": {
    "scheduleId": 1
  },
  "amount": 48333333,
  "paymentMethod": "BANK_TRANSFER",
  "referenceNumber": "TXN123456",
  "notes": "Thanh toán kỳ 1",
  "createdBy": "dealerStaff1"
}
```

**Response:** Thông tin payment vừa tạo.

**Quyền:** `ADMIN`, `EVM_STAFF`, `DEALER_STAFF`, `DEALER_MANAGER`

---

## 1️⃣1️⃣ CẬP NHẬT TRẠNG THÁI NỢ

### **PUT** `/api/debts/{id}/status`

**Mô tả:** Cập nhật trạng thái của khoản nợ.

**URL:** `/api/debts/1/status?status=PAID&notes=Đã trả hết`

**Query Parameters:**
- `status`: Trạng thái mới (ACTIVE, PAID, OVERDUE, CANCELLED)
- `notes`: Ghi chú (optional)

**Quyền:** `ADMIN`, `EVM_STAFF`, `DEALER_STAFF`, `DEALER_MANAGER`

---

## 1️⃣2️⃣ XÓA KHOẢN NỢ

### **DELETE** `/api/debts/{id}`

**Mô tả:** Xóa khoản nợ khỏi hệ thống.

**URL:** `/api/debts/1`

**Quyền:** `ADMIN`, `EVM_STAFF`

---

## 📊 THỐNG KÊ

### 1️⃣3️⃣ **GET** `/api/debts/dealer/{dealerId}/stats`

**Mô tả:** Lấy thống kê nợ của dealer.

**Response:**

```json
{
  "success": true,
  "message": "Debt statistics retrieved successfully",
  "data": {
    "totalDebt": 1000000000,
    "totalPaid": 200000000,
    "totalRemaining": 800000000,
    "overdueAmount": 100000000,
    "activeDebts": 5,
    "paidDebts": 2,
    "overdueDebts": 1
  }
}
```

### 1️⃣4️⃣ **GET** `/api/debts/dealer/{dealerId}/outstanding`

**Mô tả:** Lấy tổng số tiền nợ chưa thanh toán của dealer.

**Response:**

```json
{
  "success": true,
  "message": "Total outstanding amount retrieved successfully",
  "data": 800000000
}
```

### 1️⃣5️⃣ **GET** `/api/debts/customer/{customerId}/outstanding`

**Mô tả:** Lấy tổng số tiền nợ chưa thanh toán của khách hàng.

### 1️⃣6️⃣ **GET** `/api/debts/overdue-schedules/{dealerId}`

**Mô tả:** Lấy danh sách các kỳ trả nợ bị quá hạn.

---

## 🔄 WORKFLOW

### **Trả góp (INSTALLMENT):**

```
1. Tạo Debt với payment_type = INSTALLMENT
   ↓
2. Hệ thống TỰ ĐỘNG tạo 12 kỳ DebtSchedule
   ↓
3. Khách hàng trả từng kỳ → Tạo DebtPayment
   ↓
4. Hệ thống cập nhật trạng thái từng kỳ
   ↓
5. Khi trả hết → Status = PAID
```

### **Trả thẳng (FULL_PAYMENT):**

```
1. Tạo Debt với payment_type = FULL_PAYMENT
   ↓
2. KHÔNG có DebtSchedule
   ↓
3. Khách hàng trả 1 lần → Tạo DebtPayment
   ↓
4. Status = PAID ngay lập tức
```

---

## ⚙️ Cấu hình tự động

### Lịch trả nợ tự động:
- **Số kỳ:** 12 tháng (mặc định)
- **Công thức:** Annuity (trả đều hàng tháng)
- **Bao gồm:** Gốc + Lãi
- **Lãi suất:** Tính theo tháng

### Công thức:
```
monthly_payment = amount * (r * (1+r)^n) / ((1+r)^n - 1)

Trong đó:
- r = interest_rate / 100 / 12
- n = 12 (số kỳ)
```

---

## 🛠️ Lưu ý kỹ thuật

1. **ID tự sinh:** Tất cả ID đều được database tự động tạo (`IDENTITY`)
2. **Timestamps:** `created_date`, `updated_date` tự động
3. **Validation:** Kiểm tra Dealer, Customer, User tồn tại trước khi tạo
4. **Transaction:** Tất cả thao tác quan trọng đều có `@Transactional`

---

## 🔐 Quyền truy cập

| Endpoint | ADMIN | EVM_STAFF | DEALER_MANAGER | DEALER_STAFF |
|----------|-------|-----------|----------------|--------------|
| Tạo nợ | ✅ | ✅ | ✅ | ✅ |
| Xem tất cả nợ | ✅ | ✅ | ❌ | ❌ |
| Xem nợ theo dealer | ✅ | ✅ | ✅ | ✅ |
| Thanh toán | ✅ | ✅ | ✅ | ✅ |
| Xóa nợ | ✅ | ✅ | ❌ | ❌ |

---

## 📝 Ví dụ thực tế

### Khách hàng mua xe trả góp 500 triệu, lãi suất 2%/tháng:

```json
POST /api/debts
{
  "dealer": {"dealerId": 1},
  "customer": {"customerId": 1},
  "amountDue": 500000000,
  "interestRate": 2.0,
  "paymentType": "INSTALLMENT",
  "paymentMethod": "BANK_TRANSFER"
}
```

**Kết quả:**
- Hệ thống tạo Debt
- Tự động tạo 12 kỳ DebtSchedule
- Mỗi kỳ ~48 triệu (gốc + lãi)

---

## 🆘 Error Codes

| Code | Message | Giải pháp |
|------|---------|-----------|
| 404 | Dealer not found | Kiểm tra dealerId |
| 404 | Customer not found | Kiểm tra customerId |
| 400 | Invalid amount | amountDue phải > 0 |
| 403 | Access denied | Kiểm tra quyền |

---

**Tài liệu cập nhật:** 26/10/2024  
**Phiên bản:** 1.0  
**Contact:** Dev Team

