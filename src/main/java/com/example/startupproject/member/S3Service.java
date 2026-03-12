package com.example.startupproject.member;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.UUID;

@Service
public class S3Service {

    @Value("${cloud.aws.s3.bucket}")
    private String bucketName;

    // 추가: application.yml에 등록한 CloudFront 도메인 주소
    @Value("${cloud.aws.cloudfront.domain}")
    private String cloudFrontDomain;

    private final S3Client s3Client;

    public S3Service(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    // 1. 파일 업로드는 그대로
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

    // 2. 복잡한 로직을 지우고 단순 주소 조합으로 변경
    public String generatePresignedUrl(String fileName) {
        // 형식: https://dxxxxx.cloudfront.net/파일명
        return "https://" + cloudFrontDomain + "/" + fileName;
    }
}