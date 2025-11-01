package com.example.evm.service.storage;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {
    // Khởi tạo thư mục lưu trữ
    void init();

    // Lưu file và trả về tên file đã được lưu
    String save(MultipartFile file);

    // Tải file dưới dạng Resource
    Resource load(String filename);
}