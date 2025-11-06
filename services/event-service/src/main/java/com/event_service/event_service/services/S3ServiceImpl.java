package com.event_service.event_service.services;

import com.example.common_libraries.exception.FileUploadException;
import lombok.RequiredArgsConstructor;
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
            throw new FileUploadException("Error while uploading image, please try again");
        } catch (S3Exception e) {
            throw new FileUploadException("File Upload Failed, please try again later");

        } catch (Exception e) {
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
                throw new FileUploadException("File Upload Failed, please try again later");

            } catch (Exception e) {
                throw new FileUploadException("Something went wrong while uploading file");
            }
        }
        return imageList;
    }
}
