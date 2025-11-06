package com.example.common_libraries.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.sqs.SqsClient;

import java.net.URI;

@Slf4j
@Configuration
public class AWSConfig {
    @Value("${aws.s3.custom.endpoint}")
    private String endpoint;

    @Value("${aws.s3.region}")
    private String region;

    @Value("${spring.cloud.aws.sqs.endpoint}")
    private String sqsEndpoint;


    @Bean
    public S3Client s3Client() {
        Region awsRegion = Region.of(region);
        return S3Client.builder()
                .region(awsRegion)
                .credentialsProvider(DefaultCredentialsProvider.create())
                .endpointOverride(URI.create(endpoint))
                .build();
    }

    @Bean
    public SqsClient sqsClient(){
        log.info("SQS Endpoint from properties: '{}'", sqsEndpoint);
        log.info("SQS Endpoint length: {}", sqsEndpoint != null ? sqsEndpoint.length() : "null");

        if (sqsEndpoint == null || sqsEndpoint.trim().isEmpty()) {
            throw new IllegalStateException("SQS endpoint is not configured");
        }

        String cleanEndpoint = sqsEndpoint.trim();
        log.info("Creating SQS client with endpoint: '{}'", cleanEndpoint);

        return SqsClient.builder()
                .endpointOverride(java.net.URI.create(sqsEndpoint))
                .region(Region.EU_WEST_1)
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();
    }
}
