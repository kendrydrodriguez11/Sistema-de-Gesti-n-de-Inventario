package com.example.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.time.Duration;

@RequiredArgsConstructor
@Service
public class S3ServiceImpl implements S3Service {

    private final S3Presigner s3Presigner;

    @Override
    public String generatePresignedPutUrl(String bucketName, String key) {
        PutObjectRequest putRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();

        PresignedPutObjectRequest presigned = s3Presigner.presignPutObject(p ->
                p.signatureDuration(Duration.ofMinutes(10))
                 .putObjectRequest(putRequest)
        );

        return presigned.url().toString();
    }

    @Override
    public String generatePresignedGetUrl(String bucketName, String key) {
        GetObjectRequest getRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();

        PresignedGetObjectRequest presigned = s3Presigner.presignGetObject(p ->
                p.signatureDuration(Duration.ofMinutes(10))
                 .getObjectRequest(getRequest)
        );

        return presigned.url().toString();
    }
}
