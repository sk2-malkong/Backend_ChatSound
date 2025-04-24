# Backend_Purgo
- ‘Purgo’(라틴어로 정화하다) -> 온라인 커뮤니티 속 비속어들을 필터링 하여 정제된 깨끗한 말로 바꿔 세상을 정화해나간다는 의미

## 프로젝트 소개
- 시연용 커뮤니티 backend code

## Member
- 송보민(PL)   :  마이페이지, DB 연결(MySQL, Redis), JWT, 초기 설정 보수, ERD
- 구강현       :  사용자 관리, JWT, 초기 설정, ERD, Query, 문서 작성 및 정리(Notion)
- 정혜지       :  게시글 CRUD, 검색, ERD, Query
- 이은비(보조)  :  댓글 CRUD, ERD

## 기본 설정
- application.properties 파일 생성
- 노션 -> 백엔드 설정/application.properties 참고

## 로컬 Docker 설치
- docker run --name mysql-db -e MYSQL_ROOT_PASSWORD=1234 -p 3306:3306 -d mysql:8.4
- docker run -d --name redis -p 6379:6379 redis:7-alpine

## 프로젝트 기간
- 2025.03.26 ~ 2025.06.05

----

## 프레임워크

| 분류               | 사용 기술                | 설명                                                         |
|--------------------|--------------------------|--------------------------------------------------------------|
| **백엔드 프레임워크** | Spring Boot              | REST API, 보안, JPA 등 통합 프레임워크                        |
| **보안**            | Spring Security + JWT    | 인증/인가 처리 (Access/Refresh 토큰 기반)                    |
| **데이터베이스**     | MySQL, Redis             | RDBMS 및 토큰 저장/블랙리스트 처리용 인메모리 캐시 DB        |
| **ORM**            | JPA (Hibernate)          | 객체-관계 매핑을 통해 DB와 연동                              |
| **이메일 발송**      | Spring Mail              | 회원가입 환영 이메일 발송                                    |
| **설정 관리**        | application.properties    | JWT, DB, 메일 등 설정 정보 관리                              |

---

## 툴체인

| 분류               | 사용 기술                | 설명                                                         |
|--------------------|--------------------------|--------------------------------------------------------------|
| **IDE**            | IntelliJ IDEA            | Java 및 Spring Boot 개발에 최적화된 통합 개발 환경           |
| **빌드 도구**       | Gradle                   | 프로젝트 빌드 및 의존성 관리 자동화 도구                     |
| **버전 관리**       | Git + GitHub             | 소스 코드 이력 관리 및 협업 도구                             |
| **테스트 도구**     | Postman                  | REST API 테스트 및 문서화                                    |
| **기타 라이브러리** | Lombok                   | Getter, Setter, Builder 자동 생성                            |
| **JDK**            | Java 17                  | Spring 애플리케이션 런타임 환경                              |
| **DB 툴**          | DBeaver                  | MySQL RDS 접속 및 쿼리 테스트                                |
| **인프라 관리**     | AWS Console              | EC2, RDS, ElastiCache, S3 구성 및 모니터링 도구              |


---

## 프로그램 구성
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
        │               │   ├── RedisConfig.java
        │               │   └── SecurityConfig.java
        │               ├── controller/
        │               │   ├── AuthController.java
        │               │   ├── UserController.java
        │               │   ├── PostController.java
        │               │   └── CommentController.java
        │               ├── dto/
        │               │   ├── AuthDto.java
        │               │   ├── JwtDto.java
        │               │   ├── PostDto.java
        │               │   └── CommentDto.java
        │               ├── entity/
        │               │   ├── BadwordLogEntity.java
        │               │   ├── CommentEntity.java
        │               │   ├── PenaltyCountEntity.java
        │               │   ├── PostEntity.java
        │               │   ├── UserEntity.java
        │               │   └── UserLimitsEntity.java
        │               ├── repository/
        │               │   ├── AuthRepository.java
        │               │   ├── PostRepository.java
        │               │   └── CommentRepository.java
        │               ├── security/
        │               │   ├── CustomUserDetails.java
        │               │   └── JwtAuthorizationFilter.java
        │               └── service/
        │                   ├── AuthService.java
        │                   ├── JwtService.java
        │                   ├── RedisService.java
        │                   ├── UserDetailsServiceImpl.java
        │                   ├── UserService.java
        │                   ├── PostService.java
        │                   └── CommentService.java
        └── resources/
            └── application.properties
```


---

## API
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