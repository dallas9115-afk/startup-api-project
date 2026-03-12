package com.example.startupproject.member;

import com.example.startupproject.member.Member;
import com.example.startupproject.member.MemberRequest;
import com.example.startupproject.member.MemberRepository;
import com.example.startupproject.member.S3Service; // S3Service가 있는 패키지 경로에 맞게 임포트 해주세요!
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Slf4j // 로깅 기능 활성화
@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberRepository memberRepository;
    private final S3Service s3Service; // S3 서비스 의존성 추가

    // 기존 API
    @PostMapping
    public ResponseEntity<Member> createMember(@RequestBody MemberRequest request) {
        log.info("[API - LOG] POST /api/members 요청 - 이름: {}", request.getName());

        try {
            Member member = new Member(request.getName(), request.getAge(), request.getMbti());
            Member savedMember = memberRepository.save(member);
            return ResponseEntity.ok(savedMember);
        } catch (Exception e) {
            log.error("[API - ERROR] 멤버 저장 중 에러 발생", e);
            throw new RuntimeException("멤버 생성 실패");
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Member> getMember(@PathVariable Long id) {
        log.info("[API - LOG] GET /api/members/{} 요청", id);

        try {
            Member member = memberRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("해당 멤버가 없습니다. id=" + id));
            return ResponseEntity.ok(member);
        } catch (Exception e) {
            log.error("[API - ERROR] 멤버 조회 중 에러 발생", e);
            throw new RuntimeException("멤버 조회 실패");
        }
    }

    // 프로필 이미지 관련 API 추가

    @PostMapping("/{id}/profile-image")
    public ResponseEntity<String> uploadProfileImage(
            @PathVariable Long id,
            @RequestParam("image") MultipartFile image) {

        log.info("[API - LOG] POST /api/members/{}/profile-image 요청 - 업로드 파일명: {}", id, image.getOriginalFilename());

        try {
            // 1. 멤버 조회
            Member member = memberRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("해당 멤버가 없습니다. id=" + id));

            // 2. S3에 이미지 업로드 및 파일명(Key) 반환
            String fileName = s3Service.uploadFile(image);

            // 3. 멤버 엔티티에 S3 파일명 업데이트 및 저장
            member.updateProfileImageUrl(fileName);
            memberRepository.save(member);

            log.info("[API - LOG] 프로필 이미지 업로드 성공 - S3 저장된 파일명: {}", fileName);
            return ResponseEntity.ok("프로필 이미지 업로드 성공! S3 파일명: " + fileName);
        } catch (Exception e) {
            log.error("[API - ERROR] 프로필 이미지 업로드 중 에러 발생", e);
            throw new RuntimeException("프로필 이미지 업로드 실패");
        }
    }

    @GetMapping("/{id}/profile-image")
    public ResponseEntity<String> getProfileImageUrl(@PathVariable Long id) {

        log.info("[API - LOG] GET /api/members/{}/profile-image 요청 (Presigned URL 발급)", id);

        try {
            // 1. 멤버 조회
            Member member = memberRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("해당 멤버가 없습니다. id=" + id));

            // 2. 저장된 파일명 확인
            String fileName = member.getProfileImageUrl();
            if (fileName == null || fileName.isEmpty()) {
                log.warn("[API - WARN] 등록된 프로필 이미지가 없습니다. id={}", id);
                return ResponseEntity.badRequest().body("등록된 프로필 이미지가 없습니다.");
            }

            // 3. S3Service를 통해 7일짜리 Presigned URL 생성
            String presignedUrl = s3Service.generatePresignedUrl(fileName);

            log.info("[API - LOG] Presigned URL 발급 성공 - id: {}", id);
            return ResponseEntity.ok(presignedUrl);
        } catch (Exception e) {
            log.error("[API - ERROR] Presigned URL 발급 중 에러 발생", e);
            throw new RuntimeException("Presigned URL 발급 실패");
        }
    }
}