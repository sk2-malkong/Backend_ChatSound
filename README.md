# Backend_Purgo

## 프로젝트 소개

## Member
- 송보민(PL)   :  
- 구강현       : 
- 정혜지       : 
- 이은비(보조) : 

## 프로젝트 기간
- 2025.03.26 ~ 2025.06.04

## 프로그램 구성
### Config
- JwtConfig.java
- RedisConfig.java
- SecurityConfig.java

### Controller
- AuthController.java
- UserController.java
- PostController.java
- CommentController.java

### Dto
- AuthDto
- JwtDto
- PostDto
- CommentDto

### Entity
- BadwordLogEntity.java
- CommentEntity.java
- PenaltyCountEntity.java
- PostEntity.java
- UserEntity.java
- UserLimitsEntity.java

### Repository
- AuthRepository.java
- PostRepository.java
- CommentRepository.java

### Security
- CustomUserDetails.java
- JwtAuthorizationFilter.java

### Service
- AuthService.java
- JwtService.java
- RedisService.java
- UserDetailsServiceImpl.java
- UserService.java
- PostService.java
- CommentService.java


---

## API
### AuthController.java
- api/auth
### 📌 인증 API 목록

| 메서드 | 엔드포인트             | 설명                           |
|--------|------------------------|--------------------------------|
| POST   | `/signup`         | 회원가입                        |
| POST   | `/login`          | 로그인                          |
| POST   | `/refresh`        | 토큰 재발급                     |
| POST   | `/logout`         | 로그아웃                        |
| GET    | `/checkId`        | ID 중복 검사                    |
| GET    | `/checkName`      | 닉네임 중복 검사                |
| POST   | `/findId`         | 이메일로 ID 찾기                |
| POST   | `/resetPassword`  | 비밀번호 재설정 (ID+이메일 인증) |


### UserController.java
### PostController.java
### CommentController.java