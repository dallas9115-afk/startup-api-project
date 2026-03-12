package com.example.startupproject.member;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.io.IOException;
import java.time.Duration;
import java.util.UUID;

@Service
public class S3Service {

    @Value("${cloud.aws.s3.bucket}")
    private String bucketName;

    private final S3Client s3Client;

    public S3Service(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    public String uploadFile(MultipartFile file) throws IOException {
        String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(fileName)
                .contentType(file.getContentType())
                .build();

        s3Client.putObject(putObjectRequest,
                RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

        return fileName;
    }

    public String generatePresignedUrl(String fileName) {
        try (S3Presigner presigner = S3Presigner.builder()
                .region(Region.AP_NORTHEAST_2) // 서울 리전 직접 주입
                .build()) {

            GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofMinutes(60)) // 60분 유효기간
                    .getObjectRequest(req -> req.bucket(bucketName).key(fileName))
                    .build();

            return presigner.presignGetObject(presignRequest).url().toString();
        }
    }
}