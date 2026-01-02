package com.blaybus.application.runway;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.blaybus.application.common.PromptTranslationService;
import com.blaybus.application.runway.dto.RunwayGenerateRequest;
import com.blaybus.application.runway.dto.RunwayGenerateResponse;
import com.blaybus.application.runway.dto.RunwayTaskStatus;
import com.blaybus.core.error.enums.ErrorCode;
import com.blaybus.core.exception.BusinessException;
import com.blaybus.domain.runway.RunwayJob;
import com.blaybus.domain.runway.RunwayJobRepository;
import com.blaybus.domain.runway.RunwayJobStatus;
import com.blaybus.domain.user.User;
import com.blaybus.domain.user.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;

/**
 * Runway Gen-3 비디오 생성 서비스
 * Runway API를 통해 텍스트 프롬프트 기반 비디오 생성
 */
@Slf4j
@Service
public class RunwayService {

    private final WebClient webClient;
    private final String apiKey;
    private final RunwayJobRepository runwayJobRepository;
    private final UserRepository userRepository;
    private final PromptTranslationService promptTranslationService;

    private static final String BASE_URL = "https://api.dev.runwayml.com";
    private static final String API_VERSION = "2024-11-06";
    private static final String TEXT_TO_VIDEO_ENDPOINT = "/v1/text_to_video";
    private static final String TASKS_ENDPOINT = "/v1/tasks";
    private static final int MAX_POLL_ATTEMPTS = 120; // 최대 120회 폴링 (10분)
    private static final Duration POLL_INTERVAL = Duration.ofSeconds(5); // 5초마다 폴링

