package com.example.common_libraries.service;

import com.example.common_libraries.exception.FileUploadException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3ServiceImpl implements S3Service {

    private final S3Client s3Client;

    @Value("${aws.s3.bucket}")
    private String bucketName;

    @Value("${aws.s3.region}")
    private String region;


    @Override
    public String uploadImage(MultipartFile file) {
        String key = String.format("images/%d-%s", System.currentTimeMillis(), file.getOriginalFilename());
        try {
            s3Client.putObject(
                    PutObjectRequest.builder()
                            .bucket(bucketName)
                            .key(key)
                            .build(),
                    RequestBody.fromBytes(file.getBytes())
            );

            return String.format(
                    "https://%s.s3.%s.amazonaws.com/%s",
                    bucketName,
                    region,
                    key
            );
        } catch (IOException e) {
            log.error("IOException Error while uploading image: {}", e.getMessage());
            throw new FileUploadException("Error while uploading image, please try again");
        } catch (S3Exception e) {
            log.error("S3Exception Error while uploading image: {}", e.getMessage());
            throw new FileUploadException("File Upload Failed, please try again later");
        } catch (Exception e) {
            log.error("Error while uploading image: {}", e.getMessage());
            throw new FileUploadException("Something went wrong while uploading file");
        }
    }


    @Override
    public List<String> uploadImages(List<MultipartFile> files) {
        List<String> imageList = new ArrayList<>();
        for (MultipartFile file : files) {
            try {
                String url = uploadImage(file);
                imageList.add(url);
            } catch (S3Exception e) {
                log.error("S3Exception Error while uploading images: {}", e.getMessage());
                throw new FileUploadException("File Upload Failed, please try again later");

            } catch (Exception e) {
                log.error("Error while uploading images: {}", e.getMessage());
                throw new FileUploadException("Something went wrong while uploading file");
            }
        }
        return imageList;
    }
}

