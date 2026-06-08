package com.btvn.jobhub.service;

import org.springframework.web.multipart.MultipartFile;

public interface CloudStorageService {
    // Xử lý upload MultipartFile lên Cloudinary/AWS S3
    String uploadFile(MultipartFile file); // [cite: 272, 273]
}
