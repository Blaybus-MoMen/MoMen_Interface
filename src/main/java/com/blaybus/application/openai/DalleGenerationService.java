package com.blaybus.application.openai;

import com.blaybus.application.openai.dto.DalleGenerateRequest;
import com.blaybus.application.openai.dto.DalleGenerateResponse;
import com.blaybus.core.error.enums.ErrorCode;
import com.blaybus.core.exception.BusinessException;
import com.blaybus.domain.openai.DalleGenerationLog;
import com.blaybus.domain.openai.DalleGenerationLogRepository;
import com.blaybus.domain.openai.DalleGenerationStatus;
import com.blaybus.domain.user.User;
import com.blaybus.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

/**
 * DALL-E 이미지 생성 서비스
 * DALL-E API 호출 기록 및 결과 관리
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DalleGenerationService {

    private final DalleGenerationLogRepository dalleLogRepository;
    private final UserRepository userRepository;
    private final OpenAIClient openAIClient;

    // DALL-E 생성 로그 생성
    @Transactional
    public DalleGenerationLog createGenerationLog(Long userId,
                                                   String model,
                                                   String prompt,
                                                   String size,
                                                   String quality,
                                                   String style,
                                                   Long seed,
                                                   String metadata) {
        User user = null;
        if (userId != null) {
            user = userRepository.findById(userId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND, "사용자를 찾을 수 없습니다"));
        }

        DalleGenerationLog dalleLog = DalleGenerationLog.builder()
                .user(user)
                .model(model)
                .prompt(prompt)
                .size(size)
                .quality(quality)
                .style(style)
                .seed(seed)
                .metadata(metadata)
                .build();

        return dalleLogRepository.save(dalleLog);
    }

    // 이미지 생성 완료 저장
    @Transactional
    public void saveGeneratedImage(String jobId,
                                   String imageUrl,
                                   String revisedPrompt,
                                   String b64Json) {
        DalleGenerationLog dalleLog = dalleLogRepository.findByJobId(jobId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND, "DALL-E 로그를 찾을 수 없습니다"));

        dalleLog.setImageGenerated(imageUrl, revisedPrompt, b64Json);
        dalleLogRepository.save(dalleLog);
    }

    // 에러 저장
    @Transactional
    public void saveError(String jobId, String errorCode, String errorMessage) {
        DalleGenerationLog dalleLog = dalleLogRepository.findByJobId(jobId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND, "DALL-E 로그를 찾을 수 없습니다"));

        dalleLog.setError(errorCode, errorMessage);
        dalleLogRepository.save(dalleLog);
    }

    // 저작권 플래그 설정
    @Transactional
    public void setCopyrightFlag(String jobId, Boolean copyrightFlag) {
        DalleGenerationLog dalleLog = dalleLogRepository.findByJobId(jobId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND, "DALL-E 로그를 찾을 수 없습니다"));

        dalleLog.setCopyrightFlag(copyrightFlag);
        dalleLogRepository.save(dalleLog);
    }

    // 안전 필터 트리거 설정
    @Transactional
    public void setSafetyFilterTriggered(String jobId, Boolean triggered) {
        DalleGenerationLog dalleLog = dalleLogRepository.findByJobId(jobId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND, "DALL-E 로그를 찾을 수 없습니다"));

        dalleLog.setSafetyFilterTriggered(triggered);
        dalleLogRepository.save(dalleLog);
    }

    // Job ID로 조회
    @Transactional(readOnly = true)
    public DalleGenerationLog getGenerationLog(String jobId) {
        return dalleLogRepository.findByJobId(jobId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND, "DALL-E 로그를 찾을 수 없습니다"));
    }

    // 사용자별 DALL-E 로그 조회
    @Transactional(readOnly = true)
    public List<DalleGenerationLog> getGenerationLogsByUser(Long userId) {
        return dalleLogRepository.findByUserId(userId);
    }

    // 상태별 DALL-E 로그 조회
    @Transactional(readOnly = true)
    public List<DalleGenerationLog> getGenerationLogsByStatus(DalleGenerationStatus status) {
        return dalleLogRepository.findByStatus(status);
    }

    // 사용자별 대기중인 DALL-E 로그 조회
    @Transactional(readOnly = true)
    public List<DalleGenerationLog> getPendingGenerationLogsByUser(Long userId) {
        return dalleLogRepository.findByUserIdAndStatus(userId, DalleGenerationStatus.PENDING);
    }

    /**
     * DALL-E 이미지 생성 요청 및 응답 처리 (동기 방식)
     */
    @Transactional
    public Mono<DalleGenerateResponse> generateImage(DalleGenerateRequest request) {
        // 로그 생성
        DalleGenerationLog dalleLog = createGenerationLog(
                request.getUserId(),
                request.getModel(),
                request.getPrompt(),
                request.getSize(),
                request.getQuality(),
                request.getStyle(),
                request.getSeed(),
                request.getMetadata()
        );

        String jobId = dalleLog.getJobId();

        // OpenAI DALL-E API 호출
        return openAIClient.generateImage(
                        request.getModel(),
                        request.getPrompt(),
                        request.getSize(),
                        request.getQuality(),
                        request.getStyle()
                )
                .map(response -> {
                    try {
                        // 응답 파싱
                        List<Map<String, Object>> data = (List<Map<String, Object>>) response.get("data");
                        Map<String, Object> imageData = data.get(0);

                        String imageUrl = (String) imageData.get("url");
                        String revisedPrompt = (String) imageData.get("revised_prompt");
                        String b64Json = (String) imageData.get("b64_json");

                        // 이미지 생성 완료 저장
                        saveGeneratedImage(jobId, imageUrl, revisedPrompt, b64Json);

                        return DalleGenerateResponse.success(jobId, imageUrl, revisedPrompt, b64Json);
                    } catch (Exception e) {
                        log.error("Failed to process DALL-E response", e);
                        saveError(jobId, "PARSE_ERROR", e.getMessage());
                        return DalleGenerateResponse.failed(jobId, "PARSE_ERROR", e.getMessage());
                    }
                })
                .onErrorResume(e -> {
                    log.error("DALL-E API error", e);
                    saveError(jobId, "API_ERROR", e.getMessage());
                    return Mono.just(DalleGenerateResponse.failed(jobId, "API_ERROR", e.getMessage()));
                });
    }
}

