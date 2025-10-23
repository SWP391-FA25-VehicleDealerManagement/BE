-- ===================================================================
-- MIGRATION: Vehicle & Stock Redesign - Version 2 (SIMPLIFIED)
-- Ngày: 2025-10-23
-- ===================================================================

USE [DealerManagementSystem];
GO

PRINT '=== BẮT ĐẦU MIGRATION V2 ===';
GO

-- ===================================================================
-- PHẦN 1: BACKUP DỮ LIỆU
-- ===================================================================
PRINT '--- Bước 1: Backup dữ liệu ---';

IF OBJECT_ID('ManufacturerStock_Backup', 'U') IS NOT NULL DROP TABLE ManufacturerStock_Backup;
SELECT * INTO ManufacturerStock_Backup FROM ManufacturerStock;
PRINT '✓ Backup ManufacturerStock';

IF OBJECT_ID('InventoryStock_Backup', 'U') IS NOT NULL DROP TABLE InventoryStock_Backup;
SELECT * INTO InventoryStock_Backup FROM InventoryStock;
PRINT '✓ Backup InventoryStock';

IF OBJECT_ID('Vehicle_Backup', 'U') IS NOT NULL DROP TABLE Vehicle_Backup;
SELECT * INTO Vehicle_Backup FROM Vehicle;
PRINT '✓ Backup Vehicle';
GO

-- ===================================================================
-- PHẦN 2: XÓA DỮ LIỆU CŨ
-- ===================================================================
PRINT '--- Bước 2: Xóa dữ liệu ---';

DELETE FROM Vehicle;
DELETE FROM ManufacturerStock;
DELETE FROM InventoryStock;
PRINT '✓ Đã xóa dữ liệu cũ';
GO

-- ===================================================================
-- PHẦN 3: DROP TẤT CẢ CONSTRAINTS CŨ
-- ===================================================================
PRINT '--- Bước 3: Drop tất cả constraints cũ ---';

-- Drop tất cả FK của Vehicle
DECLARE @sql NVARCHAR(MAX);
SELECT @sql = STRING_AGG('ALTER TABLE Vehicle DROP CONSTRAINT [' + name + '];', CHAR(10))
FROM sys.foreign_keys
WHERE parent_object_id = OBJECT_ID('Vehicle');
IF @sql IS NOT NULL EXEC sp_executesql @sql;
PRINT '✓ Dropped Vehicle FKs';

-- Drop tất cả CHECK constraints của Vehicle
SELECT @sql = STRING_AGG('ALTER TABLE Vehicle DROP CONSTRAINT [' + name + '];', CHAR(10))
FROM sys.check_constraints
WHERE parent_object_id = OBJECT_ID('Vehicle');
IF @sql IS NOT NULL EXEC sp_executesql @sql;
PRINT '✓ Dropped Vehicle CHECKs';

-- Drop tất cả FK của ManufacturerStock
SELECT @sql = STRING_AGG('ALTER TABLE ManufacturerStock DROP CONSTRAINT [' + name + '];', CHAR(10))
FROM sys.foreign_keys
WHERE parent_object_id = OBJECT_ID('ManufacturerStock');
IF @sql IS NOT NULL EXEC sp_executesql @sql;
PRINT '✓ Dropped ManufacturerStock FKs';

-- Drop tất cả UNIQUE constraints của ManufacturerStock
SELECT @sql = STRING_AGG('ALTER TABLE ManufacturerStock DROP CONSTRAINT [' + name + '];', CHAR(10))
FROM sys.key_constraints
WHERE type = 'UQ' AND parent_object_id = OBJECT_ID('ManufacturerStock');
IF @sql IS NOT NULL EXEC sp_executesql @sql;
PRINT '✓ Dropped ManufacturerStock UNIQUEs';

-- Drop tất cả CHECK constraints của ManufacturerStock
SELECT @sql = STRING_AGG('ALTER TABLE ManufacturerStock DROP CONSTRAINT [' + name + '];', CHAR(10))
FROM sys.check_constraints
WHERE parent_object_id = OBJECT_ID('ManufacturerStock');
IF @sql IS NOT NULL EXEC sp_executesql @sql;
PRINT '✓ Dropped ManufacturerStock CHECKs';

