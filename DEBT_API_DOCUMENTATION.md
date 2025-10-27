# �� DEBT & PAYMENT API DOCUMENTATION

## 🎯 Tổng quan

Hệ thống quản lý nợ (Debt Management) cho phép:
- **Hãng xe (EVM)** theo dõi nợ của **Dealer** (dealer nợ hãng)
- **Dealer** theo dõi nợ của **Customer** (customer nợ dealer)
- Thanh toán nợ độc lập với đơn hàng (không cần `order_id`)

---

## 📊 Kiến trúc

### Bảng dữ liệu

1. **`Debt`** - Khoản nợ
   - `debt_id`: ID
   - `debt_type`: `DEALER_DEBT` (dealer nợ hãng) hoặc `CUSTOMER_DEBT` (customer nợ dealer)
   - `amount_due`: Tổng số tiền nợ
   - `amount_paid`: Số tiền đã trả
   - `payment_type`: `FULL_PAYMENT` (trả thẳng) hoặc `INSTALLMENT` (trả góp 12 tháng)
   - `payment_method`: `CASH`, `BANK_TRANSFER`
   - `status`: `ACTIVE`, `PAID`, `OVERDUE`, `CANCELLED`

2. **`DebtSchedule`** - Lịch trả nợ (tự động sinh khi `payment_type = INSTALLMENT`)
   - Mặc định chia làm **12 kỳ** (12 tháng)
   - Mỗi kỳ gồm: `principal` (gốc), `interest` (lãi), `installment` (tổng)

3. **`DebtPayment`** - Thanh toán nợ
   - **KHÔNG CẦN `order_id`** - Thanh toán độc lập với đơn hàng
   - Gắn liền trực tiếp với `debt_id`
   - Optional: `schedule_id` (nếu thanh toán cho 1 kỳ cụ thể)

---

## 🔥 API Endpoints

### 1️⃣ TẠO NỢ MỚI

**POST** `/api/debts`

**Request Body:**
```json
{
  "debtType": "DEALER_DEBT",
  "dealerId": 1,
  "customerId": null,  // NULL nếu là DEALER_DEBT
  "amountDue": 1000000000,
  "paymentType": "INSTALLMENT",
  "paymentMethod": "BANK_TRANSFER",
  "interestRate": 1.5,
  "dueDate": "2025-12-31T00:00:00",
  "notes": "Nợ mua xe VF8"
}
```

**Response:**
```json
{
  "success": true,
  "message": "Debt created successfully",
  "data": {
    "debtId": 1,
    "debtType": "DEALER_DEBT",
    "amountDue": 1000000000,
    "amountPaid": 0,
    "status": "ACTIVE",
    "debtSchedules": [
      {
        "scheduleId": 1,
        "periodNo": 1,
        "installment": 85000000,
        "dueDate": "2025-11-01",
        "status": "PENDING"
      },
      // ... 11 kỳ còn lại
    ]
  }
}
```

**Lưu ý:** Nếu `paymentType = INSTALLMENT`, hệ thống **tự động sinh 12 kỳ thanh toán**.

---

### 2️⃣ THANH TOÁN NỢ

**POST** `/api/debts/{debtId}/payments`

**Request Body:**
```json
{
  "amount": 50000000,
  "paymentMethod": "CASH",
  "scheduleId": 1,  // Optional: ID của kỳ thanh toán
  "referenceNumber": "TT123456",  // Optional
  "notes": "Thanh toán tiền mặt",  // Optional
  "createdBy": "admin"  // Optional
}
```

**Response:**
```json
{
  "success": true,
  "message": "Payment made successfully",
  "data": {
    "paymentId": 1,
    "debtId": 1,
    "amount": 50000000,
    "paymentMethod": "CASH",
    "paymentDate": "2025-10-27T10:00:00"
  }
}
```

**Workflow:**
1. Validate số tiền không vượt quá số tiền còn nợ
2. Tạo `DebtPayment` record
3. Cập nhật `amount_paid` vào `Debt`
4. Nếu có `scheduleId`: Update status của schedule đó thành `PAID`
5. Nếu đã thanh toán đủ (`amount_paid >= amount_due`): Update `Debt.status = PAID`

---

### 3️⃣ LẤY DANH SÁCH NỢ CỦA DEALER (dealer nợ hãng)

**GET** `/api/debts/dealer-debts`

