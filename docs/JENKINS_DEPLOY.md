# Jenkins 배포 파이프라인 가이드

## 개요

- **Jenkinsfile**: Declarative Pipeline (Checkout → Build → Test → Package → Docker Build/Push → Deploy)
- **Dockerfile**: Multi-stage (Gradle 빌드 → JRE 런타임)
- **Java**: 21 (Eclipse Temurin)

## 파이프라인 단계

| 단계 | 설명 |
|------|------|
| Checkout | 지정 브랜치 체크아웃 |
| Build | `./gradlew clean build -x test` |
| Test | `./gradlew test` (SKIP_TESTS=false 시) |
| Package | `./gradlew bootJar`, JAR 아카이브 |
| Docker Build | Docker 이미지 빌드 (선택) |
| Docker Push | 레지스트리 푸시 (DOCKER_REGISTRY 설정 시) |
| Deploy | 환경별 배포 훅 (실제 배포는 여기서 확장) |

## 파라미터

| 파라미터 | 기본값 | 설명 |
|----------|--------|------|
| BRANCH | develop | 빌드할 브랜치 |
| ENVIRONMENT | dev | 배포 환경 (dev/staging/prod) |
| SKIP_TESTS | false | 테스트 건너뛰기 |
| DOCKER_BUILD | true | Docker 이미지 빌드 여부 |
| DOCKER_REGISTRY | (빈 값) | Docker 레지스트리 (예: `registry.example.com`) |

## Jenkins 설정

### 1. Pipeline Job 생성

- **New Item** → **Pipeline**
- **Pipeline** → Definition: **Pipeline script from SCM**
- SCM: Git, Repository URL, Credentials
- Script Path: `Jenkinsfile`

### 2. Docker 레지스트리 인증 (Push 사용 시)

- **Manage Jenkins** → **Credentials** → **Add**
- Kind: **Username and password**
- ID: `docker-registry-credentials`
- Username/Password: 레지스트리 계정

### 3. Jenkins 에이전트 요구 사항

- Java 21 (또는 동일 JDK)
- Docker 설치 및 Jenkins 사용자 권한 (`docker` 그룹)
- (선택) `kubectl` / SSH 등 배포 도구

## 로컬에서 Docker 이미지 빌드

```bash
docker build -t momen:local .
docker run -p 8080:8080 momen:local
```

## 배포 단계 확장 예시

`Deploy` 스테이지에서 환경별로 다음처럼 확장할 수 있습니다.

- **Dev**: `scp` / `rsync`로 JAR 복사 후 `systemctl restart momen`
- **Staging/Prod**: `kubectl set image` 또는 Helm upgrade, 또는 Ansible/SSH

예 (SSH 배포):

```groovy
stage('Deploy') {
    when { expression { params.ENVIRONMENT == 'dev' } }
    steps {
        sshagent(credentials: ['deploy-ssh-key']) {
            sh '''
                scp build/libs/*.jar user@dev-server:/app/
                ssh user@dev-server "systemctl restart momen"
            '''
        }
    }
}
```

## OWASP Dependency Check

- 파이프라인에 **OWASP Dependency Check** 단계가 포함되어 있습니다.
- `./gradlew dependencyCheckAnalyze` 로 의존성 취약점(CVE) 스캔을 수행합니다.
- **CVSS ≥ 7.0** 이면 빌드 실패 (조정: `build.gradle` 의 `dependencyCheck.failBuildOnCVSS`).
- 리포트: `build/reports/dependency-check/` (HTML, JSON, SARIF). Jenkins **HTML Publisher** 플러그인으로 결과를 게시할 수 있습니다.
- 허용(무시)할 CVE: `config/dependency-check-suppressions.xml` 에 추가합니다.

## 문제 해결

- **Docker: permission denied**  
  Jenkins 사용자를 `docker` 그룹에 추가: `usermod -aG docker jenkins`
- **Gradle OOM**  
  Agent에 `GRADLE_OPTS=-Xmx1024m` 환경 변수 설정
- **테스트 실패**  
  DB/Redis 등 테스트용 인프라가 Jenkins Agent 또는 Docker에서 접근 가능한지 확인
