package com.blaybus.application.openai;

import com.blaybus.application.openai.dto.ChatGPTRequest;
import com.blaybus.application.openai.dto.ChatGPTResponse;
import com.blaybus.application.openai.dto.SimpleChatRequest;
import com.blaybus.application.openai.dto.SimpleChatResponse;
import com.blaybus.core.error.enums.ErrorCode;
import com.blaybus.core.exception.BusinessException;
import com.blaybus.domain.openai.OpenAIChatLog;
import com.blaybus.domain.openai.OpenAIChatLogRepository;
import com.blaybus.domain.openai.OpenAIChatStatus;
import com.blaybus.domain.user.User;
import com.blaybus.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * OpenAI ChatGPT 서비스
 * ChatGPT API 호출 기록 및 결과 관리
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OpenAIChatService {

    private final OpenAIChatLogRepository chatLogRepository;
    private final UserRepository userRepository;
    private final OpenAIClient openAIClient;

    // ChatGPT 호출 로그 생성
    @Transactional
    public OpenAIChatLog createChatLog(Long userId,
                                       String model,
                                       String systemPrompt,
                                       String userPrompt,
                                       BigDecimal temperature,
                                       Integer maxTokens,
                                       String metadata) {
        User user = null;
        if (userId != null) {
            user = userRepository.findById(userId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND, "사용자를 찾을 수 없습니다"));
        }

        OpenAIChatLog chatLog = OpenAIChatLog.builder()
                .user(user)
                .model(model)
                .systemPrompt(systemPrompt)
                .userPrompt(userPrompt)
                .temperature(temperature)
                .maxTokens(maxTokens)
                .metadata(metadata)
                .build();

        return chatLogRepository.save(chatLog);
    }

    // ChatGPT 응답 저장
    @Transactional
    public void saveResponse(String jobId,
                            String assistantResponse,
                            Integer tokensUsed,
                            Integer promptTokens,
                            Integer completionTokens) {
        OpenAIChatLog chatLog = chatLogRepository.findByJobId(jobId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND, "ChatGPT 로그를 찾을 수 없습니다"));

        chatLog.setResponse(assistantResponse, tokensUsed, promptTokens, completionTokens);
        chatLogRepository.save(chatLog);
    }

    // 에러 저장
    @Transactional
    public void saveError(String jobId, String errorCode, String errorMessage) {
        OpenAIChatLog chatLog = chatLogRepository.findByJobId(jobId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND, "ChatGPT 로그를 찾을 수 없습니다"));

        chatLog.setError(errorCode, errorMessage);
        chatLogRepository.save(chatLog);
    }

    // Job ID로 조회
    @Transactional(readOnly = true)
    public OpenAIChatLog getChatLog(String jobId) {
        return chatLogRepository.findByJobId(jobId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND, "ChatGPT 로그를 찾을 수 없습니다"));
    }

    // 사용자별 ChatGPT 로그 조회
    @Transactional(readOnly = true)
    public List<OpenAIChatLog> getChatLogsByUser(Long userId) {
        return chatLogRepository.findByUserId(userId);
    }

    // 상태별 ChatGPT 로그 조회
    @Transactional(readOnly = true)
    public List<OpenAIChatLog> getChatLogsByStatus(OpenAIChatStatus status) {
        return chatLogRepository.findByStatus(status);
    }

    // 사용자별 대기중인 ChatGPT 로그 조회
    @Transactional(readOnly = true)
    public List<OpenAIChatLog> getPendingChatLogsByUser(Long userId) {
        return chatLogRepository.findByUserIdAndStatus(userId, OpenAIChatStatus.PENDING);
    }

    // ChatGPT 요청 및 응답 처리 (동기 방식)
    @Transactional
    public Mono<ChatGPTResponse> sendChatRequest(ChatGPTRequest request) {
        // 로그 생성
        OpenAIChatLog chatLog = createChatLog(
                request.getUserId(),
                request.getModel(),
                request.getSystemPrompt(),
                request.getUserPrompt(),
                request.getTemperature(),
                request.getMaxTokens(),
                request.getMetadata()
        );

        String jobId = chatLog.getJobId();

        // OpenAI API 호출
        return openAIClient.chatCompletion(
                        request.getModel(),
                        request.getSystemPrompt(),
                        request.getUserPrompt(),
                        request.getTemperature(),
                        request.getMaxTokens()
                )
                .map(response -> {
                    try {
                        // 응답 파싱
                        Map<String, Object> choices = ((List<Map<String, Object>>) response.get("choices")).get(0);
                        Map<String, Object> message = (Map<String, Object>) choices.get("message");
                        String content = (String) message.get("content");

                        Map<String, Object> usage = (Map<String, Object>) response.get("usage");
                        Integer totalTokens = (Integer) usage.get("total_tokens");
                        Integer promptTokens = (Integer) usage.get("prompt_tokens");
                        Integer completionTokens = (Integer) usage.get("completion_tokens");

                        // 응답 저장
                        saveResponse(jobId, content, totalTokens, promptTokens, completionTokens);

                        return ChatGPTResponse.success(jobId, content, totalTokens, promptTokens, completionTokens);
                    } catch (Exception e) {
                        log.error("Failed to process ChatGPT response", e);
                        saveError(jobId, "PARSE_ERROR", e.getMessage());
                        return ChatGPTResponse.failed(jobId, "PARSE_ERROR", e.getMessage());
                    }
                })
                .onErrorResume(e -> {
                    log.error("ChatGPT API error", e);
                    saveError(jobId, "API_ERROR", e.getMessage());
                    return Mono.just(ChatGPTResponse.failed(jobId, "API_ERROR", e.getMessage()));
                });
    }

    // 간단한 ChatGPT 테스트 (DB 저장 없이 즉시 응답)
    public Mono<SimpleChatResponse> sendSimpleChatRequest(SimpleChatRequest request) {
        // OpenAI API 호출 (DB 저장 없음)
        return openAIClient.chatCompletion(
                        request.getModel(),
                        null, // 시스템 프롬프트 없음
                        request.getPrompt(),
                        request.getTemperature(),
                        request.getMaxTokens()
                )
                .map(response -> {
                    try {
                        // 응답 파싱
                        Map<String, Object> choices = ((List<Map<String, Object>>) response.get("choices")).get(0);
                        Map<String, Object> message = (Map<String, Object>) choices.get("message");
                        String content = (String) message.get("content");

                        Map<String, Object> usage = (Map<String, Object>) response.get("usage");
                        Integer totalTokens = (Integer) usage.get("total_tokens");
                        Integer promptTokens = (Integer) usage.get("prompt_tokens");
                        Integer completionTokens = (Integer) usage.get("completion_tokens");

                        log.info("Simple chat test success - tokens used: {}", totalTokens);
                        return SimpleChatResponse.success(content, totalTokens, promptTokens, completionTokens);
                    } catch (Exception e) {
                        log.error("Failed to process simple chat response", e);
                        return SimpleChatResponse.failed("응답 파싱 실패: " + e.getMessage());
                    }
                })
                .onErrorResume(e -> {
                    log.error("Simple chat API error", e);
                    return Mono.just(SimpleChatResponse.failed("API 호출 실패: " + e.getMessage()));
                });
    }
}