**Quyền:** `ADMIN`, `EVM_STAFF`

**Response:**
```json
{
  "success": true,
  "data": [
    {
      "debtId": 1,
      "debtType": "DEALER_DEBT",
      "dealerId": 1,
      "amountDue": 1000000000,
      "amountPaid": 50000000,
      "status": "ACTIVE"
    }
  ]
}
```

---

### 4️⃣ LẤY NỢ CỦA CUSTOMER (customer nợ dealer)

**GET** `/api/debts/customer-debts/{dealerId}`

**Quyền:** `ADMIN`, `EVM_STAFF`, `DEALER_STAFF`, `DEALER_MANAGER`

**Response:** Tương tự endpoint trên, nhưng `debtType = CUSTOMER_DEBT`

---

### 5️⃣ LẤY LỊCH TRẢ NỢ

**GET** `/api/debts/{debtId}/schedules`

**Response:**
```json
{
  "success": true,
  "data": [
    {
      "scheduleId": 1,
      "periodNo": 1,
      "installment": 85000000,
      "principal": 83000000,
      "interest": 2000000,
      "status": "PAID",
      "dueDate": "2025-11-01"
    },
    {
      "scheduleId": 2,
      "periodNo": 2,
      "installment": 85000000,
      "principal": 83150000,
      "interest": 1850000,
      "status": "PENDING",
      "dueDate": "2025-12-01"
    }
  ]
}
```

---

### 6️⃣ LẤY LỊCH SỬ THANH TOÁN

**GET** `/api/debts/{debtId}/payments`

**Response:**
```json
{
  "success": true,
  "data": [
    {
      "paymentId": 1,
      "amount": 50000000,
      "paymentMethod": "CASH",
      "paymentDate": "2025-10-27T10:00:00",
      "notes": "Thanh toán tiền mặt"
    }
  ]
}
```

---

## 💡 Workflow Thanh toán nợ

### Trường hợp 1: Trả thẳng (FULL_PAYMENT)

```
1. Tạo Debt (paymentType = FULL_PAYMENT)
   └─> Không sinh schedule

2. Thanh toán 1 lần toàn bộ số tiền
   POST /api/debts/{debtId}/payments
   { "amount": 1000000000 }
   └─> debt.status = PAID
```

### Trường hợp 2: Trả góp (INSTALLMENT)

```
1. Tạo Debt (paymentType = INSTALLMENT)
   └─> Tự động sinh 12 kỳ thanh toán
   └─> debtSchedules = [
         { periodNo: 1, installment: 85000000, status: PENDING },
         { periodNo: 2, installment: 85000000, status: PENDING },
         ...
       ]

2. Thanh toán từng kỳ
   POST /api/debts/{debtId}/payments
   {
     "amount": 85000000,
     "scheduleId": 1  // Thanh toán kỳ 1
   }
   └─> schedule[0].status = PAID
   └─> debt.amountPaid += 85000000
   
3. Tiếp tục thanh toán kỳ 2, 3... cho đến hết

4. Khi debt.amountPaid >= debt.amountDue
   └─> debt.status = PAID
```

---


## 🚀 Ví dụ sử dụng

### Scenario: Dealer nợ hãng 1 tỷ, trả góp 12 tháng

**Bước 1:** Tạo nợ
```bash
POST /api/debts
{
  "debtType": "DEALER_DEBT",
  "dealerId": 1,
  "amountDue": 1000000000,
  "paymentType": "INSTALLMENT",
  "interestRate": 1.5
}
```

**Bước 2:** Thanh toán kỳ 1
```bash
POST /api/debts/1/payments
{
  "amount": 85000000,
  "paymentMethod": "BANK_TRANSFER",
  "scheduleId": 1,
  "referenceNumber": "TT123456"
}
```

**Kết quả:**
- `debt.amountPaid` = 85,000,000
- `schedule[0].status` = "PAID"
- `debt.status` = "ACTIVE" (chưa trả hết)

---

## 📝 Notes

1. ✅ **Không cần `order_id`** - Thanh toán độc lập với đơn hàng
2. ✅ **Tự động sinh schedule** - Khi `paymentType = INSTALLMENT`
3. ✅ **Validate số tiền** - Không cho phép thanh toán vượt số tiền còn nợ
4. ✅ **Auto update status** - Tự động chuyển `PAID` khi đủ tiền

