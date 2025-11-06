package com.example.common_libraries.service;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface S3Service {
    String uploadImage(MultipartFile file);
    List<String> uploadImages(List<MultipartFile> file);
}
