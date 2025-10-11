-- Tạo database
CREATE DATABASE DealerManagementSystem;
GO

-- Sử dụng database mới tạo
USE DealerManagementSystem;
GO

-- Bảng Dealer
CREATE TABLE Dealer (
    dealer_id INT IDENTITY(1,1) PRIMARY KEY,
    dealerName NVARCHAR(255) NOT NULL,
    phone VARCHAR(255),
    address NVARCHAR(255),
    createdby VARCHAR(255),
    createddate DATETIME DEFAULT GETDATE()
);

-- Bảng User
CREATE TABLE [User] (
    user_id INT IDENTITY(1,1) PRIMARY KEY,
    userName VARCHAR(255),
    [password] NVARCHAR(255) NOT NULL,
    phone VARCHAR(255),
    email NVARCHAR(255),
    [role] VARCHAR(255),
    dealer_id INT,
    createddate DATETIME DEFAULT GETDATE(),
    datemodified DATETIME,
    refreshTokenExpiryTime DATETIME2(6),
    twoFactorEnabled BIT DEFAULT 0,
    twoFactorSecret VARCHAR(255),
    resetToken VARCHAR(255),
    resetTokenExpiry DATETIME2(6),
    fullName NVARCHAR(255),
    FOREIGN KEY (dealer_id) REFERENCES Dealer(dealer_id)
);

-- Bảng Customer
CREATE TABLE Customer (
    customer_id INT IDENTITY(1,1) PRIMARY KEY,
    customerName NVARCHAR(255) NOT NULL,
    email NVARCHAR(255),
    phone VARCHAR(255),
    dealer_id INT,
    createBy NVARCHAR(100),
    FOREIGN KEY (dealer_id) REFERENCES Dealer(dealer_id)
);

-- Bảng VehicleModel
CREATE TABLE VehicleModel (
    model_id INT IDENTITY(1,1) PRIMARY KEY,
    name NVARCHAR(255) NOT NULL,
    description NVARCHAR(255)
);

-- Bảng VehicleVariant
CREATE TABLE VehicleVariant (
    variant_id INT IDENTITY(1,1) PRIMARY KEY,
    model_id INT,
    name NVARCHAR(255) NOT NULL,
    image NVARCHAR(255),
    FOREIGN KEY (model_id) REFERENCES VehicleModel(model_id)
);

-- Bảng Vehicle
CREATE TABLE Vehicle (
    vehicle_id INT IDENTITY(1,1) PRIMARY KEY,
    variant_id INT,
    dealer_id INT,
    name NVARCHAR(255) NOT NULL,
    color VARCHAR(255),
    image NVARCHAR(255),
    features VARCHAR(255),
    model VARCHAR(255),
    price FLOAT,
    stock INT,
    version VARCHAR(255),
    model_id INT,
    FOREIGN KEY (dealer_id) REFERENCES Dealer(dealer_id),
    FOREIGN KEY (variant_id) REFERENCES VehicleVariant(variant_id),
    FOREIGN KEY (model_id) REFERENCES VehicleModel(model_id)
);

-- Bảng InventoryStock
CREATE TABLE InventoryStock (
    stock_id INT IDENTITY(1,1) PRIMARY KEY,
    vehicle_id INT,
    dealer_id INT,
    quantity INT,
    status VARCHAR(255),
    FOREIGN KEY (vehicle_id) REFERENCES Vehicle(vehicle_id),
    FOREIGN KEY (dealer_id) REFERENCES Dealer(dealer_id)
);

-- Bảng Order
CREATE TABLE [Order] (
    order_id INT IDENTITY(1,1) PRIMARY KEY,
    customer_id INT,
    user_id INT,
    total_price DECIMAL(18,2),
    payment_method NVARCHAR(50),
    createddate DATETIME DEFAULT GETDATE(),
    dealer_id INT,
    status VARCHAR(255),
    FOREIGN KEY (customer_id) REFERENCES Customer(customer_id),
    FOREIGN KEY (dealer_id) REFERENCES Dealer(dealer_id),
    FOREIGN KEY (user_id) REFERENCES [User](user_id)
);

