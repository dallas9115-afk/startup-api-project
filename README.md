#  Startup Profile API Project

> **Cloud Native 아키텍처 설계를 통한 고가용성 프로필 관리 시스템 구축**

본 프로젝트는 AWS 클라우드 환경에서 보안과 확장성, 그리고 성능 최적화를 고려하여 설계된 회원 프로필 관리 API 서버입니다. 로컬 개발 환경부터 전 세계 배포를 위한 CDN 적용까지의 전 과정을 단계별로 구현했습니다.

---

##  핵심 구현 및 성과

### 1. 인프라 설계 및 비용 관리 (LV 0, 1, 2)

* **보안 중심 네트워킹:** VPC 환경 내에서 Public/Private Subnet을 분리하여 EC2(애플리케이션)는 외부에 노출하고, RDS(데이터베이스)는 프라이빗 영역에 격리하여 보안을 강화했습니다.
* **Security Group Chaining:** IP 기반의 화이트리스트 대신 보안 그룹 ID를 참조하는 체이닝 방식을 적용하여, 내부 IP 변경에 유연하게 대응하는 방화벽 체계를 구축했습니다.
* **중앙 집중형 설정 관리:** AWS Parameter Store를 연동하여 DB 접속 정보 및 운영 환경 변수를 안전하게 관리하고, Spring Boot Actuator를 통해 서버 상태를 실시간 모니터링합니다.

### 2. 스토리지 및 권한 관리 (LV 3)

* **객체 스토리지 연동:** S3의 "모든 퍼블릭 액세스 차단"을 활성화하고, **IAM Role**을 EC2에 부여하여 Access Key 없이 안전하게 이미지 업로드 기능을 구현했습니다.
* **보안 접근 제어:** 초기에는 유효기간이 설정된 Presigned URL 방식을 도입하여 파일에 대한 한시적 접근 권한을 관리했습니다.

### 3. CI/CD 및 운영 자동화 (LV 4)

* **Docker 컨테이너화:** 애플리케이션을 도커 이미지로 패키징하여 "내 로컬에선 되는데 서버에선 안 되는" 환경 의존성 문제를 해결했습니다.
* **GitHub Actions 파이프라인:** `main` 브랜치 푸시 시 [빌드 → 도커 라이징 → 도커 허브 푸시 → EC2 배포] 가 자동으로 수행되는 CI/CD 환경을 구축했습니다.

### 4. 성능 최적화: CloudFront CDN (LV 6)

* **전 세계 가속화:** CloudFront를 S3 앞에 배치하여 전 세계 어디서나 빠른 속도로 이미지를 로딩할 수 있도록 최적화했습니다.
* **OAC(Origin Access Control):** S3 버킷을 비공개로 유지한 채 오직 CloudFront 배포판을 통해서만 객체에 접근할 수 있도록 보안 정책을 강화했습니다.

---

## 트러블슈팅 (Troubleshooting)

###  VPC 리소스 통신 장애 해결

* **문제:** EC2와 RDS 간 연결 시 `different networks` 에러 발생.
* **원인:** 서로 다른 VPC에 리소스가 프로비저닝되어 내부 통신 불가.
* **해결:** `startup-final-vpc`를 새로 구축하고 동일 VPC 내 Subnet 그룹을 재설정하여 해결.

###  Actuator 엔드포인트 404 에러

* **문제:** `/actuator/info` 호출 시 404 에러 발생.
* **원인:** `application.yml` 내 `management` 속성의 들여쓰기(Indentation) 오류로 계층 구조가 어긋남.
* **해결:** `management` 속성을 최상위 노드로 이동시켜 정상 노출 확인.

###  CI/CD 배포 타임아웃

* **문제:** GitHub Actions에서 EC2 SSH 접속 시 `i/o timeout` 발생.
* **원인:** 보안 그룹이 '내 IP'로만 제한되어 깃허브 가상 서버의 접근을 차단함.
* **해결:** 인바운드 규칙의 SSH(22번) 포트를 `0.0.0.0/0`으로 개방하여 자동화 배포 성공.

---

##  과제 증빙 자료

### [LV 0] 예산 관리

* **설정 내용:** 월 예산 $100 / 80% 도달 시 알림
<img width="515" height="829" alt="AWS 예산 설정 1" src="https://github.com/user-attachments/assets/454b288d-4ba2-4da4-ab72-df826d1d27eb" />
<img width="499" height="435" alt="AWS 예산 설정 2" src="https://github.com/user-attachments/assets/95443176-f675-4103-8f98-2c6ade404329" />
<img width="512" height="534" alt="AWS 예산 설정 3" src="https://github.com/user-attachments/assets/4713ed56-e9e2-43d9-b4e2-b95677641f29" />

### [LV 1 & 2] 운영 서버 및 DB 보안

* **EC2 퍼블릭 IP:** `13.124.144.180`
<img width="760" height="638" alt="EC2 퍼플릭 IP" src="https://github.com/user-attachments/assets/7569be13-6c81-4a3d-bacd-1bba5afc2f56" />


* **Actuator Info URL:** [http://13.124.144.180:8080/actuator/info](https://www.google.com/search?q=http://13.124.144.180:8080/actuator/info)

* **RDS 보안 그룹 (Chaining):**
<img width="1264" height="350" alt="RDS 보안그룹 설정" src="https://github.com/user-attachments/assets/131a4ff5-4933-4ebb-8730-6188e8c39791" />

### [LV 3] S3 접근 성공 확인

* **설명:** IAM Role을 통해 권한을 획득하고 이미지를 정상적으로 로드한 화면입니다.
  
<img width="810" height="761" alt="Lv3  프로필 사진 호출 성공" src="https://github.com/user-attachments/assets/fc1a94b3-3dd0-4536-8b1c-11d12df524b6" />


### [LV 4] CI/CD 배포 현황

* **GitHub Actions 성공:**
  
* **EC2 Docker 실행:**
<img width="613" height="154" alt="도커 배포중" src="https://github.com/user-attachments/assets/8b688394-0740-4de3-b3d5-17972f208fe2" />
<img width="923" height="730" alt="도커 배포 성공" src="https://github.com/user-attachments/assets/64f74ef8-0994-4127-90e7-1662f4c653ff" />
<img width="1080" height="104" alt="도커 배포 성공 터미널" src="https://github.com/user-attachments/assets/1504d4eb-f32c-4ab2-a05c-ba11854c9075" />


### [LV 6] CloudFront CDN 적용 결과

* **최종 이미지 URL:** [https://d2q468e029nfe.cloudfront.net/파일명](https://www.google.com/search?q=https://d2q468e029nfe.cloudfront.net/%ED%8C%8C%EC%9D%BC%EB%AA%85)
* **브라우저 확인:**
  <img width="760" height="639" alt="CDN 이용 프로필 사진 호출 성공" src="https://github.com/user-attachments/assets/b4ea75d7-7846-447d-b33f-ccd742f547d7" />
