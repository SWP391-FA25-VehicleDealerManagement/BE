-- =============================================
-- DEALER MANAGEMENT SYSTEM - DATABASE SCHEMA
-- Ready for dbdiagram.io import
-- =============================================

-- 1. DEALER
CREATE TABLE Dealer (
    dealer_id BIGINT IDENTITY(1,1) PRIMARY KEY,
    dealerName NVARCHAR(255) NOT NULL,
    phone VARCHAR(50),
    address NVARCHAR(500),
    createdBy NVARCHAR(100),
    createdDate DATETIME DEFAULT GETDATE(),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE'
);

-- 2. USER
CREATE TABLE [User] (
    user_id BIGINT IDENTITY(1,1) PRIMARY KEY,
    userName NVARCHAR(100) NOT NULL UNIQUE,
    fullName NVARCHAR(255),
    password NVARCHAR(255) NOT NULL,
    phone VARCHAR(50),
    email NVARCHAR(255) UNIQUE,
    role VARCHAR(50) NOT NULL,
    dealer_id BIGINT,
    createdDate DATETIME DEFAULT GETDATE(),
    dateModified DATETIME,
    refreshTokenExpiryTime DATETIME2(6),
    resetToken VARCHAR(255),
    resetTokenExpiry DATETIME2(6),
    FOREIGN KEY (dealer_id) REFERENCES Dealer(dealer_id)
);

-- 3. CUSTOMER
CREATE TABLE Customer (
    customer_id BIGINT IDENTITY(1,1) PRIMARY KEY,
    customerName NVARCHAR(255) NOT NULL,
    email NVARCHAR(255),
    phone VARCHAR(50),
    dealer_id BIGINT,
    createBy NVARCHAR(100),
    FOREIGN KEY (dealer_id) REFERENCES Dealer(dealer_id)
);

-- 4. VEHICLE MODEL
CREATE TABLE VehicleModel (
    model_id BIGINT IDENTITY(1,1) PRIMARY KEY,
    name NVARCHAR(100) NOT NULL,
    description NVARCHAR(MAX),
    status NVARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    manufacturer NVARCHAR(100) NOT NULL DEFAULT 'VinFast',
    year INT NOT NULL DEFAULT 2024,
    body_type NVARCHAR(50)
);

-- 5. VEHICLE VARIANT
CREATE TABLE VehicleVariant (
    variant_id BIGINT IDENTITY(1,1) PRIMARY KEY,
    model_id BIGINT NOT NULL,
    name NVARCHAR(150) NOT NULL,
    image NVARCHAR(500),
    status NVARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    msrp DECIMAL(18, 2) NOT NULL DEFAULT 0,
    FOREIGN KEY (model_id) REFERENCES VehicleModel(model_id)
);

-- 6. VEHICLE DETAIL (1-1 with Variant)
CREATE TABLE VehicleDetail (
    detail_id BIGINT IDENTITY(1,1) PRIMARY KEY,
    variant_id BIGINT NOT NULL UNIQUE,
    dimensions_mm NVARCHAR(100),
    wheelbase_mm INT,
    ground_clearance_mm INT,
    curb_weight_kg INT,
    seating_capacity INT,
    trunk_capacity_liters INT,
    engine_type NVARCHAR(255),
    max_power NVARCHAR(100),
    max_torque NVARCHAR(100),
    top_speed_kmh INT,
    drivetrain NVARCHAR(100),
    drive_modes NVARCHAR(255),
    battery_capacity_kwh DECIMAL(10, 2),
    range_per_charge_km INT,
    charging_time NVARCHAR(255),
    exterior_features NVARCHAR(MAX),
    interior_features NVARCHAR(MAX),
    airbags NVARCHAR(100),
    braking_system NVARCHAR(255),
    has_esc BIT,
    has_tpms BIT,
    has_rear_camera BIT,
    has_child_lock BIT,
    CONSTRAINT FK_VehicleDetail_VehicleVariant FOREIGN KEY (variant_id) 
        REFERENCES VehicleVariant(variant_id) ON DELETE CASCADE
);

