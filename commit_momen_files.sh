#!/usr/bin/env bash
# com.momen 파일별 상세 커밋 스크립트
set -e
cd /Users/sirious920/Desktop/DevOps_RosieOh/Project/blaybus

get_msg() {
  local f="$1"
  local name="${f##*/}"
  name="${name%.java}"
  case "$f" in
    *MomenApplication.java) echo "FEAT : Spring Boot 애플리케이션 진입점(Main) 추가 - com.momen 패키지 기동 클래스";;
    *application/auth/AuthService.java) echo "FEAT : 인증 서비스 추가 - 이메일 회원가입·로그인·JWT 발급·리프레시 토큰 처리";;
    *application/auth/EmailVerificationService.java) echo "FEAT : 이메일 인증 서비스 추가 - 인증 코드 발송·검증·만료 처리";;
    *application/auth/dto/LoginRequest.java) echo "FEAT : 로그인 요청 DTO 추가 - 이메일·비밀번호 필드";;
    *application/auth/dto/RefreshTokenRequest.java) echo "FEAT : 리프레시 토큰 요청 DTO 추가 - 액세스 토큰 재발급 요청";;
    *application/auth/dto/SignupRequest.java) echo "FEAT : 회원가입 요청 DTO 추가 - 이메일·비밀번호·닉네임 등";;
    *application/auth/dto/TokenResponse.java) echo "FEAT : 토큰 응답 DTO 추가 - 액세스·리프레시 토큰·만료 정보";;
    *application/common/PromptTranslationService.java) echo "FEAT : 공통 프롬프트 번역 서비스 추가 - 다국어/키워드 변환";;
    *application/oauth/KakaoApiClient.java) echo "FEAT : 카카오 OAuth API 클라이언트 추가 - 토큰·사용자정보 API 호출";;
    *application/oauth/KakaoOAuthService.java) echo "FEAT : 카카오 OAuth 서비스 추가 - 로그인 URL·토큰·연동 해제 처리";;
    *application/oauth/OAuthConnectionService.java) echo "FEAT : OAuth 연동 서비스 추가 - 연동 상태 조회·해제 오케스트레이션";;
    *application/oauth/dto/KakaoLoginRequest.java) echo "FEAT : 카카오 로그인 요청 DTO 추가 - 인가코드·리다이렉트 URI";;
    *application/oauth/dto/KakaoLoginUrlResponse.java) echo "FEAT : 카카오 로그인 URL 응답 DTO 추가 - 인증 페이지 URL";;
    *application/oauth/dto/KakaoTokenResponse.java) echo "FEAT : 카카오 토큰 응답 DTO 추가 - 액세스/리프레시 토큰";;
    *application/oauth/dto/KakaoUserInfo.java) echo "FEAT : 카카오 사용자 정보 DTO 추가 - 프로필·이메일 등";;
    *application/oauth/dto/OAuthConnectionsResponse.java) echo "FEAT : OAuth 연동 목록 응답 DTO 추가 - 연동된 제공자 목록";;
    *application/oauth/dto/OAuthLoginResponse.java) echo "FEAT : OAuth 로그인 응답 DTO 추가 - 토큰·사용자 정보";;
    *application/openai/DalleGenerationService.java) echo "FEAT : DALL-E 이미지 생성 서비스 추가 - 프롬프트 기반 이미지 생성·로그";;
    *application/openai/OpenAIChatService.java) echo "FEAT : OpenAI 채팅 서비스 추가 - ChatGPT API 호출·대화 로그 저장";;
    *application/openai/OpenAIClient.java) echo "FEAT : OpenAI 애플리케이션 클라이언트 추가 - 채팅·임베딩 요청 래핑";;
    *application/openai/dto/ChatGPTRequest.java) echo "FEAT : ChatGPT 요청 DTO 추가 - 메시지·모델·옵션";;
    *application/openai/dto/ChatGPTResponse.java) echo "FEAT : ChatGPT 응답 DTO 추가 - 선택 메시지·사용량";;
    *application/openai/dto/DalleGenerateRequest.java) echo "FEAT : DALL-E 생성 요청 DTO 추가 - 프롬프트·크기·개수";;
    *application/openai/dto/DalleGenerateResponse.java) echo "FEAT : DALL-E 생성 응답 DTO 추가 - 이미지 URL·메타데이터";;
    *application/openai/dto/SimpleChatRequest.java) echo "FEAT : 단순 채팅 요청 DTO 추가 - 메시지 단일 필드";;
    *application/openai/dto/SimpleChatResponse.java) echo "FEAT : 단순 채팅 응답 DTO 추가 - 응답 텍스트";;
    *application/user/UserService.java) echo "FEAT : 사용자 서비스 추가 - 프로필 조회·수정·온보딩·이메일/비밀번호 변경";;
    *application/user/dto/OnboardingRequest.java) echo "FEAT : 온보딩 요청 DTO 추가 - 최초 설정 정보";;
    *application/user/dto/UpdateEmailRequest.java) echo "FEAT : 이메일 변경 요청 DTO 추가";;
    *application/user/dto/UpdatePasswordRequest.java) echo "FEAT : 비밀번호 변경 요청 DTO 추가";;
    *application/user/dto/UpdateProfileRequest.java) echo "FEAT : 프로필 수정 요청 DTO 추가 - 닉네임 등";;
    *application/user/dto/UserResponse.java) echo "FEAT : 사용자 응답 DTO 추가 - 프로필·역할·OAuth 연동 정보";;
    *core/annotation/LogExecutionTime.java) echo "REFACTOR : 실행 시간 로깅 어노테이션 추가 - AOP 메서드 실행 시간 측정";;
    *core/annotation/LogMethod.java) echo "REFACTOR : 메서드 로깅 어노테이션 추가 - 입출력 로깅";;
    *core/annotation/Retryable.java) echo "REFACTOR : 재시도 어노테이션 추가 - 실패 시 재시도 정책";;
    *core/aop/AopConfig.java) echo "REFACTOR : AOP 설정 추가 - 어노테이션 기반 애스펙트 활성화";;
    *core/aspect/LoggingAspect.java) echo "REFACTOR : 로깅 애스펙트 추가 - LogMethod·LogExecutionTime 처리";;
    *core/aspect/RetryAspect.java) echo "REFACTOR : 재시도 애스펙트 추가 - Retryable 재시도 로직";;
    *core/config/AppConfig.java) echo "CHORE : 애플리케이션 공통 설정 빈 추가";;
    *core/config/CacheConfig.java) echo "CHORE : 캐시 설정 추가 - Redis 등 캐시 매니저";;
    *core/config/GlobalMapperConfig.java) echo "CHORE : MapStruct 글로벌 매퍼 설정 추가";;
    *core/config/ScheduleConfig.java) echo "CHORE : 스케줄 설정 추가 - 스케줄러 활성화";;
    *core/config/SwaggerConfig.java) echo "CHORE : Swagger(OpenAPI) 설정 추가 - API 문서화";;
    *core/config/TransactionConfig.java) echo "CHORE : 트랜잭션 설정 추가 - 트랜잭션 매니저";;
    *core/constants/CommonConstants.java) echo "REFACTOR : 공통 상수 추가 - 앱 전역 상수 정의";;
    *core/controller/BaseController.java) echo "REFACTOR : 컨트롤러 공통 베이스 추가 - ApiResponse 래핑·유틸";;
    *core/dto/request/ApiRequest.java) echo "REFACTOR : API 요청 공통 DTO 추가";;
    *core/dto/response/ApiResponse.java) echo "REFACTOR : API 응답 공통 DTO 추가 - 성공/실패 래핑";;
    *core/entity/BaseTimeEntity.java) echo "REFACTOR : JPA Auditing 베이스 엔티티 추가 - createdAt·updatedAt";;
    *core/error/dto/ErrorResponse.java) echo "REFACTOR : 에러 응답 DTO 추가 - 에러코드·메시지";;
    *core/error/enums/ErrorCode.java) echo "REFACTOR : 에러 코드 열거형 추가 - 비즈니스·인증·시스템 에러";;
    *core/exception/AuthenticationException.java) echo "REFACTOR : 인증 예외 클래스 추가";;
    *core/exception/BusinessException.java) echo "REFACTOR : 비즈니스 예외 클래스 추가";;
    *core/exception/ResourceNotFoundException.java) echo "REFACTOR : 리소스 없음 예외 클래스 추가";;
    *core/handler/GlobalExceptionHandler.java) echo "REFACTOR : 전역 예외 핸들러 추가 - @ControllerAdvice·에러 응답 변환";;
    *core/helper/MappingHelpers.java) echo "REFACTOR : 매핑 헬퍼 유틸 추가";;
    *core/jenkins/client/JenkinsClient.java) echo "FEAT : Jenkins HTTP 클라이언트 추가 - 빌드·잡·노드 API 호출";;
    *core/jenkins/config/JenkinsConfig.java) echo "CHORE : Jenkins 빈 설정 추가 - JenkinsClient·Properties";;
    *core/jenkins/config/JenkinsProperties.java) echo "CHORE : Jenkins 프로퍼티 추가 - URL·인증 등";;
    *core/jenkins/config/SecretKeyLoader.java) echo "CHORE : Jenkins 시크릿 로더 추가 - 자격증명 로드";;
    *core/jenkins/controller/JenkinsController.java) echo "FEAT : Jenkins API 컨트롤러 추가 - 빌드·잡·노드·큐 엔드포인트";;
    *core/jenkins/dto/BuildLogResponse.java) echo "FEAT : Jenkins 빌드 로그 응답 DTO 추가";;
    *core/jenkins/dto/BuildRequest.java) echo "FEAT : Jenkins 빌드 요청 DTO 추가";;
    *core/jenkins/dto/BuildResponse.java) echo "FEAT : Jenkins 빌드 응답 DTO 추가";;
    *core/jenkins/dto/JobCreateRequest.java) echo "FEAT : Jenkins 잡 생성 요청 DTO 추가";;
    *core/jenkins/dto/JobResponse.java) echo "FEAT : Jenkins 잡 응답 DTO 추가";;
    *core/jenkins/dto/NodeResponse.java) echo "FEAT : Jenkins 노드 응답 DTO 추가";;
    *core/jenkins/dto/QueueItemResponse.java) echo "FEAT : Jenkins 큐 아이템 응답 DTO 추가";;
    *core/jenkins/exception/JenkinsBuildException.java) echo "REFACTOR : Jenkins 빌드 예외 추가";;
    *core/jenkins/exception/JenkinsConnectionException.java) echo "REFACTOR : Jenkins 연결 예외 추가";;
    *core/jenkins/exception/JenkinsException.java) echo "REFACTOR : Jenkins 공통 예외 추가";;
    *core/jenkins/exception/JenkinsJobNotFoundException.java) echo "REFACTOR : Jenkins 잡 없음 예외 추가";;
    *core/jenkins/service/JenkinsService.java) echo "FEAT : Jenkins 서비스 인터페이스 추가";;
    *core/jenkins/service/JenkinsServiceImpl.java) echo "FEAT : Jenkins 서비스 구현 추가 - 빌드·잡·노드·큐 비즈니스 로직";;
    *core/jenkins/util/JenkinsUtils.java) echo "CHORE : Jenkins 유틸 추가 - URL·파라미터 처리";;
    *core/mapper/BaseMapper.java) echo "REFACTOR : MapStruct 베이스 매퍼 인터페이스 추가";;
    *core/mapper/EntityToResponseMapper.java) echo "REFACTOR : 엔티티→응답 매퍼 추가";;
    *core/mapper/OAuthConnectionMapper.java) echo "REFACTOR : OAuth 연동 매퍼 추가 - 엔티티↔응답";;
    *core/mapper/UserMapper.java) echo "REFACTOR : 사용자 매퍼 추가 - User 엔티티↔UserResponse";;
    *domain/auth/EmailVerification.java) echo "FEAT : 이메일 인증 도메인 엔티티 추가 - 인증코드·만료·상태";;
    *domain/auth/EmailVerificationRepository.java) echo "FEAT : 이메일 인증 리포지토리 인터페이스 추가";;
    *domain/auth/RefreshToken.java) echo "FEAT : 리프레시 토큰 도메인 엔티티 추가";;
    *domain/auth/RefreshTokenRepository.java) echo "FEAT : 리프레시 토큰 리포지토리 인터페이스 추가";;
    *domain/openai/DalleGenerationLog.java) echo "FEAT : DALL-E 생성 로그 도메인 엔티티 추가";;
    *domain/openai/DalleGenerationLogRepository.java) echo "FEAT : DALL-E 생성 로그 리포지토리 인터페이스 추가";;
    *domain/openai/DalleGenerationStatus.java) echo "FEAT : DALL-E 생성 상태 열거형 추가";;
    *domain/openai/OpenAIChatLog.java) echo "FEAT : OpenAI 채팅 로그 도메인 엔티티 추가";;
    *domain/openai/OpenAIChatLogRepository.java) echo "FEAT : OpenAI 채팅 로그 리포지토리 인터페이스 추가";;
    *domain/openai/OpenAIChatStatus.java) echo "FEAT : OpenAI 채팅 상태 열거형 추가";;
    *domain/user/OAuthProvider.java) echo "FEAT : OAuth 제공자 열거형 추가 - KAKAO 등";;
    *domain/user/User.java) echo "FEAT : 사용자 도메인 엔티티 추가 - 이메일·역할·OAuth 연동";;
    *domain/user/UserRepository.java) echo "FEAT : 사용자 리포지토리 인터페이스 추가";;
    *domain/user/UserRole.java) echo "FEAT : 사용자 역할 열거형 추가 - ROLE_USER 등";;
    *infrastructure/config/JavaMailConfig.java) echo "CHORE : JavaMail 설정 추가 - 이메일 발송";;
    *infrastructure/config/JpaAuditingConfig.java) echo "CHORE : JPA Auditing 설정 추가 - @EnableJpaAuditing";;
    *infrastructure/config/RedisConfig.java) echo "CHORE : Redis 설정 추가 - 연결·직렬화";;
    *infrastructure/config/WebConfig.java) echo "CHORE : Web 설정 추가 - CORS·인터셉터 등";;
    *infrastructure/config/WebSocketConfig.java) echo "CHORE : WebSocket 설정 추가";;
    *infrastructure/external/image/ImageGenerationClient.java) echo "FEAT : 이미지 생성 외부 API 클라이언트 추가 - DALL-E 등 호출";;
    *infrastructure/external/image/dto/ImageData.java) echo "FEAT : 이미지 데이터 DTO 추가";;
    *infrastructure/external/image/dto/ImageGenerationResponse.java) echo "FEAT : 이미지 생성 API 응답 DTO 추가";;
    *infrastructure/external/openai/OpenAiClient.java) echo "FEAT : OpenAI 외부 API 클라이언트 추가 - 채팅·임베딩 호출";;
    *infrastructure/external/openai/dto/ChatChoice.java) echo "FEAT : OpenAI 채팅 선택 DTO 추가";;
    *infrastructure/external/openai/dto/ChatCompletionResponse.java) echo "FEAT : OpenAI 채팅 완료 응답 DTO 추가";;
    *infrastructure/external/openai/dto/ChatMessage.java) echo "FEAT : OpenAI 채팅 메시지 DTO 추가";;
    *infrastructure/external/openai/dto/EmbeddingData.java) echo "FEAT : OpenAI 임베딩 데이터 DTO 추가";;
    *infrastructure/external/openai/dto/EmbeddingResponse.java) echo "FEAT : OpenAI 임베딩 응답 DTO 추가";;
    *infrastructure/jpa/auth/EmailVerificationJpaRepository.java) echo "FEAT : 이메일 인증 JPA 리포지토리 추가";;
    *infrastructure/jpa/auth/EmailVerificationRepositoryImpl.java) echo "FEAT : 이메일 인증 리포지토리 구현 추가";;
    *infrastructure/jpa/auth/RefreshTokenJpaRepository.java) echo "FEAT : 리프레시 토큰 JPA 리포지토리 추가";;
    *infrastructure/jpa/auth/RefreshTokenRepositoryImpl.java) echo "FEAT : 리프레시 토큰 리포지토리 구현 추가";;
    *infrastructure/jpa/user/UserJpaRepository.java) echo "FEAT : 사용자 JPA 리포지토리 추가";;
    *infrastructure/jpa/user/UserRepositoryImpl.java) echo "FEAT : 사용자 리포지토리 구현 추가";;
    *infrastructure/redis/EmailVerificationRedisService.java) echo "FEAT : 이메일 인증 Redis 서비스 추가 - 인증코드 캐시";;
    *infrastructure/redis/OAuthStateRedisService.java) echo "FEAT : OAuth state Redis 서비스 추가 - CSRF·상태 캐시";;
    *infrastructure/redis/TokenRedisService.java) echo "FEAT : 토큰 Redis 서비스 추가 - 리프레시 토큰 저장";;
    *infrastructure/security/CustomUserDetails.java) echo "FEAT : Spring Security UserDetails 구현 추가 - 인증 주체";;
    *infrastructure/security/CustomUserDetailsService.java) echo "FEAT : UserDetailsService 구현 추가 - DB 기반 사용자 로드";;
    *infrastructure/security/JwtAuthenticationFilter.java) echo "FEAT : JWT 인증 필터 추가 - 요청 헤더에서 토큰 검증";;
    *infrastructure/security/JwtTokenProvider.java) echo "FEAT : JWT 토큰 프로바이더 추가 - 발급·검증·파싱";;
    *infrastructure/security/SecurityConfig.java) echo "FEAT : Spring Security 설정 추가 - JWT·인가·CORS";;
    *infrastructure/util/PasswordUtil.java) echo "CHORE : 비밀번호 유틸 추가 - 인코딩·검증";;
    *presentation/auth/AuthController.java) echo "FEAT : 인증 API 컨트롤러 추가 - 로그인·회원가입·리프레시·로그아웃";;
    *presentation/auth/EmailVerificationController.java) echo "FEAT : 이메일 인증 API 컨트롤러 추가 - 인증코드 발송·검증";;
    *presentation/oauth/OAuthController.java) echo "FEAT : OAuth API 컨트롤러 추가 - 카카오 로그인·연동·해제";;
    *presentation/openai/OpenAIController.java) echo "FEAT : OpenAI API 컨트롤러 추가 - 채팅·DALL-E 이미지 생성";;
    *presentation/user/UserController.java) echo "FEAT : 사용자 API 컨트롤러 추가 - 프로필·온보딩·수정";;
    *MomenApplicationTests.java) echo "CHORE : Spring Boot 애플리케이션 기동 테스트 추가 - 컨텍스트 로드 검증";;
    *) echo "FEAT : com.momen 소스 추가 - $name";;
  esac
}

git show --name-only --format= cdf11ed | sort | while read -r f; do
  [ -z "$f" ] && continue
  [ ! -f "$f" ] && continue
  msg=$(get_msg "$f")
  git add "$f" && git commit -m "$msg" || exit 1
done
