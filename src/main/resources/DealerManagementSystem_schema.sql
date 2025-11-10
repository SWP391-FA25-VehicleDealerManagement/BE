//////////////////////////////////////////////////////////
// DEALER MANAGEMENT SYSTEM - DATABASE SCHEMA (DBML)
//////////////////////////////////////////////////////////

Table Dealer {
  dealer_id bigint [pk, increment]
  dealerName nvarchar(255)
  phone varchar(50)
  address nvarchar(500)
  createdBy nvarchar(100)
  createdDate datetime [default: `GETDATE()`]
  status varchar(20) [default: 'ACTIVE']
}

Table User {
  user_id bigint [pk, increment]
  userName nvarchar(100) [unique]
  fullName nvarchar(255)
  password nvarchar(255)
  phone varchar(50)
  email nvarchar(255) [unique]
  role varchar(50)
  dealer_id bigint [ref: > Dealer.dealer_id]
  createdDate datetime [default: `GETDATE()`]
  dateModified datetime
  refreshTokenExpiryTime datetime2(6)
  resetToken varchar(255)
  resetTokenExpiry datetime2(6)
}

Table Customer {
  customer_id bigint [pk, increment]
  customerName nvarchar(255)
  email nvarchar(255)
  phone varchar(50)
  dealer_id bigint [ref: > Dealer.dealer_id]
  createBy nvarchar(100)
}

Table VehicleModel {
  model_id bigint [pk, increment]
  name nvarchar(100)
  description nvarchar(max)
  status nvarchar(50) [default: 'ACTIVE']
  manufacturer nvarchar(100) [default: 'VinFast']
  year int [default: 2024]
  body_type nvarchar(50)
}

Table VehicleVariant {
  variant_id bigint [pk, increment]
  model_id bigint [ref: > VehicleModel.model_id]
  name nvarchar(150)
  image nvarchar(500)
  status nvarchar(50) [default: 'ACTIVE']
  msrp decimal(18,2) [default: 0]
}

Table VehicleDetail {
  detail_id bigint [pk, increment]
  variant_id bigint [unique, ref: > VehicleVariant.variant_id]
  dimensions_mm nvarchar(100)
  wheelbase_mm int
  ground_clearance_mm int
  curb_weight_kg int
  seating_capacity int
  trunk_capacity_liters int
  engine_type nvarchar(255)
  max_power nvarchar(100)
  max_torque nvarchar(100)
  top_speed_kmh int
  drivetrain nvarchar(100)
  drive_modes nvarchar(255)
  battery_capacity_kwh decimal(10,2)
  range_per_charge_km int
  charging_time nvarchar(255)
  exterior_features nvarchar(max)
  interior_features nvarchar(max)
  airbags nvarchar(100)
  braking_system nvarchar(255)
  has_esc bit
  has_tpms bit
  has_rear_camera bit
  has_child_lock bit
}

Table ManufacturerStock {
  manufacturer_stock_id bigint [pk, increment]
  warehouse_name nvarchar(100)
  location nvarchar(255)
  status nvarchar(50) [default: 'ACTIVE']
  quantity int [default: 0]
}

Table InventoryStock {
  stock_id bigint [pk, increment]
  dealer_id bigint [ref: > Dealer.dealer_id]
  status nvarchar(50) [default: 'ACTIVE']
  quantity int [default: 0]
}

Table Vehicle {
  vehicle_id bigint [pk, increment]
  vin_number nvarchar(100) [unique]
  variant_id bigint [ref: > VehicleVariant.variant_id]
  color nvarchar(50)
  image nvarchar(500)
  manufacture_date date
  warranty_expiry_date date
  status nvarchar(50)
  manufacturer_stock_id bigint [ref: > ManufacturerStock.manufacturer_stock_id]
  inventory_stock_id bigint [ref: > InventoryStock.stock_id]
}

Table SalePrice {
  saleprice_id bigint [pk, increment]
  dealer_id bigint [ref: > Dealer.dealer_id]
  variant_id bigint [ref: > VehicleVariant.variant_id]
  base_price decimal(18,2)
  price decimal(18,2)
  effectivedate date
}

Table TestDrive {
  testdrive_id bigint [pk, increment]
  dealer_id bigint [ref: > Dealer.dealer_id]
  customer_id bigint [ref: > Customer.customer_id]
  vehicle_id bigint [ref: > Vehicle.vehicle_id]
  scheduled_date datetime
  status nvarchar(30) [default: 'SCHEDULED']
  notes nvarchar(500)
  assigned_by nvarchar(100)
  created_date datetime [default: `GETDATE()`]
}

