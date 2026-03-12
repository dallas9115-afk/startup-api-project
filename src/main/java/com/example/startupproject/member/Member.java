package com.example.startupproject.member;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
public class Member {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private int age;
    private String mbti;
    private String profileImageUrl; // S3의 객체 키(파일 이름) 또는 전체 경로 저장

    public Member(String name, int age, String mbti) {
        this.name = name;
        this.age = age;
        this.mbti = mbti;
    }

    // 프로필 사진 업데이트용 메서드
    public void updateProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }
}