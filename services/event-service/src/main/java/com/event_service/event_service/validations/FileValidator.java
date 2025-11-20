package com.event_service.event_service.validations;

import com.example.common_libraries.exception.InvalidFileException;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Component
public class FileValidator {

    private final List<String> allowedTypes = List.of(
            MediaType.IMAGE_JPEG_VALUE,
            "image/jpg",
            MediaType.IMAGE_PNG_VALUE
    );

    private final long maxSize = 10 * 1024 * 1024;

    public void validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }
        checkTypeAndSize(file);
    }


    public void validate(List<MultipartFile> files) {
        for (MultipartFile file : files) {
            checkTypeAndSize(file);
        }
    }

    private void checkTypeAndSize(MultipartFile file) {
        if (!allowedTypes.contains(file.getContentType())) {
            throw new InvalidFileException("Invalid file type: " + file.getOriginalFilename());
        }
        if (file.getSize() > maxSize) {
            throw new InvalidFileException("File too large: " + file.getOriginalFilename());
        }
    }
}