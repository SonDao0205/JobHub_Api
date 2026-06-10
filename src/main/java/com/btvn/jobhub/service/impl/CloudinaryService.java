package com.btvn.jobhub.service.impl;

import com.btvn.jobhub.exception.BadRequestException;
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
            "application/pdf", "image/jpeg", "image/png"
    );

    // Xác định hằng số kích thước tối đa: 15MB đổi ra Bytes
    private static final long MAX_FILE_SIZE_BYTES = 15 * 1024 * 1024;

    public String uploadFile(MultipartFile file) {
        // 1. Fail-Fast: Kiểm tra file rỗng
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("File tải lên không được để trống.");
        }

        // 2. Fail-Fast: Kiểm tra dung lượng file (SRS yêu cầu giới hạn tối đa 15MB)
        if (file.getSize() > MAX_FILE_SIZE_BYTES) {
            throw new BadRequestException("Dung lượng file không được vượt quá 15MB.");
        }

        // 3. Fail-Fast: Kiểm tra định dạng dựa trên Content-Type nhận diện
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            throw new BadRequestException("Định dạng file không hợp lệ! Hệ thống chỉ chấp nhận file định dạng PDF, JPG, hoặc PNG.");
        }

        // 4. Tiến hành upload lên Cloudinary sau khi đã vượt qua các tầng kiểm duyệt
        try {
            Map uploadResult = cloudinary.uploader().upload(file.getBytes(),
                    ObjectUtils.asMap("resource_type", "auto"));

            return uploadResult.get("secure_url").toString();
        } catch (IOException e) {
            // Lỗi hệ thống khi truyền phát file
            throw new RuntimeException("Lỗi xảy ra trong quá trình upload file lên Cloudinary: " + e.getMessage());
        }
    }
}