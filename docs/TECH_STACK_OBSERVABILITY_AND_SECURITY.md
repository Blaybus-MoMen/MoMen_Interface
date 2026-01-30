# 관측성·보안 기술 스택 요약

## 1. Resilience4j (카카오 API 호출)

- **의존성**: `io.github.resilience4j:resilience4j-spring-boot3:2.3.0`
- **적용 위치**: `KakaoApiClient` — `getToken()`, `getUserInfo()` 에 `@Retry`, `@CircuitBreaker` 적용
- **설정**: `application.properties` — `resilience4j.retry.instances.kakaoApi`, `resilience4j.circuitbreaker.instances.kakaoApi`
  - Retry: 최대 3회, 대기 500ms, ConnectException/Timeout/WebClientException 시 재시도
  - CircuitBreaker: 슬라이딩 윈도우 10, 실패율 50% 이상 시 Open, 30초 후 Half-Open
- **효과**: 카카오 API 일시 장애 시 재시도, 연속 실패 시 서킷 오픈으로 장애 전파 완화

## 2. Actuator · Prometheus

- **이미 적용**: `spring-boot-starter-actuator`, `micrometer-registry-prometheus`
- **노출 엔드포인트**: `health`, `info`, `metrics`, `prometheus`
- **추가 설정**: `management.health.redis.enabled=true`, `management.health.db.enabled=true`, liveness/readiness (K8s 프로브용)
- **Prometheus**: `management.metrics.export.prometheus.enabled=true` — 스크래핑 URL 예: `/actuator/prometheus`
- **Grafana**: Prometheus를 데이터 소스로 추가 후 JVM/HTTP 메트릭 대시보드 구성 가능

## 3. OWASP Dependency Check

- **Gradle 플러그인**: `org.owasp.dependencycheck` 11.1.0
- **실행**: `./gradlew dependencyCheckAnalyze`
- **설정**: `build.gradle` — `failBuildOnCVSS = 7.0`, 리포트 경로 `build/reports/dependency-check/`
- **Jenkins**: 파이프라인에 **OWASP Dependency Check** 단계 포함, HTML Publisher로 리포트 게시
- **Suppression**: `config/dependency-check-suppressions.xml` 에 허용할 CVE 추가