-- 7. MANUFACTURER STOCK (Warehouse Container)
CREATE TABLE ManufacturerStock (
    manufacturer_stock_id BIGINT IDENTITY(1,1) PRIMARY KEY,
    warehouse_name NVARCHAR(100) NOT NULL,
    location NVARCHAR(255),
    status NVARCHAR(50) DEFAULT 'ACTIVE',
    quantity INT NOT NULL DEFAULT 0
);

-- 8. INVENTORY STOCK (Dealer Warehouse Container)
CREATE TABLE InventoryStock (
    stock_id BIGINT IDENTITY(1,1) PRIMARY KEY,
    dealer_id BIGINT NOT NULL,
    status NVARCHAR(50) DEFAULT 'ACTIVE',
    quantity INT NOT NULL DEFAULT 0,
    FOREIGN KEY (dealer_id) REFERENCES Dealer(dealer_id)
);

-- 9. VEHICLE (Central Entity with VIN)
CREATE TABLE Vehicle (
    vehicle_id BIGINT IDENTITY(1,1) PRIMARY KEY,
    vin_number NVARCHAR(100) NOT NULL UNIQUE,
    variant_id BIGINT NOT NULL,
    color NVARCHAR(50) NOT NULL,
    image NVARCHAR(500),
    manufacture_date DATE,
    warranty_expiry_date DATE,
    status NVARCHAR(50),
    manufacturer_stock_id BIGINT,
    inventory_stock_id BIGINT,
    FOREIGN KEY (variant_id) REFERENCES VehicleVariant(variant_id),
    FOREIGN KEY (manufacturer_stock_id) REFERENCES ManufacturerStock(manufacturer_stock_id),
    FOREIGN KEY (inventory_stock_id) REFERENCES InventoryStock(stock_id),
    CONSTRAINT CHK_Vehicle_Location CHECK (
        (manufacturer_stock_id IS NOT NULL AND inventory_stock_id IS NULL) OR
        (manufacturer_stock_id IS NULL AND inventory_stock_id IS NOT NULL) OR
        (manufacturer_stock_id IS NULL AND inventory_stock_id IS NULL AND status = 'SOLD')
    )
);

-- 10. SALE PRICE
CREATE TABLE SalePrice (
    saleprice_id BIGINT IDENTITY(1,1) PRIMARY KEY,
    dealer_id BIGINT NOT NULL,
    variant_id BIGINT NOT NULL,
    base_price DECIMAL(18, 2) NOT NULL DEFAULT 0,
    price DECIMAL(18, 2) NOT NULL,
    effectivedate DATE,
    FOREIGN KEY (dealer_id) REFERENCES Dealer(dealer_id),
    FOREIGN KEY (variant_id) REFERENCES VehicleVariant(variant_id)
);

-- 11. PROMOTIONS
CREATE TABLE Promotions (
    promo_id BIGINT IDENTITY(1,1) PRIMARY KEY,
    dealer_id BIGINT,
    title NVARCHAR(255) NOT NULL,
    description NVARCHAR(MAX),
    discount_rate DECIMAL(5, 2),
    start_date DATE,
    end_date DATE,
    FOREIGN KEY (dealer_id) REFERENCES Dealer(dealer_id)
);

-- 12. PROMOTION VEHICLE (Many-to-Many)
CREATE TABLE PromotionVehicle (
    promo_id BIGINT NOT NULL,
    vehicle_id BIGINT NOT NULL,
    PRIMARY KEY (promo_id, vehicle_id),
    FOREIGN KEY (promo_id) REFERENCES Promotions(promo_id),
    FOREIGN KEY (vehicle_id) REFERENCES Vehicle(vehicle_id)
);

-- 13. PROMOTION DEALER (Many-to-Many)
CREATE TABLE PromotionDealer (
    promo_id BIGINT NOT NULL,
    dealer_id BIGINT NOT NULL,
    PRIMARY KEY (promo_id, dealer_id),
    FOREIGN KEY (promo_id) REFERENCES Promotions(promo_id),
    FOREIGN KEY (dealer_id) REFERENCES Dealer(dealer_id)
);