-- Drop tất cả FK của InventoryStock
SELECT @sql = STRING_AGG('ALTER TABLE InventoryStock DROP CONSTRAINT [' + name + '];', CHAR(10))
FROM sys.foreign_keys
WHERE parent_object_id = OBJECT_ID('InventoryStock');
IF @sql IS NOT NULL EXEC sp_executesql @sql;
PRINT '✓ Dropped InventoryStock FKs';

-- Drop tất cả CHECK constraints của InventoryStock
SELECT @sql = STRING_AGG('ALTER TABLE InventoryStock DROP CONSTRAINT [' + name + '];', CHAR(10))
FROM sys.check_constraints
WHERE parent_object_id = OBJECT_ID('InventoryStock');
IF @sql IS NOT NULL EXEC sp_executesql @sql;
PRINT '✓ Dropped InventoryStock CHECKs';

-- Drop tất cả DEFAULT constraints (Vehicle)
SELECT @sql = STRING_AGG('ALTER TABLE Vehicle DROP CONSTRAINT [' + name + '];', CHAR(10))
FROM sys.default_constraints
WHERE parent_object_id = OBJECT_ID('Vehicle');
IF @sql IS NOT NULL EXEC sp_executesql @sql;
PRINT '✓ Dropped Vehicle DEFAULTs';

-- Drop tất cả DEFAULT constraints (ManufacturerStock)
SELECT @sql = STRING_AGG('ALTER TABLE ManufacturerStock DROP CONSTRAINT [' + name + '];', CHAR(10))
FROM sys.default_constraints
WHERE parent_object_id = OBJECT_ID('ManufacturerStock');
IF @sql IS NOT NULL EXEC sp_executesql @sql;
PRINT '✓ Dropped ManufacturerStock DEFAULTs';

-- Drop tất cả DEFAULT constraints (InventoryStock)
SELECT @sql = STRING_AGG('ALTER TABLE InventoryStock DROP CONSTRAINT [' + name + '];', CHAR(10))
FROM sys.default_constraints
WHERE parent_object_id = OBJECT_ID('InventoryStock');
IF @sql IS NOT NULL EXEC sp_executesql @sql;
PRINT '✓ Dropped InventoryStock DEFAULTs';
GO

-- ===================================================================
-- PHẦN 4: DROP COLUMNS CŨ
-- ===================================================================
PRINT '--- Bước 4: Drop columns cũ ---';

-- Vehicle
IF EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('Vehicle') AND name = 'stock_id')
    ALTER TABLE Vehicle DROP COLUMN stock_id;
PRINT '✓ Dropped Vehicle.stock_id';

-- ManufacturerStock
IF EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('ManufacturerStock') AND name = 'variant_id')
    ALTER TABLE ManufacturerStock DROP COLUMN variant_id;
IF EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('ManufacturerStock') AND name = 'color')
    ALTER TABLE ManufacturerStock DROP COLUMN color;
IF EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('ManufacturerStock') AND name = 'quantity')
    ALTER TABLE ManufacturerStock DROP COLUMN quantity;
PRINT '✓ Dropped ManufacturerStock columns';

-- InventoryStock
IF EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('InventoryStock') AND name = 'variant_id')
    ALTER TABLE InventoryStock DROP COLUMN variant_id;
IF EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('InventoryStock') AND name = 'color')
    ALTER TABLE InventoryStock DROP COLUMN color;
IF EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('InventoryStock') AND name = 'quantity')
    ALTER TABLE InventoryStock DROP COLUMN quantity;
IF EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('InventoryStock') AND name = 'listing_price')
    ALTER TABLE InventoryStock DROP COLUMN listing_price;
PRINT '✓ Dropped InventoryStock columns';
GO

-- ===================================================================
-- PHẦN 5: ADD COLUMNS MỚI
-- ===================================================================
PRINT '--- Bước 5: Add columns mới ---';

-- Vehicle
IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('Vehicle') AND name = 'color')
    ALTER TABLE Vehicle ADD color VARCHAR(50) NOT NULL DEFAULT 'White';
IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('Vehicle') AND name = 'manufacturer_stock_id')
    ALTER TABLE Vehicle ADD manufacturer_stock_id BIGINT NULL;
IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('Vehicle') AND name = 'inventory_stock_id')
    ALTER TABLE Vehicle ADD inventory_stock_id BIGINT NULL;
IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('Vehicle') AND name = 'status')
    ALTER TABLE Vehicle ADD status VARCHAR(50) NOT NULL DEFAULT 'IN_MANUFACTURER_STOCK';
PRINT '✓ Added Vehicle columns';

-- ManufacturerStock
IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('ManufacturerStock') AND name = 'warehouse_name')
    ALTER TABLE ManufacturerStock ADD warehouse_name VARCHAR(100) NOT NULL DEFAULT 'Main Warehouse';
IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('ManufacturerStock') AND name = 'location')
    ALTER TABLE ManufacturerStock ADD location VARCHAR(255) NULL;
PRINT '✓ Added ManufacturerStock columns';
GO

-- ===================================================================
-- PHẦN 6: ADD FOREIGN KEYS
-- ===================================================================
PRINT '--- Bước 6: Add Foreign Keys ---';

-- Vehicle FKs
IF NOT EXISTS (SELECT * FROM sys.foreign_keys WHERE name = 'FK_Vehicle_ManufacturerStock')
    ALTER TABLE Vehicle
    ADD CONSTRAINT FK_Vehicle_ManufacturerStock 
    FOREIGN KEY (manufacturer_stock_id) REFERENCES ManufacturerStock(manufacturer_stock_id);

IF NOT EXISTS (SELECT * FROM sys.foreign_keys WHERE name = 'FK_Vehicle_InventoryStock')
    ALTER TABLE Vehicle
    ADD CONSTRAINT FK_Vehicle_InventoryStock 
    FOREIGN KEY (inventory_stock_id) REFERENCES InventoryStock(stock_id);

IF NOT EXISTS (SELECT * FROM sys.foreign_keys WHERE name = 'FK_Vehicle_Variant')
    ALTER TABLE Vehicle
    ADD CONSTRAINT FK_Vehicle_Variant 
    FOREIGN KEY (variant_id) REFERENCES VehicleVariant(variant_id);

PRINT '✓ Added Vehicle FKs';

-- InventoryStock FK (dealer_id)
IF NOT EXISTS (SELECT * FROM sys.foreign_keys WHERE name = 'FK_InventoryStock_Dealer')
    ALTER TABLE InventoryStock
    ADD CONSTRAINT FK_InventoryStock_Dealer 
    FOREIGN KEY (dealer_id) REFERENCES Dealer(dealer_id);

PRINT '✓ Added InventoryStock FKs';
GO

-- ===================================================================
-- PHẦN 7: ADD CHECK CONSTRAINT
-- ===================================================================
PRINT '--- Bước 7: Add Check Constraint ---';

IF NOT EXISTS (SELECT * FROM sys.check_constraints WHERE name = 'CK_Vehicle_OneLocation')
    ALTER TABLE Vehicle
    ADD CONSTRAINT CK_Vehicle_OneLocation 
    CHECK (
        (manufacturer_stock_id IS NOT NULL AND inventory_stock_id IS NULL) OR
        (manufacturer_stock_id IS NULL AND inventory_stock_id IS NOT NULL) OR
        (manufacturer_stock_id IS NULL AND inventory_stock_id IS NULL)
    );
PRINT '✓ Added Check Constraint';
GO

-- ===================================================================
-- PHẦN 8: CREATE INDEXES
-- ===================================================================
PRINT '--- Bước 8: Create Indexes ---';

IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = 'IX_Vehicle_ManufacturerStock')
    CREATE INDEX IX_Vehicle_ManufacturerStock ON Vehicle(manufacturer_stock_id);

IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = 'IX_Vehicle_InventoryStock')
    CREATE INDEX IX_Vehicle_InventoryStock ON Vehicle(inventory_stock_id);

IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = 'IX_Vehicle_Variant_Color')
    CREATE INDEX IX_Vehicle_Variant_Color ON Vehicle(variant_id, color);

IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = 'IX_Vehicle_Status')
    CREATE INDEX IX_Vehicle_Status ON Vehicle(status);

PRINT '✓ Created Indexes';
GO

-- ===================================================================
-- PHẦN 9: INSERT KHO MẶC ĐỊNH
-- ===================================================================
PRINT '--- Bước 9: Insert kho mặc định ---';

IF NOT EXISTS (SELECT 1 FROM ManufacturerStock WHERE warehouse_name = N'Kho Tổng VinFast')
BEGIN
    INSERT INTO ManufacturerStock (warehouse_name, location, status)
    VALUES (N'Kho Tổng VinFast', N'Hải Phòng, Việt Nam', 'ACTIVE');
    PRINT '✓ Đã tạo kho tổng mặc định';
END
GO

-- ===================================================================
-- PHẦN 10: INSERT SAMPLE DATA
-- ===================================================================
PRINT '--- Bước 10: Insert sample data ---';

DECLARE @WarehouseId BIGINT;
SELECT @WarehouseId = manufacturer_stock_id 
FROM ManufacturerStock 
WHERE status = 'ACTIVE' AND warehouse_name = N'Kho Tổng VinFast';

IF @WarehouseId IS NOT NULL
BEGIN
    -- Insert xe mẫu nếu có variant
    IF EXISTS (SELECT 1 FROM VehicleVariant WHERE variant_id = 1)
    BEGIN
        INSERT INTO Vehicle (vin_number, variant_id, color, manufacturer_stock_id, inventory_stock_id, manufacture_date, warranty_expiry_date, status)
        VALUES 
            ('VINVF5PLS00000001', 1, 'Red', @WarehouseId, NULL, GETDATE(), DATEADD(YEAR, 5, GETDATE()), 'IN_MANUFACTURER_STOCK'),
            ('VINVF5PLS00000002', 1, 'White', @WarehouseId, NULL, GETDATE(), DATEADD(YEAR, 5, GETDATE()), 'IN_MANUFACTURER_STOCK'),
            ('VINVF5PLS00000003', 1, 'Blue', @WarehouseId, NULL, GETDATE(), DATEADD(YEAR, 5, GETDATE()), 'IN_MANUFACTURER_STOCK');
        PRINT '✓ Đã tạo 3 xe mẫu (variant 1)';
    END

    IF EXISTS (SELECT 1 FROM VehicleVariant WHERE variant_id = 6)
    BEGIN
        INSERT INTO Vehicle (vin_number, variant_id, color, manufacturer_stock_id, inventory_stock_id, manufacture_date, warranty_expiry_date, status)
        VALUES 
            ('VINVF8ECO00000001', 6, 'Black', @WarehouseId, NULL, GETDATE(), DATEADD(YEAR, 5, GETDATE()), 'IN_MANUFACTURER_STOCK'),
            ('VINVF8ECO00000002', 6, 'Gray', @WarehouseId, NULL, GETDATE(), DATEADD(YEAR, 5, GETDATE()), 'IN_MANUFACTURER_STOCK');
        PRINT '✓ Đã tạo 2 xe mẫu (variant 6)';
    END
END
GO

-- ===================================================================
-- PHẦN 11: VERIFY KẾT QUẢ
-- ===================================================================
PRINT '--- Bước 11: Verify kết quả ---';

-- Đếm xe
DECLARE @VehicleCount INT;
SELECT @VehicleCount = COUNT(*) FROM Vehicle WHERE manufacturer_stock_id IS NOT NULL;
PRINT 'Tổng số xe trong kho: ' + CAST(@VehicleCount AS VARCHAR);

-- Hiển thị tổng hợp kho
PRINT '';
PRINT '=== TỔNG HỢP KHO ===';
SELECT 
    vr.name AS VariantName,
    v.color AS Color,
    COUNT(*) AS Quantity
FROM Vehicle v
JOIN VehicleVariant vr ON v.variant_id = vr.variant_id
WHERE v.manufacturer_stock_id IS NOT NULL
GROUP BY vr.name, v.color
ORDER BY vr.name, v.color;

PRINT '';
PRINT '=== ✅ MIGRATION HOÀN TẤT ===';
GO

