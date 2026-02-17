<div align="center">

# 🚍 Momen – AI 학습 플래너 & DevOps 관리 플랫폼

**멘티 학습 플래너부터 Jenkins·OpenAI 연동까지 한 번에 관리하는 Spring Boot 기반 플랫폼**

[![Java](https://img.shields.io/badge/Java-21-007396?style=flat-square&logo=openjdk&logoColor=white)](https://adoptium.net/)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.5.9-6DB33F?style=flat-square&logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![MariaDB](https://img.shields.io/badge/MariaDB-11-003545?style=flat-square&logo=mariadb&logoColor=white)](https://mariadb.org/)
[![Redis](https://img.shields.io/badge/Redis-Cache-DC382D?style=flat-square&logo=redis&logoColor=white)](https://redis.io/)
[![Docker](https://img.shields.io/badge/Docker-Compose-2496ED?style=flat-square&logo=docker&logoColor=white)](https://www.docker.com/)
[![Jenkins](https://img.shields.io/badge/Jenkins-CI%2FCD-D24939?style=flat-square&logo=jenkins&logoColor=white)](https://www.jenkins.io/)
[![Prometheus](https://img.shields.io/badge/Prometheus-Metrics-E6522C?style=flat-square&logo=prometheus&logoColor=white)](https://prometheus.io/)
[![Grafana](https://img.shields.io/badge/Grafana-Dashboards-F46800?style=flat-square&logo=grafana&logoColor=white)](https://grafana.com/)
[![Resilience4j](https://img.shields.io/badge/Resilience4j-Circuit_Breaker-00897B?style=flat-square)](https://resilience4j.readme.io/)

</div>

---

## ✨ 주요 기능

|  | 기능 |
|---|------|
| 📚 | `StudyController` · `TodoService` 기반 **학습 Todo/타이머/통계 관리** |
| 🧑‍🎓 | 멘티 마이페이지(`MypageResponse`) – 최근 30일 성취율, 과목별 완료율, 총 학습 시간, 멘토 정보 제공 |
| 📝 | 과제 제출(`AssignmentService`) 및 오답노트(`MistakeNoteService`) 관리 |
| 💬 | 멘토–멘티 챗(`MentoringController`, `MentoringChatService`) 및 응원 메시지·특징 카드 관리 |
| 🔐 | `AuthController`·`AuthService` + `SecurityConfig` 로 구성된 **JWT 기반 인증/인가** |
| 🧠 | `OpenAIController`·`OpenAiClient`를 통한 ChatGPT/DALL-E 이미지 생성 및 로그 관리 |
| 🔔 | `NotificationController`·`NotificationService` + `SseEmitterManager` 기반 실시간 알림 |
| 🎯 | `FocusController`·`FocusService` – 집중 세션(포커스 타이머) 기록 및 통계 |
| 🧾 | `FileController`·`FileStorageService` 기반 학습 자료·첨부파일 업로드/다운로드 |
| 🛠 | `AdminController`·`AdminService` – 관리자용 멘티/과제/피드백 관리 API |
| 🧵 | `GlobalExceptionHandler`·`ErrorCode` – 일관된 에러 응답 포맷 제공 |
| ⚙️ | `JenkinsController`·`JenkinsServiceImpl`를 통한 Jenkins Job/빌드/노드 관리 API |
| 📊 | Actuator + Prometheus 메트릭, Redis 캐시, Zipkin(Brave) 기반 관측성 인프라 |

---

## 🛠 기술 스택

<table>
<tr>
<td width="50%">

#### 💻 백엔드 (코드 구조 기준)

| 기술 | 설명 |
|------|------|
| ![Java](https://img.shields.io/badge/Java_21-007396?style=flat-square&logo=openjdk&logoColor=white) | 런타임 (JDK 21, Gradle 기반) |
| ![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.5.9-6DB33F?style=flat-square&logo=springboot&logoColor=white) | 애플리케이션 프레임워크 |
| ![Spring Web](https://img.shields.io/badge/Spring_Web-REST_API-6DB33F?style=flat-square&logo=spring&logoColor=white) | REST API (멘티/멘토/관리자/알림 등) |
| ![Spring Security](https://img.shields.io/badge/Spring_Security-Auth-6DB33F?style=flat-square&logo=springsecurity&logoColor=white) | 인증/인가, JWT 보안 |
| ![Spring Data JPA](https://img.shields.io/badge/Spring_Data_JPA-ORM-59666C?style=flat-square&logo=hibernate&logoColor=white) | JPA 기반 데이터 접근 |
| ![MyBatis](https://img.shields.io/badge/MyBatis-3.0.5-000000?style=flat-square) | 세밀한 SQL 매핑 |
| ![QueryDSL](https://img.shields.io/badge/QueryDSL-5.0.0-009688?style=flat-square) | 타입 세이프 쿼리 |
| ![Thymeleaf](https://img.shields.io/badge/Thymeleaf-View-005F0F?style=flat-square&logo=thymeleaf&logoColor=white) | 서버사이드 템플릿 |
| ![MariaDB](https://img.shields.io/badge/MariaDB-11-003545?style=flat-square&logo=mariadb&logoColor=white) | 메인 데이터베이스 |
| ![Redis](https://img.shields.io/badge/Redis-Cache-DC382D?style=flat-square&logo=redis&logoColor=white) | 캐시 · 세션 저장소 |
| ![Batch](https://img.shields.io/badge/Spring_Batch-Job-607D8B?style=flat-square) | 배치 처리 인프라 (MVP 단계, 비활성 설정) |

</td>
<td width="50%">

#### 🏗 인프라 · 관측성 · 보안 (실제 설정 기준)

| 기술 | 역할 |
|------|------|
| ![Docker](https://img.shields.io/badge/Docker-Container-2496ED?style=flat-square&logo=docker&logoColor=white) | 애플리케이션 컨테이너 |
| ![docker-compose](https://img.shields.io/badge/docker--compose-Stack-2496ED?style=flat-square&logo=docker&logoColor=white) | 앱·Redis 스택 구성 |
| ![Jenkins](https://img.shields.io/badge/Jenkins-CI%2FCD-D24939?style=flat-square&logo=jenkins&logoColor=white) | 빌드 & 배포 자동화 |
| ![Prometheus](https://img.shields.io/badge/Prometheus-Metrics-E6522C?style=flat-square&logo=prometheus&logoColor=white) | 메트릭 수집 (Actuator) |
| ![Grafana](https://img.shields.io/badge/Grafana-Dashboard-F46800?style=flat-square&logo=grafana&logoColor=white) | 대시보드 시각화 |
| ![Resilience4j](https://img.shields.io/badge/Resilience4j-Retry%2FCircuitBreaker-00897B?style=flat-square) | Kakao 등 외부 API 호출 탄력성 (Retry/CircuitBreaker) |
| ![Zipkin](https://img.shields.io/badge/Zipkin-Brave_Tracing-FF9800?style=flat-square) | Brave 기반 분산 추적 연동 |
| ![OWASP](https://img.shields.io/badge/OWASP-Dependency_Check-000000?style=flat-square) | 의존성 취약점 검사 |
| ![OpenAI](https://img.shields.io/badge/OpenAI-API-412991?style=flat-square&logo=openai&logoColor=white) | 텍스트·임베딩 생성 |
| ![Gemini](https://img.shields.io/badge/Gemini-API-4285F4?style=flat-square&logo=google&logoColor=white) | LLM 연동 |
| ![Imagen](https://img.shields.io/badge/Imagen_3-Image_Generation-34A853?style=flat-square&logo=google&logoColor=white) | 이미지 생성 |

</td>
</tr>
</table>

---

## 🚀 시작하기

### 📋 사전 요구사항

- **Java 21** (JDK)
- **Gradle** (Wrapper `./gradlew` 사용 권장)
- **MariaDB** (로컬 또는 외부 인스턴스)
- **Redis** (로컬 실행 시)
- **Docker & Docker Compose** (배포/통합 실행 시)

---

### ⚙️ Docker로 실행 (권장 – `docker-compose.yml` 기준)

```bash
git clone <repository-url>
cd blaybus

# (선택) 환경 변수 예시
cp .env.example .env   # 없다면 아래 예시를 참고해 직접 생성

# 애플리케이션 빌드 & 이미지 생성
./gradlew clean bootJar
docker build -t momen .

# 전체 스택 실행 (app + redis)
docker-compose up -d
```

#### 🔧 `.env` 예시

```bash
DB_HOST=localhost
DB_USERNAME=momen_user
DB_PASSWORD=momen_password
JWT_SECRET=your-jwt-secret
OPENAI_API_KEY=sk-...
GEMINI_API_KEY=your-gemini-key
GEMINI_PROJECT_ID=your-gcp-project
IMAGEN_PROJECT_ID=your-gcp-imagen-project
MAIL_PASSWORD=your-mail-app-password
TZ=Asia/Seoul
```

`docker-compose.yml` 에서는 `DB_HOST`, `DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET`, `OPENAI_API_KEY`, `MAIL_PASSWORD` 등을 읽어 MariaDB 및 외부 API에 연결합니다.

#### 🔗 서비스 접근 (Docker 실행 시)

| 서비스 | URL |
|--------|-----|
| 🌐 애플리케이션 | `http://localhost:8089` |
| 📚 Swagger UI | `http://localhost:8089/api/v1/swagger-ui.html` |
| 🩺 Actuator Health | `http://localhost:8089/actuator/health` |
| 📈 Prometheus Scrape | `http://localhost:8089/actuator/prometheus` |

---

### 💻 로컬에서 실행 (Docker 없이 – Spring Boot 단독)

**Spring Boot 애플리케이션만** 로컬에서 실행하는 방식입니다. Redis · Prometheus · Grafana 등은 별도 구성입니다.

**필요 조건**

- 로컬 MariaDB에 `momen` 데이터베이스 생성
- `application.properties` 또는 환경 변수로 DB 계정 정보 설정

#### 1) 데이터베이스 생성

```bash
# 기본값 (storyg → 필요에 맞게 변경)
chmod +x setup-database.sh
./setup-database.sh
```

또는 수동으로 MariaDB에서 데이터베이스 및 사용자를 생성한 뒤, `src/main/resources/application.properties` 의 `spring.datasource.*` 설정을 수정합니다.

#### 2) 애플리케이션 실행

```bash
# 프로젝트 루트에서
./gradlew clean bootRun
```

| 항목 | 로컬 실행 시 |
|------|--------------|
| 앱 | `http://localhost:8080` |
| Swagger UI | `http://localhost:8080/api/v1/swagger-ui.html` |
| Actuator Health | `http://localhost:8080/actuator/health` |

> ⚠️ MariaDB 및 (선택) Redis가 실행 중이 아니면 애플리케이션 기동에 실패할 수 있습니다.

---

## 🔌 주요 도메인 & API 개요

### 📚 Study / Planner (멘티 학습)

- **주요 클래스**
  - 컨트롤러: `StudyController`
  - 서비스: `TodoService`, `AssignmentService`, `MistakeNoteService`, `PlannerService`, `TodoFeedbackService`, `WeeklyFeedbackService`, `MonthlyFeedbackService`
  - JPA: `TodoRepository`, `AssignmentSubmissionRepository`, `MistakeNoteRepository`, `WeeklyFeedbackRepository`, `MonthlyFeedbackRepository` 등
- **기능 요약**
  - 일/주/월 단위 Todo 조회 및 카드 뷰
  - Todo 생성/수정/삭제, 학습 시간 누적(타이머 값 반영)
  - 과제 제출(텍스트+파일), 제출물 조회
  - 오답노트 생성 및 AI 변형 문제 생성 트리거
  - 당일/기간별 학습시간 통계, 마이페이지 성취율·과목별 완료율 집계

### 🙋‍♀️ Auth & User

- **주요 클래스**
  - `AuthController`, `AuthService`, `EmailVerificationController`, `EmailVerificationService`
  - `UserController`, `UserService`, `User`, `UserRepository`
  - `SecurityConfig`, `JwtAuthenticationFilter`, `JwtTokenProvider`, `CustomUserDetailsService`
- **기능 요약**
  - 회원가입/로그인/토큰 재발급/로그아웃(JWT)
  - 이메일 인증 및 인증 코드 관리(Redis)
  - 사용자 프로필 조회/수정
  - Spring Security + JWT 기반 Stateless 인증

### 🧑‍🏫 Mentoring

- **주요 클래스**
  - `MentoringController`, `MentoringService`, `MentoringChatService`
  - `Mentor`, `Mentee`, `MentoringChatLog`, 각종 JPA 리포지토리
- **기능 요약**
  - 멘토–멘티 매핑 및 멘티 정보 조회
  - 응원 메시지/특징 카드 관리
  - 멘토링 채팅 로그 관리

### 🧠 OpenAI 연동

- **주요 클래스**
  - `OpenAIController`, `OpenAIChatService`, `DalleGenerationService`
  - `OpenAiClient`, `AiClient`, `MockAiClient`
  - `OpenAIChatLog`, `DalleGenerationLog`
- **기능 요약**
  - ChatGPT 테스트 및 정식 챗 API (Job ID 기반 로그 저장/조회)
  - DALL-E 이미지 생성 요청 및 결과 로그 관리

### 🔔 Notification & Focus

- **Notification**
  - `NotificationController`, `NotificationService`, `NotificationScheduler`
  - `Notification`, `NotificationType`, `NotificationRepository`
  - `SseEmitterManager` 를 이용한 **SSE 기반 실시간 알림**
- **Focus**
  - `FocusController`, `FocusService`, `FocusSession`, `FocusSessionRepository`
  - 집중 세션(포커스 타이머) 기록 및 분석

### 🛠 Admin & Jenkins

- **Admin**
  - `AdminController`, `AdminService` – 관리자용 통계·관리 API
- **Jenkins**
  - `JenkinsController`, `JenkinsServiceImpl` – Jenkins REST API 클라이언트
  - Job 목록/생성/수정/삭제, 빌드 트리거/로그 조회, 노드 목록 조회 등 제공

---

## 📚 OpenAPI / Swagger

`springdoc-openapi` 를 통해 자동 생성된 API 문서를 제공합니다.

| 환경 | URL |
|------|-----|
| 로컬 | `http://localhost:8080/api/v1/swagger-ui.html` |
| Docker | `http://localhost:8089/api/v1/swagger-ui.html` |

### 🩺 Actuator 엔드포인트

`application.properties` 에서 다음 엔드포인트가 노출됩니다.

| Endpoint | 설명 |
|----------|------|
| `/actuator/health` | 애플리케이션 헬스 체크 |
| `/actuator/info` | 빌드/애플리케이션 정보 |
| `/actuator/metrics` | JVM · HTTP 메트릭 |
| `/actuator/prometheus` | Prometheus 스크랩용 메트릭 |

Prometheus에서 `/actuator/prometheus` 를 스크랩 대상으로 등록하고, Grafana에서 Prometheus를 데이터 소스로 추가하면 JVM/HTTP/비즈니스 메트릭을 대시보드로 시각화할 수 있습니다.

---

## 📁 프로젝트 구조 (패키지 기준)

```bash
├── src
│   ├── main
│   │   ├── java
│   │   │   └── com/momen/
│   │   │       ├── MomenApplication.java      # Spring Boot 진입점
│   │   │       ├── application/              # 도메인별 서비스 (planner, auth, user, mentoring, openai, admin, notification 등)
│   │   │       ├── core/                     # 공통 설정, 예외 처리, Swagger, Jenkins 연동 등
│   │   │       ├── domain/                   # 도메인 모델 (user, planner, mentoring, openai, notification 등)
│   │   │       ├── infrastructure/           # JPA, Security, Redis, 외부 OpenAI 클라이언트, SSE, Jenkins 등
│   │   │       └── presentation/             # REST 컨트롤러 (auth, user, planner, mentoring, openai, admin, notification 등)
│   │   └── resources
│   │       ├── application.properties        # 기본 환경 설정
│   │       ├── application-*.properties      # 프로필별 설정
│   │       ├── templates/                    # Thymeleaf 템플릿
│   │       └── static/                       # 정적 리소스
├── config/
│   └── dependency-check-suppressions.xml     # OWASP Dependency Check 예외 규칙
├── docs/
│   ├── TECH_STACK_OBSERVABILITY_AND_SECURITY.md  # 관측성·보안 상세 설명
│   └── JENKINS_DEPLOY.md                         # Jenkins 배포 가이드
├── docker-compose.yml                        # app + redis 스택 정의
├── Dockerfile                                # 애플리케이션 컨테이너 빌드
├── Jenkinsfile                               # CI/CD 파이프라인 정의
├── setup-database.sh                         # MariaDB 초기 설정 스크립트
└── build.gradle                              # Gradle 빌드 스크립트
```

---

## 📊 관측성 (Observability)

| 영역 | 내용 |
|------|------|
| **메트릭** | Actuator + Micrometer + Prometheus를 통해 JVM, HTTP 요청, DB, Redis, 사용자 정의 메트릭 수집 |
| **헬스 체크** | DB, Redis 등 의존성 상태를 `health` 엔드포인트로 노출, K8s Liveness/Readiness Probe에 활용 가능 |
| **로깅** | `logs/Momen.log` 로 파일 로깅, 로그 패턴·레벨을 `application.properties` 에서 제어 |
| **추적** | Brave/Zipkin 연동으로 분산 트레이싱 환경과 연계 가능 (스팬·트레이스 ID 기반 요청 추적) |

자세한 내용은 `docs/TECH_STACK_OBSERVABILITY_AND_SECURITY.md` 를 참고하세요.

---

## 🔒 보안

- **Spring Security + JWT**
  - 로그인 시 Access/Refresh Token 발급
  - 토큰 만료 시간 (`jwt.access-token-validity`, `jwt.refresh-token-validity`) 설정 가능
- **OWASP Dependency Check**
  - Gradle 플러그인 `org.owasp.dependencycheck` 사용
  - `./gradlew dependencyCheckAnalyze` 로 취약점 스캔
  - `config/dependency-check-suppressions.xml` 로 허용할 CVE 관리
- **환경 변수 기반 비밀 관리**
  - DB 비밀번호, JWT 시크릿, OpenAI/Gemini/Imagen API Key, SMTP 비밀번호 등은 `.env` 또는 환경 변수 사용 권장
- **네트워크 & 접근 제어**
  - 프로파일(`local`, `dev`, `prod`) 별 설정으로 포트, 로깅, 보안 정책 분리 가능

---

## 🤝 기여하기

1. 저장소를 **Fork** 합니다.
2. 기능 브랜치를 생성합니다. (`git checkout -b feature/amazing-feature`)
3. 변경 사항을 커밋합니다. (`git commit -m 'Add some amazing feature'`)
4. 브랜치를 원격 저장소에 푸시합니다. (`git push origin feature/amazing-feature`)
5. Pull Request를 생성합니다.

---

## 📄 라이선스

이 프로젝트의 라이선스는 **팀/조직 정책**에 따라 관리됩니다.  
외부 공개 또는 오픈소스 전환 시 별도의 `LICENSE` 파일로 공지될 예정입니다.

---

## 📞 연락처

**프로젝트 관리자** — [dhxogns920@gmail.com](mailto:dhxogns920@gmail.com)

