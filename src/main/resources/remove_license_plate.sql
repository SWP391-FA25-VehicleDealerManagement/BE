-- ============================================
-- Script: Xóa cột license_plate khỏi bảng Vehicle
-- Ngày tạo: 2025-10-20
-- Mô tả: Migration script để xóa cột license_plate không còn sử dụng
-- Phải xóa UNIQUE constraint trước khi xóa cột
-- ============================================

USE [DealerManagement];
GO

-- Kiểm tra xem cột license_plate có tồn tại không
IF EXISTS (
    SELECT 1 
    FROM sys.columns 
    WHERE object_id = OBJECT_ID(N'[dbo].[Vehicle]') 
    AND name = 'license_plate'
)
BEGIN
    PRINT 'Đang xóa cột license_plate khỏi bảng Vehicle...';
    
    -- Bước 1: Tìm và xóa UNIQUE constraint trên cột license_plate
    DECLARE @ConstraintName NVARCHAR(200);
    
    -- Tìm trong key_constraints
    SELECT @ConstraintName = name
    FROM sys.key_constraints
    WHERE type = 'UQ'
    AND parent_object_id = OBJECT_ID(N'[dbo].[Vehicle]')
    AND COL_NAME(parent_object_id, parent_column_id) = 'license_plate';
    
    -- Nếu không tìm thấy, tìm trong indexes (UNIQUE constraint có thể là index)
    IF @ConstraintName IS NULL
    BEGIN
        SELECT @ConstraintName = i.name
        FROM sys.indexes i
        INNER JOIN sys.index_columns ic ON i.object_id = ic.object_id AND i.index_id = ic.index_id
        INNER JOIN sys.columns c ON ic.object_id = c.object_id AND ic.column_id = c.column_id
        WHERE i.is_unique_constraint = 1
        AND i.object_id = OBJECT_ID(N'[dbo].[Vehicle]')
        AND c.name = 'license_plate';
    END
    
    -- Xóa constraint nếu tìm thấy
    IF @ConstraintName IS NOT NULL
    BEGIN
        DECLARE @SQL NVARCHAR(500);
        SET @SQL = 'ALTER TABLE [dbo].[Vehicle] DROP CONSTRAINT [' + @ConstraintName + ']';
        PRINT 'Đang xóa constraint: ' + @ConstraintName;
        EXEC sp_executesql @SQL;
        PRINT 'Đã xóa constraint thành công!';
    END
    ELSE
    BEGIN
        PRINT 'Không tìm thấy UNIQUE constraint trên cột license_plate.';
    END
    
    -- Bước 2: Xóa cột license_plate
    ALTER TABLE [dbo].[Vehicle] 
    DROP COLUMN [license_plate];
    
    PRINT 'Đã xóa cột license_plate thành công!';
END
ELSE
BEGIN
    PRINT 'Cột license_plate không tồn tại trong bảng Vehicle.';
END
GO

-- Xác nhận thay đổi
PRINT '✅ Migration hoàn tất!';
GO
