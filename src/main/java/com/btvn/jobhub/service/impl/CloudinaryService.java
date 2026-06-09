package com.btvn.jobhub.service.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CloudinaryService {

    private final Cloudinary cloudinary;

    // Danh sách các Content-Type hợp lệ được hệ thống cho phép
    private static final List<String> ALLOWED_CONTENT_TYPES = List.of(
            "application/pdf",   // File PDF
            "image/jpeg",        // Ảnh JPG / JPEG
            "image/png"          // Ảnh PNG
    );

    public String uploadFile(MultipartFile file) {
        // 1. Fail-Fast: Kiểm tra file rỗng
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("File tải lên không được để trống.");
        }

        // 2. Fail-Fast: Kiểm tra định dạng dựa trên Content-Type nhận diện từ trình duyệt
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            throw new RuntimeException("Định dạng file không hợp lệ! Hệ thống chỉ chấp nhận file định dạng PDF, JPG, hoặc PNG.");
        }

        // 3. Tiến hành upload lên Cloudinary sau khi đã vượt qua các tầng kiểm duyệt
        try {
            Map uploadResult = cloudinary.uploader().upload(file.getBytes(),
                    ObjectUtils.asMap("resource_type", "auto"));

            return uploadResult.get("secure_url").toString();
        } catch (IOException e) {
            throw new RuntimeException("Lỗi xảy ra trong quá trình upload file lên Cloudinary: " + e.getMessage());
        }
    }
}