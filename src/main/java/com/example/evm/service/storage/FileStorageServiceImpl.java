package com.example.evm.service.storage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileStorageServiceImpl implements FileStorageService {

    private final Path rootLocation;

    // Đọc đường dẫn lưu file từ application.properties (vd: file.upload-dir=uploads)
    public FileStorageServiceImpl(@Value("${file.upload-dir}") String uploadDir) {
        this.rootLocation = Paths.get(uploadDir);
    }

    // Khởi tạo thư mục gốc khi app chạy lần đầu
    @Override
    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(rootLocation);
        } catch (IOException e) {
            throw new RuntimeException(" Could not initialize storage directory!", e);
        }
    }

    /**
     * Lưu file vào thư mục gốc
     */
    @Override
    public String save(MultipartFile file) {
        return saveToSubFolder(file, "");
    }

    /**
     * Lưu file vào thư mục con (ví dụ: "contracts", "vehicles", ...)
     */
    public String saveToSubFolder(MultipartFile file, String subFolder) {
        if (file.isEmpty()) {
            throw new RuntimeException(" Failed to store empty file.");
        }

        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
        String uniqueFilename = UUID.randomUUID().toString() + "_" + originalFilename;

        try {
            Path destinationFolder = rootLocation.resolve(subFolder).normalize().toAbsolutePath();
            Files.createDirectories(destinationFolder); // tự động tạo thư mục con nếu chưa có

            Path destinationFile = destinationFolder.resolve(uniqueFilename);

            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, destinationFile, StandardCopyOption.REPLACE_EXISTING);
            }

            return uniqueFilename;
        } catch (IOException e) {
            throw new RuntimeException(" Failed to store file.", e);
        }
    }

    /**
     * Load file theo thư mục con + tên file
     * Ví dụ: load("contracts", "Contract_123.docx")
     */
    public Resource load(String subFolder, String filename) {
        try {
            Path folderPath = rootLocation.resolve(subFolder).normalize().toAbsolutePath();
            Path file = folderPath.resolve(filename);
            Resource resource = new UrlResource(file.toUri());

            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new RuntimeException(" Could not read the file: " + filename);
            }
        } catch (MalformedURLException e) {
            throw new RuntimeException(" Error while reading file: " + e.getMessage());
        }
    }

    @Override
    public void delete(String relativePath) {
         try {
            // Nối đường dẫn gốc (uploads) với đường dẫn tương đối
            Path file = rootLocation.resolve(Paths.get(relativePath)).normalize().toAbsolutePath();
            
            // Kiểm tra bảo mật
            if (!file.startsWith(this.rootLocation)) {
                 throw new RuntimeException("Cannot access file outside current directory.");
            }

            // Xóa file nếu nó tồn tại
            Files.deleteIfExists(file);
         } catch (IOException e) {
             // Ném lỗi runtime nếu không xóa được
             throw new RuntimeException("Failed to delete file: " + relativePath, e);
         }
    }

}
