package com.vider.quantum.engine.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Service
public class S3Service {

    private final S3Client s3Client;

    @Value("${aws.bucketName}")
    private String bucketName;

    @Autowired
    public S3Service(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    public byte[] downloadFile(String bucketName, String key) throws IOException {
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();

        try (ResponseInputStream<GetObjectResponse> s3Object = s3Client.getObject(getObjectRequest);
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = s3Object.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            return outputStream.toByteArray();
        }
    }

    public byte[] downloadFile(String key) throws IOException {
        return downloadFile(bucketName, key);
    }
}

