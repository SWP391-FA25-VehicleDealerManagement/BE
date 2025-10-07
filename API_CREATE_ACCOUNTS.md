# API Tạo Tài Khoản - EVM Management System

## Tổng quan
Hệ thống có 3 API để tạo các loại tài khoản khác nhau:

## 1. API Tạo Dealer Account (Manager) 
**Endpoint:** `POST /api/admin/create-dealer-account`
**Quyền:** Chỉ ADMIN mới có thể gọi
**Mô tả:** Tạo đồng thời cả Dealer entity và User account với role DEALER_MANAGER

### Request Body:
```json
{
  "dealerName": "ABC Motors",
  "dealerPhone": "0901234567",
  "dealerAddress": "123 Nguyen Van A Street, Ho Chi Minh City",
  "username": "abcmotors_manager",
  "password": "password123",
  "fullName": "Nguyen Van Manager",
  "userPhone": "0901234567",
  "email": "manager@abcmotors.com",
  "role": "ROLE_DEALER_MANAGER"
}
```

### Response:
```json
{
  "success": true,
  "message": "Dealer account created successfully",
  "data": {
    "dealerId": 1,
    "dealerName": "ABC Motors",
    "userId": 2,
    "username": "abcmotors_manager",
    "role": "ROLE_DEALER_MANAGER",
    "message": "Dealer account created successfully"
  }
}
```

## 2. API Tạo Dealer Staff Account
**Endpoint:** `POST /api/admin/create-dealer-staff`
**Quyền:** ADMIN hoặc EVM_STAFF có thể gọi
**Mô tả:** Tạo User account với role DEALER_STAFF cho dealer đã tồn tại

### Request Body:
```json
{
  "username": "abcmotors_staff",
  "password": "password123",
  "fullName": "Nguyen Van Staff",
  "phone": "0909876543",
  "email": "staff@abcmotors.com",
  "dealerId": 1
}
```

### Response:
```json
{
  "success": true,
  "message": "Dealer staff account created successfully",
  "data": {
    "userId": 3,
    "username": "abcmotors_staff",
    "role": "ROLE_DEALER_STAFF",
    "fullName": "Nguyen Van Staff",
    "email": "staff@abcmotors.com",
    "dealerId": 1,
    "dealerName": "ABC Motors",
    "message": "User account created successfully"
  }
}
```

## 3. API Tạo EVM Staff Account
**Endpoint:** `POST /api/admin/create-evm-staff`
**Quyền:** Chỉ ADMIN mới có thể gọi
**Mô tả:** Tạo User account với role EVM_STAFF (không thuộc dealer nào)

### Request Body:
```json
{
  "username": "evm_staff1",
  "password": "password123",
  "fullName": "Tran Van EVM",
  "phone": "0912345678",
  "email": "evmstaff@company.com"
}
```

### Response:
```json
{
  "success": true,
  "message": "EVM staff account created successfully",
  "data": {
    "userId": 4,
    "username": "evm_staff1",
    "role": "ROLE_EVM_STAFF",
    "fullName": "Tran Van EVM",
    "email": "evmstaff@company.com",
    "dealerId": null,
    "dealerName": null,
    "message": "User account created successfully"
  }
}
```

## Validation Rules

### Dealer Account (API 1):
- `dealerName`: Bắt buộc, tối đa 255 ký tự, không được trùng
- `username`: Bắt buộc, 3-100 ký tự, không được trùng
- `password`: Bắt buộc, tối thiểu 6 ký tự
- `role`: Phải là "ROLE_DEALER_MANAGER" hoặc "ROLE_DEALER_STAFF"

### User Account (API 2 & 3):
- `username`: Bắt buộc, 3-100 ký tự, không được trùng
- `password`: Bắt buộc, tối thiểu 6 ký tự
- `email`: Định dạng email hợp lệ
- `dealerId`: Bắt buộc cho DEALER_STAFF, dealer phải tồn tại

## Error Responses

### 400 Bad Request:
```json
{
  "success": false,
  "message": "Username already exists: abcmotors_manager",
  "data": null
}
```

### 403 Forbidden:
```json
{
  "success": false,
  "message": "Access denied",
  "data": null
}
```

### 500 Internal Server Error:
```json
{
  "success": false,
  "message": "Failed to create dealer account: Database error",
  "data": null
}
```

## Roles trong hệ thống:
- `ROLE_ADMIN`: Quản trị viên hệ thống
- `ROLE_EVM_STAFF`: Nhân viên EVM 
- `ROLE_DEALER_MANAGER`: Quản lý dealer
- `ROLE_DEALER_STAFF`: Nhân viên dealer

## Authentication:
Tất cả API đều yêu cầu JWT token trong header:
```
Authorization: Bearer <jwt-token>
```