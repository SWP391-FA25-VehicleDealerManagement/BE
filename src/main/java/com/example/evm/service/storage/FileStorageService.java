package com.example.evm.service.storage;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {
    // 🔹 Khởi tạo thư mục lưu trữ gốc
    void init();

    // 🔹 Lưu file vào thư mục gốc
    String save(MultipartFile file);

    // 🔹 Lưu file vào thư mục con (ví dụ: "vehicles", "contracts")
    String saveToSubFolder(MultipartFile file, String subFolder);

    // 🔹 Load file từ thư mục con
    Resource load(String subFolder, String filename);
}
