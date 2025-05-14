![header](https://capsule-render.vercel.app/api?type=waving&color=gradient&height=192&section=header&text=PURGO%20backend&fontSize=90&animation=fadeIn&fontColor=FFF)


<p align="center">
  <img src="https://img.shields.io/badge/redis-%23DC382D.svg?&style=for-the-badge&logo=redis&logoColor=white"/>
  <img src="https://img.shields.io/badge/spring-%236DB33F.svg?&style=for-the-badge&logo=spring&logoColor=white"/>
  <img src="https://img.shields.io/badge/fastapi-%23009688.svg?&style=for-the-badge&logo=fastapi&logoColor=white"/>
  <img src="https://img.shields.io/badge/gradle-%2302303A.svg?&style=for-the-badge&logo=gradle&logoColor=white"/>
  <img src="https://img.shields.io/badge/java-%23007396.svg?&style=for-the-badge&logo=java&logoColor=white"/>
  <img src="https://img.shields.io/badge/docker-%232496ED.svg?&style=for-the-badge&logo=docker&logoColor=white"/>
  <img src="https://img.shields.io/badge/mysql-%234479A1.svg?&style=for-the-badge&logo=mysql&logoColor=white"/>
    <br>
  <img src="https://img.shields.io/badge/github-%23181717.svg?&style=for-the-badge&logo=github&logoColor=white"/>
  <img src="https://img.shields.io/badge/Notion-000000?style=for-the-badge&logo=Notion&logoColor=white"/>
  <img src="https://img.shields.io/badge/postman-%23FF6C37.svg?&style=for-the-badge&logo=postman&logoColor=white"/>
  <img src="https://img.shields.io/badge/swagger-%2385EA2D.svg?&style=for-the-badge&logo=swagger&logoColor=black"/>
</p>





## 프로젝트 소개
- 시연용 커뮤니티 backend code
- 사용자가 작성하는 게시글과 댓글에서 비속어를 실시간 감지 및 대체어 변환을 통해 사용자들에게 비속어에 대한 노출을 줄이기 위해 제작
- 사용자별 비속어 사용횟수를 기록하고 누적 관리
- 사용자별 비속어 사용횟수 5회 누적 시 게시글 및 댓글 작성, 수정 제한
- FastAPI를 통해 비속어 탐지 및 대체어 변환 AI와 연동

## Member
- 송보민(PL)   :  마이페이지, DB 연결(MySQL, Redis), JWT, 초기 설정 보수, ERD , 프록시 서버 구현, ai 연동
- 구강현       :  사용자 관리, JWT, 초기 설정, ERD, Query, 문서 작성 및 정리(Notion), ai 기능 연결(욕설 카운트, 기능 제한)
- 정혜지       :  게시글 CRUD, 검색, ERD, Query , swagger
- 이은비(보조)  :  댓글 CRUD, ERD

## 기본 설정
- application.properties 파일 생성
- 노션 -> 백엔드 설정/application.properties 참고


## 로컬 Docker 설치
- Mysql
     - docker run --name mysql-db -e MYSQL_ROOT_PASSWORD=1234 -p 3306:3306 -d mysql:8.4
          - 쿼리 : query.sql

- Redis
     - docker run -d --name redis -p 6379:6379 redis:7-alpine



## 프로젝트 기간
- 2025.03.26 ~ 2025.06.05



# 툴체인 & 프레임워크

## 프레임워크

| 분류               | 사용 기술                 | 설명                                            |
|--------------------|-----------------------|--------------------------------------------------|
| **백엔드 프레임워크** | Spring Boot           | REST API, 보안, DB 연동을 스타터 의존성 기반으로 통합 구성                    |
| **보안**            | Spring Security + JWT | 필터 체인 기반 인증/인가 흐름 분리와 JWT 토큰 검증으로 세션 없는 인증 상태 유지 및 서버 부하 감소 구성               |
| **데이터베이스**     | MySQL                 | 관계형 데이터 정합성과 트랜잭션 처리에 특화된 구조                  |
| **데이터베이스**     | Redis                 | 인메모리 기반으로 고속 키-값 저장, TTL 설정, 구조 단순화에 최적화               |
| **ORM**            | JPA (Hibernate)       | 자바 객체로 DB를 직접 제어하고 유지보수성을 높이는 ORM 접근             |
| **이메일 발송**      | Spring Mail           | 인증 및 알림 메일 발송을 SMTP 설정 기반으로 간편하게 구현                                  |



## 툴체인


| 분류           | 사용 기술                | 설명                                                         |
|--------------|--------------------------|--------------------------------------------------------------|
| **IDE**      | IntelliJ IDEA            | Java 및 Spring Boot 개발에 최적화된 통합 개발 환경           |
| **빌드 도구**    | Gradle                   | 프로젝트 빌드 및 의존성 관리 자동화 도구                     |
| **버전 관리**    | Git + GitHub             | 소스 코드 이력 관리 및 협업 도구                             |
| **테스트 도구**   | Postman                  | REST API 테스트 및 문서화                                    |
| **기타 라이브러리** | Lombok                   | Getter, Setter, Builder 자동 생성                            |
| **JDK(JRE)** | Java 17                  | Spring 애플리케이션 런타임 환경                              |
| **DB 툴**     | DBeaver                  | MySQL RDS 접속 및 쿼리 테스트                                |
| **인프라 관리**   | AWS Console              | EC2, RDS, ElastiCache, S3 구성 및 모니터링 도구              |

---

## 백엔드 흐름도
<img src="image/백엔드%20흐름도.png"  width="700px">

----
## 인증 흐름
### [1] 로그인 시
```text
┌────────────┐
│클라이언트   │
└────┬───────┘
     ↓
POST /api/auth/login
     ↓
  ID/PW 인증
     ↓
  AccessToken + RefreshToken 생성
     ↓
토큰 응답 → 클라이언트 저장
```
### [2] 인증된 요청 시
```
┌────────────┐
│클라이언트   │
└────┬───────┘
     ↓
Authorization: Bearer <token>
     ↓
JwtAuthorizationFilter
  └→ 토큰 서명 검증 (라이브러리 내부)
  └→ userId(sub) 추출
  └→ DB에서 유저 조회
  └→ SecurityContext에 등록
     ↓
@AuthenticationPrincipal 접근 가능
```


---

# API
### Auth API


| 메서드 | 엔드포인트              | 설명                          |
|--------|--------------------------|-------------------------------|
| POST   | /auth/signup             | 회원가입                      |
| POST   | /auth/login              | 로그인 및 토큰 발급           |
| POST   | /auth/refresh            | 토큰 재발급                   |
| POST   | /auth/logout             | 로그아웃                      |
| GET    | /auth/checkId            | 아이디 중복 확인              |
| GET    | /auth/checkName          | 닉네임 중복 확인              |
| POST   | /auth/findId             | 이메일로 아이디 찾기          |
| POST   | /auth/resetPassword      | 비밀번호 재설정               |

---

### User API

| 메서드 | 엔드포인트                | 설명                            |
|--------|----------------------------|---------------------------------|
| GET    | /api/user/profile          | 내 프로필 조회                  |
| PUT    | /api/user/profile          | 내 프로필 수정                  |
| POST   | /api/user/profile/upload   | 프로필 이미지 업로드            |
| DELETE | /api/user/delete           | 회원 탈퇴                        |
| POST   | /api/user/penaltyCount     | 패널티 횟수 조회                 |
| POST   | /api/user/limits           | 사용자 제한 정보 조회           |

---

### Post API

| 메서드 | 엔드포인트                   | 설명                           |
|--------|-------------------------------|--------------------------------|
| GET    | /api/post/list                | 게시글 목록 조회 (페이지네이션) |
| GET    | /api/post/{postId}           | 게시글 상세 조회               |
| POST   | /api/post/create              | 게시글 작성                    |
| PUT    | /api/post/update/{postId}     | 게시글 수정                    |
| DELETE | /api/post/delete/{postId}     | 게시글 삭제                    |
| GET    | /api/post/my                  | 내가 작성한 게시글 조회        |

---

### Comment API

| 메서드 | 엔드포인트                   | 설명                           |
|--------|-------------------------------|--------------------------------|
| GET    | /api/comment/{postId}         | 특정 게시글의 댓글 조회        |
| POST   | /api/comment/{postId}         | 댓글 작성                      |
| PUT    | /api/comment/{commentId}      | 댓글 수정                      |
| DELETE | /api/comment/{commentId}      | 댓글 삭제                      |

---

### Search API

| 메서드 | 엔드포인트     | 설명                  |
|--------|----------------|-----------------------|
| GET    | /api/search    | 키워드로 게시글 검색 |

---

# 프로그램 구성
```
backend/
└── src/
    └── main/
        ├── java/
        │   └── com/
        │       └── example/
        │           └── final_backend/
        │               ├── config/
        │               │   ├── JwtConfig.java
        │               │   ├── PurgoClientConfig.java
        │               │   ├── RedisConfig.java
        │               │   ├── SecurityConfig.java
        │               │   ├── SwaggerConfig.java
        │               │   └── WebMvcConfig.java
        │               ├── controller/
        │               │   ├── AuthController.java
        │               │   ├── CommentController.java
        │               │   ├── MypageController.java
        │               │   ├── PostController.java
        │               │   └── SearchController.java
        │               ├── dto/
        │               │   ├── AuthDto.java
        │               │   ├── CommentDto.java
        │               │   ├── JwtDto.java
        │               │   ├── PostDto.java
        │               │   └── ProfileDto.java
        │               ├── entity/
        │               │   ├── BadwordLogEntity.java
        │               │   ├── CommentEntity.java
        │               │   ├── PenaltyCountEntity.java
        │               │   ├── PostEntity.java
        │               │   ├── UserEntity.java
        │               │   └── UserLimitsEntity.java
        │               ├── factory/
        │               │   └── UserFactory.java
        │               ├── repository/
        │               │   ├── BadwordLogRepository.java
        │               │   ├── CommentRepository.java
        │               │   ├── PenaltyCountRepository.java
        │               │   ├── PostRepository.java
        │               │   ├── UserLimitsRepository.java
        │               │   └── UserRepository.java
        │               ├── security/
        │               │   ├── CustomUserDetails.java
        │               │   └── JwtAuthorizationFilter.java
        │               └── service/
        │                   ├── AsyncService.java
        │                   ├── AuthService.java
        │                   ├── CheckBadwordService.java
        │                   ├── CommentService.java
        │                   ├── JwtService.java
        │                   ├── MypageService.java
        │                   ├── PostService.java
        │                   ├── RedisService.java
        │                   ├── ServerToProxyJwtService.java
        │                   ├── UserDetailsServiceImpl.java
        │                   └── UserPenaltyService.java
        └── resources/
            ├── application.properties
            └── dummy.txt
```


---