-- 14. TEST DRIVE
CREATE TABLE TestDrive (
    testdrive_id BIGINT IDENTITY(1,1) PRIMARY KEY,
    dealer_id BIGINT,
    customer_id BIGINT,
    vehicle_id BIGINT,
    scheduled_date DATETIME,
    status NVARCHAR(30) DEFAULT 'SCHEDULED',
    notes NVARCHAR(500),
    assigned_by NVARCHAR(100),
    created_date DATETIME DEFAULT GETDATE(),
    FOREIGN KEY (dealer_id) REFERENCES Dealer(dealer_id),
    FOREIGN KEY (customer_id) REFERENCES Customer(customer_id),
    FOREIGN KEY (vehicle_id) REFERENCES Vehicle(vehicle_id)
);

-- 15. FEEDBACK
CREATE TABLE Feedback (
    feedback_id BIGINT IDENTITY(1,1) PRIMARY KEY,
    testdrive_id BIGINT,
    description NVARCHAR(255),
    feedbackType NVARCHAR(255),
    content NVARCHAR(255),
    status NVARCHAR(255),
    FOREIGN KEY (testdrive_id) REFERENCES TestDrive(testdrive_id)
);

-- 16. DEALER REQUEST
CREATE TABLE DealerRequest (
    request_id BIGINT IDENTITY(1,1) PRIMARY KEY,
    dealer_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    request_date DATETIME DEFAULT GETDATE(),
    required_date DATETIME,
    status NVARCHAR(20) DEFAULT 'PENDING',
    priority NVARCHAR(10) DEFAULT 'NORMAL',
    notes NVARCHAR(500),
    approved_date DATETIME,
    approved_by NVARCHAR(100),
    shipped_date DATETIME,
    delivery_date DATETIME,
    total_amount DECIMAL(18, 2),
    FOREIGN KEY (dealer_id) REFERENCES Dealer(dealer_id),
    FOREIGN KEY (user_id) REFERENCES [User](user_id)
);

-- 17. DEALER REQUEST DETAIL
CREATE TABLE DealerRequestDetail (
    detail_id BIGINT IDENTITY(1,1) PRIMARY KEY,
    request_id BIGINT NOT NULL,
    variant_id BIGINT NOT NULL,
    color NVARCHAR(50),
    quantity INT NOT NULL,
    unit_price DECIMAL(18, 2),
    notes NVARCHAR(255),
    FOREIGN KEY (request_id) REFERENCES DealerRequest(request_id),
    FOREIGN KEY (variant_id) REFERENCES VehicleVariant(variant_id)
);

-- 18. ORDER
CREATE TABLE [Order] (
    order_id BIGINT IDENTITY(1,1) PRIMARY KEY,
    customer_id BIGINT NULL,
    user_id BIGINT NOT NULL,
    dealer_id BIGINT NOT NULL,
    total_price DECIMAL(18, 2),
    payment_method NVARCHAR(50),
    createddate DATETIME DEFAULT GETDATE(),
    status VARCHAR(50) DEFAULT 'PENDING',
    FOREIGN KEY (customer_id) REFERENCES Customer(customer_id),
    FOREIGN KEY (user_id) REFERENCES [User](user_id),
    FOREIGN KEY (dealer_id) REFERENCES Dealer(dealer_id)
);

-- 19. ORDER DETAIL
CREATE TABLE OrderDetail (
    orderdetail_id BIGINT IDENTITY(1,1) PRIMARY KEY,
    order_id BIGINT NOT NULL,
    vehicle_id BIGINT NOT NULL,
    promotion_id BIGINT,
    quantity INT NOT NULL,
    price DECIMAL(18, 2) NOT NULL,
    FOREIGN KEY (order_id) REFERENCES [Order](order_id),
    FOREIGN KEY (vehicle_id) REFERENCES Vehicle(vehicle_id),
    FOREIGN KEY (promotion_id) REFERENCES Promotions(promo_id)
);