    public RunwayService(@Value("${runway.api.key:}") String apiKey, RunwayJobRepository runwayJobRepository, UserRepository userRepository, PromptTranslationService promptTranslationService) {
        this.apiKey = apiKey;
        this.runwayJobRepository = runwayJobRepository;
        this.userRepository = userRepository;
        this.promptTranslationService = promptTranslationService;
        
        // API 키 유효성 검사 및 로깅
        if (apiKey == null || apiKey.trim().isEmpty()) {
            log.error("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            log.error("⚠️ Runway API Key가 설정되지 않았습니다!");
            log.error("   환경 변수 RUNWAY_API_KEY 또는 프로퍼티 runway.api.key를 설정해주세요.");
            log.error("   API 키가 없으면 Mock/테스트 응답이 반환될 수 있습니다.");
            log.error("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        } else {
            // API 키의 일부만 로깅 (보안)
            String maskedKey = apiKey.length() > 8 
                ? apiKey.substring(0, 8) + "..." + apiKey.substring(apiKey.length() - 4)
                : "***";
            log.info("✅ Runway API Key 설정됨: {}", maskedKey);
        }
        
        // API 키가 실제로 헤더에 전달되는지 확인
        String authHeader = "Bearer " + apiKey;
        log.info("🔑 Runway API Authorization Header: Bearer {}...{}", 
                apiKey.length() > 8 ? apiKey.substring(0, 8) : "***",
                apiKey.length() > 4 ? apiKey.substring(apiKey.length() - 4) : "");
        
        this.webClient = WebClient.builder()
                .baseUrl(BASE_URL)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.AUTHORIZATION, authHeader)
                .defaultHeader("X-Runway-Version", API_VERSION)
                .build();
    }

    // 비디오 생성 요청
    @Transactional
    public Mono<RunwayGenerateResponse> generateVideo(RunwayGenerateRequest request) {
        // 한글 프롬프트를 영어로 번역
        String originalPrompt = request.getPromptText();
        String translatedPrompt = promptTranslationService.processPrompt(originalPrompt);

        if (!originalPrompt.equals(translatedPrompt)) {
            log.info("Prompt translated from Korean to English: {} -> {}", originalPrompt, translatedPrompt);
        }

        // 사용자 조회 (nullable) - final로 선언하여 람다에서 사용 가능하도록
        final User user;
        if (request.getUserId() != null) {
            user = userRepository.findById(request.getUserId()).orElse(null);
        } else {
            user = null;
        }

        // 프롬프트도 final로 선언 (번역된 프롬프트 사용)
        final String promptText = translatedPrompt;

        // duration 값 검증 및 정규화 (4, 6, 8초만 허용)
        Integer duration = request.getDuration();
        if (duration == null || (duration != 4 && duration != 6 && duration != 8)) {
            log.warn("Invalid duration value: {}. Using default value 6 seconds.", duration);
            duration = 6;
        }

        // Runway API 요청 페이로드 구성
        Map<String, Object> requestBody = Map.of(
                "promptText", promptText,
                "model", request.getModel(),
                "ratio", request.getRatio(),
                "duration", duration,
                "audio", request.getAudio()
        );

        // 실제 API로 전송되는 요청 본문 로깅
        log.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        log.info("📤 Runway API Request:");
        log.info("   Original Prompt: {}", originalPrompt);
        log.info("   Translated Prompt: {}", promptText);
        log.info("   Model: {}", request.getModel());
        log.info("   Ratio: {}", request.getRatio());
        log.info("   Duration: {} seconds", duration);
        log.info("   Audio: {}", request.getAudio());
        log.info("   Full Request Body: {}", requestBody);
        log.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");

        return webClient.post()
                .uri(TEXT_TO_VIDEO_ENDPOINT)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class) // 먼저 문자열로 받아서 로깅
                .doOnNext(rawResponse -> {
                    log.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
                    log.info("📥 Runway API Response:");
                    log.info("   Full Response: {}", rawResponse);
                    log.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
                })
                .map(rawResponse -> {
                    try {
                        ObjectMapper mapper = new ObjectMapper();
                        Map<String, Object> response = mapper.readValue(rawResponse, Map.class);
                        String taskId = response.get("id").toString();
                        
                        // 응답에 추가 정보가 있는지 확인
                        if (response.containsKey("status")) {
                            log.info("Response status: {}", response.get("status"));
                        }
                        if (response.containsKey("mock") || response.containsKey("test")) {
                            log.warn("⚠️ Runway API가 Mock/Test 응답을 반환했습니다!");
                            log.warn("   Response: {}", rawResponse);
                        }

                        // DB에 작업 저장 (원본 프롬프트 저장)
                        RunwayJob job = RunwayJob.builder()
                                .user(user)
                                .prompt(originalPrompt) // 원본 프롬프트 저장
                                .taskId(taskId)
                                .build();
                        runwayJobRepository.save(job);
                        
                        return RunwayGenerateResponse.pending(taskId);
                    } catch (Exception e) {
                        log.error("Error parsing Runway API response: {}", e.getMessage());
                        log.error("Failed response content: {}", rawResponse);
                        throw new RuntimeException("Failed to parse response", e);
                    }
                })
                .onErrorResume(WebClientResponseException.class, e -> {
                    log.error("Runway API error: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
                    return Mono.just(RunwayGenerateResponse.failed(
                            "API 호출 실패: " + e.getMessage()
                    ));
                })
                .onErrorResume(Exception.class, e -> {
                    log.error("Unexpected error during video generation", e);
                    return Mono.just(RunwayGenerateResponse.failed(
                            "비디오 생성 요청 실패: " + e.getMessage()
                    ));
                });
    }

    // 작업 상태 조회 (단일 폴링)
    public Mono<RunwayTaskStatus> getTaskStatus(String taskId) {
        String uri = TASKS_ENDPOINT + "/" + taskId;

        return webClient.get()
                .uri(uri)
                .retrieve()
                .bodyToMono(String.class) // 먼저 문자열로 받아서 로깅
                .doOnNext(rawResponse -> {
                    // Mock/Test 응답 확인
                    if (rawResponse.contains("\"mock\"") || rawResponse.contains("\"test\"") || 
                        rawResponse.contains("\"Mock\"") || rawResponse.contains("\"Test\"") ||
                        rawResponse.toLowerCase().contains("mock") || rawResponse.toLowerCase().contains("test")) {
                        log.warn("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
                        log.warn("⚠️ Runway API가 Mock/Test 응답을 반환했습니다!");
                        log.warn("TaskId: {}", taskId);
                        log.warn("Full API Response: {}", rawResponse);
                        log.warn("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
                    }
                    // FAILED 상태인 경우 ERROR 레벨로 로깅
                    else if (rawResponse.contains("\"status\":\"FAILED\"") || rawResponse.contains("\"status\":\"FAILED\"")) {
                        log.error("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
                        log.error("⚠️ Runway API returned FAILED status");
                        log.error("TaskId: {}", taskId);
                        log.error("Full API Response: {}", rawResponse);
                        log.error("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
                    } else {
                        log.info("📥 Runway API Task Status Response: {}", rawResponse);
                    }
                })
                .map(rawResponse -> {
                    try {
                        ObjectMapper mapper = new ObjectMapper();
                        RunwayTaskStatus status = mapper.readValue(rawResponse, RunwayTaskStatus.class);
                        return status;
                    } catch (Exception e) {
                        log.error("Error parsing Runway API response: {}", e.getMessage());
                        log.error("Failed response content: {}", rawResponse);
                        throw new RuntimeException("Failed to parse response", e);
                    }
                })
                .doOnSuccess(status -> {
                    // DB에 상태 업데이트
                    updateJobStatus(taskId, status);
                    
                    if (status.isDone()) {
                        if (status.hasError()) {
                            String errorMsg = status.getErrorMessage();
                            log.error("❌ Task failed - TaskId: {}, Status: {}, Error: {}",
                                    taskId, status.getStatus(), errorMsg);

                            // 에러 상세 정보 로깅
                            RunwayTaskStatus.ErrorInfo errorInfo = status.getError();
                            if (errorInfo != null) {
                                log.error("   Error Code: {}", errorInfo.getCode());
                                log.error("   Error Message: {}", errorInfo.getMessage());
                            } else {
                                log.error("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
                                log.error("⚠️ Error object is null. Status is FAILED but no error details available.");
                                log.error("TaskId: {}", taskId);
                                
                                // DB에서 해당 작업의 프롬프트 정보 조회
                                try {
                                    Optional<RunwayJob> jobOpt = runwayJobRepository.findByTaskId(taskId);
                                    if (jobOpt.isPresent()) {
                                        RunwayJob job = jobOpt.get();
                                        log.error("Original Prompt: {}", job.getPrompt());
                                        log.error("Job Status: {}", job.getStatus());
                                        log.error("Job Created At: {}", job.getCreateDt());
                                    }
                                } catch (Exception e) {
                                    log.error("Failed to retrieve job info from DB: {}", e.getMessage());
                                }
                                
                                log.error("Possible causes:");
                                log.error("   1. Content filtering - 프롬프트가 부적절하거나 금지된 내용 포함");
                                log.error("   2. API quota exceeded - API 할당량 초과");
                                log.error("   3. Invalid prompt - 프롬프트 형식이 잘못됨");
                                log.error("   4. Model limitation - 선택한 모델이 해당 프롬프트를 처리할 수 없음");
                                log.error("   5. API key issue - API 키가 유효하지 않거나 권한 부족");
                                log.error("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
                            }
                        } else {
                            log.info("Task completed successfully. Video URL: {}", status.getVideoUrl());
                        }
                    }
                })
                .onErrorResume(WebClientResponseException.class, e -> {
                    throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "작업 상태 조회 실패: " + e.getMessage());
                });
    }

    // DB에 작업 상태 업데이트
    @Transactional
    protected void updateJobStatus(String taskId, RunwayTaskStatus status) {
        try {
            Optional<RunwayJob> jobOpt = runwayJobRepository.findByTaskId(taskId);
            if (jobOpt.isEmpty()) {
                log.warn("Runway job not found for taskId: {}", taskId);
                return;
            }

            RunwayJob job = jobOpt.get();
            
            // 상태 업데이트
            String statusStr = status.getStatus();
            if ("RUNNING".equalsIgnoreCase(statusStr)) {
                job.markAsRunning();
            } else if ("SUCCEEDED".equalsIgnoreCase(statusStr)) {
                job.updateStatus(RunwayJobStatus.SUCCEEDED);
            } else if ("FAILED".equalsIgnoreCase(statusStr)) {
                job.updateStatus(RunwayJobStatus.FAILED);
            } else if ("CANCELLED".equalsIgnoreCase(statusStr)) {
                job.updateStatus(RunwayJobStatus.CANCELLED);
            }
            
            // 진행률 업데이트
            Integer progress = status.getProgress();
            if (progress != null) {
                job.setProgress(progress);
            }
            
            // 완료 시 비디오 URL 저장
            if (status.isDone() && !status.hasError()) {
                String videoUrl = status.getVideoUrl();
                if (videoUrl != null && !videoUrl.isBlank()) {
                    job.setVideoUrl(videoUrl);
                }
            }
            
            // 실패 시 에러 정보 저장
            if (status.hasError()) {
                String errorMessage = status.getErrorMessage();
                job.setError(null, errorMessage);
            }
            
            runwayJobRepository.save(job);
        } catch (Exception e) {
            log.error("Error updating job status in DB for taskId: {}", taskId, e);
        }
    }

    // jobId로 작업 상태 조회 (DB에서)
    @Transactional(readOnly = true)
    public RunwayJob getJobStatus(String jobId) {
        return runwayJobRepository.findByJobId(jobId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND, "작업을 찾을 수 없습니다: " + jobId));
    }

    // taskId로 작업 상태 조회 (DB에서)
    @Transactional(readOnly = true)
    public RunwayJob getJobByTaskId(String taskId) {
        return runwayJobRepository.findByTaskId(taskId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND, "작업을 찾을 수 없습니다: " + taskId));
    }

    // jobId로 비디오 URL 조회 (DB에서)
    @Transactional(readOnly = true)
    public String getVideoUrlByJobId(String jobId) {
        RunwayJob job = getJobStatus(jobId);
        
        if (job.getStatus() != RunwayJobStatus.SUCCEEDED) {
            throw new BusinessException(ErrorCode.DATA_NOT_FOUND, "비디오가 아직 생성 중입니다. 현재 상태: " + job.getStatus());
        }

        if (job.getVideoUrl() == null || job.getVideoUrl().isBlank()) {
            throw new BusinessException(ErrorCode.DATA_NOT_FOUND, "비디오 URL을 찾을 수 없습니다.");
        }

        return job.getVideoUrl();
    }

    // 작업 완료까지 자동 폴링 (비동기) - 완료될 때까지 주기적으로 상태를 확인합니다.
    public Mono<RunwayTaskStatus> pollUntilComplete(String taskId) {
        return Mono.defer(() -> getTaskStatus(taskId))
                .flatMap(status -> {
                    if (status.isDone()) {
                        if (status.hasError()) {
                            return Mono.error(new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "비디오 생성 실패: " + status.getErrorMessage()));
                        }
                        return Mono.just(status);
                    }
                    // 아직 완료되지 않음 - 에러를 던져서 재시도 트리거
                    return Mono.error(new RuntimeException("Task not yet complete"));
                })
                .retryWhen(Retry.fixedDelay(MAX_POLL_ATTEMPTS, POLL_INTERVAL)
                        .filter(throwable -> throwable.getMessage().equals("Task not yet complete"))
                        .doBeforeRetry(retrySignal -> {
                            log.debug("Retrying task polling... attempt {}",
                                    retrySignal.totalRetries() + 1);
                        })
                        .onRetryExhaustedThrow((retryBackoffSpec, retrySignal) -> {
                            log.error("Polling timeout: Maximum attempts ({}) exceeded", MAX_POLL_ATTEMPTS);
                            return new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "비디오 생성 타임아웃: 최대 대기 시간 초과");
                        })
                );
    }

    // 비디오 생성 요청 및 완료까지 대기 (동기적 처리)
    public Mono<RunwayTaskStatus> generateAndWait(RunwayGenerateRequest request) {

        return generateVideo(request)
                .flatMap(response -> {
                    if ("FAILED".equals(response.getStatus())) {
                        return Mono.error(new BusinessException(
                                ErrorCode.INTERNAL_SERVER_ERROR,
                                response.getErrorMessage()
                        ));
                    }
                    return pollUntilComplete(response.getTaskId());
                });
    }

    // 비디오 다운로드 URL 추출
    public String extractVideoUrl(RunwayTaskStatus taskStatus) {
        if (taskStatus == null) {
            throw new BusinessException(ErrorCode.DATA_NOT_FOUND, "작업 상태를 찾을 수 없습니다");
        }

        if (!taskStatus.isDone()) {
            throw new BusinessException(ErrorCode.DATA_NOT_FOUND, "비디오가 아직 생성 중입니다");
        }

        String videoUrl = taskStatus.getVideoUrl();
        if (videoUrl == null || videoUrl.isBlank()) {
            throw new BusinessException(ErrorCode.DATA_NOT_FOUND, "비디오 URL을 찾을 수 없습니다. Task status: " + taskStatus.getStatus());
        }

        return videoUrl;
    }

    // 작업 취소
    public Mono<Boolean> cancelTask(String taskId) {
        String uri = TASKS_ENDPOINT + "/" + taskId + "/cancel";

        return webClient.post()
                .uri(uri)
                .retrieve()
                .bodyToMono(Void.class)
                .map(v -> {
                    log.info("Task cancelled successfully: {}", taskId);
                    return true;
                })
                .onErrorResume(e -> {
                    log.error("Failed to cancel task: {}", taskId, e);
                    return Mono.just(false);
                });
    }

    // API 상태 확인 (헬스 체크용) Runway API는 별도의 health check 엔드포인트가 없으므로 API 키 존재 여부만 확인
    public Mono<Boolean> healthCheck() {
        // API 키가 설정되어 있으면 정상으로 간주
        boolean isConfigured = apiKey != null && !apiKey.trim().isEmpty();

        if (isConfigured) {
            log.debug("Runway API key configured, service is ready");
            return Mono.just(true);
        } else {
            log.warn("Runway API key is not configured");
            return Mono.just(false);
        }
    }
}
