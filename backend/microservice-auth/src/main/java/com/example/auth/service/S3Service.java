package com.example.auth.service;

public interface S3Service {
    String generatePresignedPutUrl(String bucketName, String key);
    String generatePresignedGetUrl(String bucketName, String key);
}