-- Bảng OrderDetail
CREATE TABLE OrderDetail (
    orderdetail_id INT IDENTITY(1,1) PRIMARY KEY,
    order_id INT,
    vehicle_id INT,
    promotion_id INT,
    quantity INT,
    price FLOAT,
    FOREIGN KEY (order_id) REFERENCES [Order](order_id),
    FOREIGN KEY (vehicle_id) REFERENCES Vehicle(vehicle_id)
);

-- Bảng Payment
CREATE TABLE Payment (
    payment_id INT IDENTITY(1,1) PRIMARY KEY,
    order_id INT,
    amount FLOAT,
    status VARCHAR(255),
    payment_method NVARCHAR(50),
    payment_date DATETIME,
    FOREIGN KEY (order_id) REFERENCES [Order](order_id)
);

-- Bảng Promotions
CREATE TABLE Promotions (
    promo_id INT IDENTITY(1,1) PRIMARY KEY,
    dealer_id INT,
    title NVARCHAR(255),
    description NVARCHAR(255),
    discount_rate DECIMAL(5,2),
    start_date DATE,
    end_date DATE,
    FOREIGN KEY (dealer_id) REFERENCES Dealer(dealer_id)
);

-- Bảng PromotionDealer
CREATE TABLE PromotionDealer (
    promotdealer_id INT IDENTITY(1,1) PRIMARY KEY,
    promo_id INT,
    dealer_id INT,
    FOREIGN KEY (promo_id) REFERENCES Promotions(promo_id),
    FOREIGN KEY (dealer_id) REFERENCES Dealer(dealer_id)
);

-- Bảng PromotionVehicle
CREATE TABLE PromotionVehicle (
    promotvehicle_id INT IDENTITY(1,1) PRIMARY KEY,
    vehicle_id INT,
    promo_id INT,
    FOREIGN KEY (promo_id) REFERENCES Promotions(promo_id),
    FOREIGN KEY (vehicle_id) REFERENCES Vehicle(vehicle_id)
);

-- Bảng SalePrice
CREATE TABLE SalePrice (
    saleprice_id INT IDENTITY(1,1) PRIMARY KEY,
    dealer_id INT,
    variant_id INT,
    price FLOAT,
    effectivedate DATE,
    FOREIGN KEY (dealer_id) REFERENCES Dealer(dealer_id),
    FOREIGN KEY (variant_id) REFERENCES VehicleVariant(variant_id)
);

-- Bảng Debt
CREATE TABLE Debt (
    debt_id INT IDENTITY(1,1) PRIMARY KEY,
    user_id INT,
    dealer_id INT,
    customer_id INT,
    amount_due DECIMAL(18,2),
    amount_paid DECIMAL(18,2),
    status VARCHAR(255),
    FOREIGN KEY (user_id) REFERENCES [User](user_id),
    FOREIGN KEY (dealer_id) REFERENCES Dealer(dealer_id),
    FOREIGN KEY (customer_id) REFERENCES Customer(customer_id)
);

-- Bảng DebtSchedule
CREATE TABLE DebtSchedule (
    schedule_id INT IDENTITY(1,1) PRIMARY KEY,
    debt_id INT,
    period_no INT,
    start_balance DECIMAL(18,2),
    principal FLOAT,
    interest FLOAT,
    installment FLOAT,
    end_balance DECIMAL(18,2),
    due_date DATE,
    paid_amount DECIMAL(18,2),
    status VARCHAR(255),
    FOREIGN KEY (debt_id) REFERENCES Debt(debt_id)
);

-- Bảng Testdrive
CREATE TABLE Testdrive (
    testdrive_id INT IDENTITY(1,1) PRIMARY KEY,
    dealer_id INT,
    customer_id INT,
    [date] DATE,
    status VARCHAR(255),
    assignedBy VARCHAR(255),
    FOREIGN KEY (dealer_id) REFERENCES Dealer(dealer_id),
    FOREIGN KEY (customer_id) REFERENCES Customer(customer_id)
);