-- 20. VEHICLE CONTRACT
CREATE TABLE VehicleContract (
    contract_id BIGINT IDENTITY(1,1) PRIMARY KEY,
    contract_number NVARCHAR(50) NOT NULL UNIQUE,
    order_id BIGINT NOT NULL,
    order_detail_id BIGINT NOT NULL,
    dealer_id BIGINT NOT NULL,
    customer_id BIGINT NOT NULL,
    vehicle_id BIGINT NOT NULL,
    sale_price DECIMAL(18,2) NOT NULL,
    payment_method NVARCHAR(100),
    contract_date DATE NOT NULL DEFAULT GETDATE(),
    status NVARCHAR(50) DEFAULT 'ACTIVE',
    notes NVARCHAR(500),
    file_url NVARCHAR(500),
    FOREIGN KEY (order_id) REFERENCES [Order](order_id),
    FOREIGN KEY (order_detail_id) REFERENCES OrderDetail(orderdetail_id),
    FOREIGN KEY (dealer_id) REFERENCES Dealer(dealer_id),
    FOREIGN KEY (customer_id) REFERENCES Customer(customer_id),
    FOREIGN KEY (vehicle_id) REFERENCES Vehicle(vehicle_id)
);

-- 21. PAYMENT
CREATE TABLE Payment (
    payment_id BIGINT IDENTITY(1,1) PRIMARY KEY,
    order_id BIGINT,
    amount DECIMAL(18, 2),
    status VARCHAR(255),
    payment_method NVARCHAR(50),
    payment_type NVARCHAR(50),
    payment_date DATETIME,
    FOREIGN KEY (order_id) REFERENCES [Order](order_id)
);

-- 22. DEBT
CREATE TABLE Debt (
    debt_id BIGINT IDENTITY(1,1) PRIMARY KEY,
    user_id BIGINT,
    dealer_id BIGINT,
    customer_id BIGINT,
    amount_due DECIMAL(18, 2),
    amount_paid DECIMAL(18, 2) DEFAULT 0,
    debt_type NVARCHAR(20) NOT NULL DEFAULT 'CUSTOMER_DEBT',
    interest_rate DECIMAL(5, 2) DEFAULT 0,
    start_date DATETIME2(6),
    due_date DATETIME2(6),
    status NVARCHAR(20) DEFAULT 'ACTIVE',
    payment_method NVARCHAR(50),
    notes NVARCHAR(500) NULL,
    created_date DATETIME2(6) NULL,
    updated_date DATETIME2(6) NULL,
    FOREIGN KEY (customer_id) REFERENCES Customer(customer_id),
    FOREIGN KEY (dealer_id) REFERENCES Dealer(dealer_id),
    FOREIGN KEY (user_id) REFERENCES [User](user_id)
);

-- 23. DEBT SCHEDULE
CREATE TABLE DebtSchedule (
    schedule_id BIGINT IDENTITY(1,1) PRIMARY KEY,
    debt_id BIGINT NOT NULL,
    period_no BIGINT,
    start_balance DECIMAL(18, 2),
    principal DECIMAL(18, 2),
    interest DECIMAL(18, 2),
    installment DECIMAL(18, 2),
    end_balance DECIMAL(18, 2),
    due_date DATE,
    paid_amount DECIMAL(18, 2) DEFAULT 0,
    payment_date DATE,
    status NVARCHAR(20) DEFAULT 'PENDING',
    notes NVARCHAR(255),
    FOREIGN KEY (debt_id) REFERENCES Debt(debt_id)
);

-- 24. DEBT PAYMENT
CREATE TABLE DebtPayment (
    payment_id BIGINT IDENTITY(1,1) PRIMARY KEY,
    debt_id BIGINT NOT NULL,
    schedule_id BIGINT,
    amount DECIMAL(18, 2) NOT NULL,
    payment_date DATETIME DEFAULT GETDATE(),
    payment_method NVARCHAR(50),
    reference_number NVARCHAR(100),
    notes NVARCHAR(500),
    created_by NVARCHAR(100),
    status NVARCHAR(20) DEFAULT 'PENDING',
    confirmed_by NVARCHAR(100),
    confirmed_date DATETIME,
    rejection_reason NVARCHAR(500),
    FOREIGN KEY (debt_id) REFERENCES Debt(debt_id),
    FOREIGN KEY (schedule_id) REFERENCES DebtSchedule(schedule_id)
);
