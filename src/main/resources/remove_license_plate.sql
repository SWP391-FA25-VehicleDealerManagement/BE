-- ============================================
-- Script: Xóa cột license_plate khỏi bảng Vehicle
-- Ngày tạo: 2025-10-20
-- Mô tả: Migration script để xóa cột license_plate không còn sử dụng
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
    
    -- Xóa cột license_plate
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
PRINT 'Migration hoàn tất!';
GO