Table Feedback {
  feedback_id bigint [pk, increment]
  testdrive_id bigint [ref: > TestDrive.testdrive_id]
  description nvarchar(255)
  feedbackType nvarchar(255)
  content nvarchar(255)
  status nvarchar(255)
}

Table DealerRequest {
  request_id bigint [pk, increment]
  dealer_id bigint [ref: > Dealer.dealer_id]
  user_id bigint [ref: > User.user_id]
  request_date datetime [default: `GETDATE()`]
  required_date datetime
  status nvarchar(20) [default: 'PENDING']
  priority nvarchar(10) [default: 'NORMAL']
  notes nvarchar(500)
  approved_date datetime
  approved_by nvarchar(100)
  shipped_date datetime
  delivery_date datetime
  total_amount decimal(18,2)
}

Table DealerRequestDetail {
  detail_id bigint [pk, increment]
  request_id bigint [ref: > DealerRequest.request_id]
  variant_id bigint [ref: > VehicleVariant.variant_id]
  color nvarchar(50)
  quantity int
  unit_price decimal(18,2)
  notes nvarchar(255)
}

Table Order {
  order_id bigint [pk, increment]
  customer_id bigint [ref: > Customer.customer_id]
  user_id bigint [ref: > User.user_id]
  dealer_id bigint [ref: > Dealer.dealer_id]
  total_price decimal(18,2)
  payment_method nvarchar(50)
  createddate datetime [default: `GETDATE()`]
  status varchar(50) [default: 'PENDING']
}

Table OrderDetail {
  orderdetail_id bigint [pk, increment]
  order_id bigint [ref: > Order.order_id]
  vehicle_id bigint [ref: > Vehicle.vehicle_id]
  quantity int
  price decimal(18,2)
}

Table VehicleContract {
  contract_id bigint [pk, increment]
  contract_number nvarchar(50) [unique]
  order_id bigint [ref: > Order.order_id]
  order_detail_id bigint [ref: > OrderDetail.orderdetail_id]
  dealer_id bigint [ref: > Dealer.dealer_id]
  customer_id bigint [ref: > Customer.customer_id]
  vehicle_id bigint [ref: > Vehicle.vehicle_id]
  sale_price decimal(18,2)
  payment_method nvarchar(100)
  contract_date date [default: `GETDATE()`]
  status nvarchar(50) [default: 'ACTIVE']
  notes nvarchar(500)
  file_url nvarchar(500)
}

Table Payment {
  payment_id bigint [pk, increment]
  order_id bigint [ref: > Order.order_id]
  amount decimal(18,2)
  status varchar(255)
  payment_method nvarchar(50)
  payment_type nvarchar(50)
  payment_date datetime
}

Table Debt {
  debt_id bigint [pk, increment]
  user_id bigint [ref: > User.user_id]
  dealer_id bigint [ref: > Dealer.dealer_id]
  customer_id bigint [ref: > Customer.customer_id]
  amount_due decimal(18,2)
  amount_paid decimal(18,2) [default: 0]
  debt_type nvarchar(20) [default: 'CUSTOMER_DEBT']
  interest_rate decimal(5,2) [default: 0]
  start_date datetime2(6)
  due_date datetime2(6)
  status nvarchar(20) [default: 'ACTIVE']
  payment_method nvarchar(50)
  notes nvarchar(500)
  created_date datetime2(6)
  updated_date datetime2(6)
}

Table DebtSchedule {
  schedule_id bigint [pk, increment]
  debt_id bigint [ref: > Debt.debt_id]
  period_no bigint
  start_balance decimal(18,2)
  principal decimal(18,2)
  interest decimal(18,2)
  installment decimal(18,2)
  end_balance decimal(18,2)
  due_date date
  paid_amount decimal(18,2) [default: 0]
  payment_date date
  status nvarchar(20) [default: 'PENDING']
  notes nvarchar(255)
}

Table DebtPayment {
  payment_id bigint [pk, increment]
  debt_id bigint [ref: > Debt.debt_id]
  schedule_id bigint [ref: > DebtSchedule.schedule_id]
  amount decimal(18,2)
  payment_date datetime [default: `GETDATE()`]
  payment_method nvarchar(50)
  reference_number nvarchar(100)
  notes nvarchar(500)
  created_by nvarchar(100)
  status nvarchar(20) [default: 'PENDING']
  confirmed_by nvarchar(100)
  confirmed_date datetime
  rejection_reason nvarchar(500)
}
