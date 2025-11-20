package com.event_service.event_service.services;

import com.event_service.event_service.validations.FileValidator;
import com.example.common_libraries.exception.InvalidFileException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FileSizeValidatorTest {

    private FileValidator fileValidator;

    @BeforeEach
    void setUp() {
        fileValidator = new FileValidator();
    }

    @Test
    void shouldAcceptValidSingleFile() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "image.png", "image/png", "dummy content".getBytes()
        );
        fileValidator.validate(file);
    }

    @Test
    void shouldRejectInvalidFileType() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.txt", "text/plain", "dummy content".getBytes()
        );
        assertThrows(InvalidFileException.class, () -> fileValidator.validate(file));
    }

    @Test
    void shouldRejectFileExceedingMaxSize() {
        byte[] largeContent = new byte[11 * 1024 * 1024]; // 11 MB
        MockMultipartFile file = new MockMultipartFile(
                "file", "large-image.png", "image/png", largeContent
        );
        assertThrows(InvalidFileException.class, () -> fileValidator.validate(file));
    }

    @Test
    void shouldAcceptValidMultipleFiles() {
        MockMultipartFile file1 = new MockMultipartFile(
                "file1", "image1.png", "image/png", "dummy1".getBytes()
        );
        MockMultipartFile file2 = new MockMultipartFile(
                "file2", "image2.jpg", "image/jpg", "dummy2".getBytes()
        );
        fileValidator.validate(List.of(file1, file2));
    }

    @Test
    void shouldRejectMultipleFilesWithInvalidFile() {
        MockMultipartFile file1 = new MockMultipartFile(
                "file1", "image1.png", "image/png", "dummy1".getBytes()
        );
        MockMultipartFile file2 = new MockMultipartFile(
                "file2", "bad.txt", "text/plain", "dummy2".getBytes()
        );
        assertThrows(InvalidFileException.class, () -> fileValidator.validate(List.of(file1, file2)));
    }

    @Test
    void shouldRejectMultipleFilesWithTooLargeFile() {
        MockMultipartFile file1 = new MockMultipartFile(
                "file1", "image1.png", "image/png", "dummy1".getBytes()
        );
        byte[] largeContent = new byte[11 * 1024 * 1024]; // 11 MB
        MockMultipartFile file2 = new MockMultipartFile(
                "file2", "large-image.png", "image/png", largeContent
        );
        assertThrows(InvalidFileException.class, () -> fileValidator.validate(List.of(file1, file2)));
    }
}