-- Bảng Feedback
CREATE TABLE Feedback (
    feedback_id INT IDENTITY(1,1) PRIMARY KEY,
    testdrive_id INT,
    description NVARCHAR(255),
    feedbackType VARCHAR(255),
    content NVARCHAR(255),
    status VARCHAR(255),
    FOREIGN KEY (testdrive_id) REFERENCES Testdrive(testdrive_id)
);




--Admin user
INSERT INTO [dbo].[User]
       ([userName]
       ,[password]
       ,[phone]
       ,[email]
       ,[role]
       ,[dealer_id]
       ,[createddate]
       ,[datemodified]
       ,[refreshtokenexpiryTime])
VALUES
       ('admin'
       ,'$2a$10$vI8aWBnW3fID.ZQ4/zo1G.q1lRps.9cGLcZEiGDMVr5yUP1KUOYTa' -- bcrypt hash of 'password'
       ,'0123456789'
       ,'admin@example.com'
       ,'ADMIN'
       ,NULL -- assuming admin is not associated with a dealer
       ,GETDATE() -- current date/time for creation
       ,NULL
       ,NULL
       )
GO


--EVM Staff User
INSERT INTO [dbo].[User]
       ([userName]
       ,[password]
       ,[phone]
       ,[email]
       ,[role]
       ,[dealer_id]
       ,[createddate]
       ,[datemodified]
       ,[refreshtokenexpiryTime])
VALUES
       ('evmStaff'
       ,'$2a$10$vI8aWBnW3fID.ZQ4/zo1G.q1lRps.9cGLcZEiGDMVr5yUP1KUOYTa' -- bcrypt hash of 'password'
       ,'0123456789'
       ,'admin@example.com'
       ,'EVM_STAFF'
       ,NULL -- assuming admin is not associated with a dealer
       ,GETDATE() -- current date/time for creation
       ,NULL
       ,NULL
       )
GO

-- Dealer Manager User
INSERT INTO [dbo].[User]
       ([userName]
       ,[password]
       ,[phone]
       ,[email]
       ,[role]
       ,[dealer_id]
       ,[createddate]
       ,[datemodified]
       ,[refreshtokenexpiryTime])
VALUES
       ('dealerManager'
       ,'$2a$10$vI8aWBnW3fID.ZQ4/zo1G.q1lRps.9cGLcZEiGDMVr5yUP1KUOYTa' -- bcrypt hash of 'password'
       ,'0987654321'
       ,'dealermanager@example.com'
       ,'DEALER_MANAGER'
       ,1 -- assuming dealer_id 1 exists, change as needed
       ,GETDATE() -- current date/time for creation
       ,NULL
       ,NULL)
GO

-- Dealer Staff User
INSERT INTO [dbo].[User]
       ([userName]
       ,[password]
       ,[phone]
       ,[email]
       ,[role]
       ,[dealer_id]
       ,[createddate]
       ,[datemodified]
       ,[refreshtokenexpiryTime])
VALUES
       ('dealerStaff'
       ,'$2a$10$vI8aWBnW3fID.ZQ4/zo1G.q1lRps.9cGLcZEiGDMVr5yUP1KUOYTa' -- bcrypt hash of 'password'
       ,'0567891234'
       ,'dealerstaff@example.com'
       ,'DEALER_STAFF'
       ,1 -- assuming dealer_id 1 exists, change as needed
       ,GETDATE() -- current date/time for creation
       ,NULL
       ,NULL)
GO


INSERT INTO Dealer (dealerName, phone, address, createdby)
VALUES (N'Đại lý A', '0901234567', N'123 Nguyễn Trãi, HCM', 'system');


INSERT INTO [User] (userName, [password], dealer_id)
VALUES ('admin', '123456', 1);