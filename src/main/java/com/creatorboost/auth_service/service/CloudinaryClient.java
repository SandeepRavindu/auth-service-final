package com.creatorboost.auth_service.service;

import com.creatorboost.auth_service.config.CloudinaryConfig;
import com.cloudinary.*;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class CloudinaryClient {

    private final Cloudinary cloudinary;

    public String uploadFile(MultipartFile file, String fileName) throws IOException {
        Map uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
                "public_id", fileName,
                "overwrite", true,
                "resource_type", "image"
        ));
        return uploadResult.get("secure_url").toString();
    }

    public boolean deleteFile(String filename) {
        try {
            cloudinary.uploader().destroy(filename, ObjectUtils.asMap("resource_type", "image"));
            return true;
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete file", e);
        }
    }
}
