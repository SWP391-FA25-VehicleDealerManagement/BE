-- ==========================================
-- THÊM CỘT 'shipped_date' VÀO BẢNG DealerRequest
-- ==========================================
-- Script này thêm trường shipped_date để tracking ngày giao hàng cho đại lý
-- Khi allocate xe thành công, hệ thống sẽ tự động:
-- 1. Đổi status từ "APPROVED" → "SHIPPED"
-- 2. Set shipped_date = thời điểm hiện tại
-- ==========================================

PRINT '=== BẮT ĐẦU THÊM CỘT shipped_date VÀO DealerRequest ===';
GO

-- Kiểm tra xem cột shipped_date đã tồn tại chưa
IF NOT EXISTS (
    SELECT 1 
    FROM INFORMATION_SCHEMA.COLUMNS 
    WHERE TABLE_SCHEMA = 'dbo' 
    AND TABLE_NAME = 'DealerRequest' 
    AND COLUMN_NAME = 'shipped_date'
)
BEGIN
    PRINT '--- Bước 1: Thêm cột shipped_date (DATETIME, NULL) vào DealerRequest ---';
    
    ALTER TABLE dbo.DealerRequest
    ADD shipped_date DATETIME NULL;
    
    PRINT '✅ Đã thêm cột shipped_date thành công!';
    
    -- Optional: Cập nhật shipped_date cho các request đã có status = "SHIPPED"
    -- (Nếu muốn đồng bộ dữ liệu cũ)
    -- UPDATE dbo.DealerRequest
    -- SET shipped_date = approved_date
    -- WHERE status = 'SHIPPED' AND shipped_date IS NULL;
    
    PRINT '--- Hoàn tất! Cột shipped_date đã sẵn sàng sử dụng. ---';
END
ELSE
BEGIN
    PRINT '⚠️ Cột shipped_date đã tồn tại trong bảng DealerRequest. Bỏ qua.';
END
GO

PRINT '=== HOÀN TẤT MIGRATION ===';
GO

