package com.blaybus.application.openai;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * OpenAI API 클라이언트
 * ChatGPT 및 DALL-E API 호출
 */
@Slf4j
@Component
public class OpenAIClient {

    private final WebClient webClient;
    private final String apiKey;

    private static final String BASE_URL = "https://api.openai.com/v1";
    private static final String CHAT_COMPLETIONS_ENDPOINT = "/chat/completions";
    private static final String IMAGE_GENERATIONS_ENDPOINT = "/images/generations";

    public OpenAIClient(@Value("${openai.api.key:}") String apiKey) {
        this.apiKey = apiKey;

        // API 키 유효성 검사 및 로깅
        if (apiKey == null || apiKey.trim().isEmpty()) {
            log.error("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            log.error("⚠️ OpenAI API Key가 설정되지 않았습니다!");
            log.error("   환경 변수 OPENAI_API_KEY 또는 프로퍼티 openai.api.key를 설정해주세요.");
            log.error("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        } else {
            String maskedKey = apiKey.length() > 8
                    ? apiKey.substring(0, 8) + "..." + apiKey.substring(apiKey.length() - 4)
                    : "***";
            log.info("✅ OpenAI API Key 설정됨: {}", maskedKey);
        }

        this.webClient = WebClient.builder()
                .baseUrl(BASE_URL)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .build();
    }

    /**
     * ChatGPT API 호출
     */
    public Mono<Map<String, Object>> chatCompletion(String model,
                                                     String systemPrompt,
                                                     String userPrompt,
                                                     BigDecimal temperature,
                                                     Integer maxTokens) {
        // 메시지 구성
        List<Map<String, String>> messages = systemPrompt != null && !systemPrompt.isBlank()
                ? List.of(
                        Map.of("role", "system", "content", systemPrompt),
                        Map.of("role", "user", "content", userPrompt)
                )
                : List.of(
                        Map.of("role", "user", "content", userPrompt)
                );

        // 요청 페이로드 구성
        Map<String, Object> requestBody = Map.of(
                "model", model,
                "messages", messages,
                "temperature", temperature.doubleValue(),
                "max_tokens", maxTokens
        );

        return webClient.post()
                .uri(CHAT_COMPLETIONS_ENDPOINT)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class)
                .doOnNext(rawResponse -> {
                    log.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
                    log.info("📥 ChatGPT API Response:");
                    log.info("   Full Response: {}", rawResponse);
                    log.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
                })
                .map(rawResponse -> {
                    try {
                        ObjectMapper mapper = new ObjectMapper();
                        return mapper.readValue(rawResponse, Map.class);
                    } catch (Exception e) {
                        log.error("Failed to parse ChatGPT response", e);
                        throw new RuntimeException("Failed to parse ChatGPT response", e);
                    }
                });
    }

    /**
     * DALL-E 이미지 생성 API 호출
     */
    public Mono<Map<String, Object>> generateImage(String model,
                                                    String prompt,
                                                    String size,
                                                    String quality,
                                                    String style) {
        // 요청 페이로드 구성
        Map<String, Object> requestBody;

        if ("dall-e-3".equals(model)) {
            // DALL-E 3는 quality와 style 지원
            requestBody = Map.of(
                    "model", model,
                    "prompt", prompt,
                    "size", size,
                    "quality", quality,
                    "style", style,
                    "n", 1
            );
        } else {
            // DALL-E 2는 기본 옵션만
            requestBody = Map.of(
                    "model", model,
                    "prompt", prompt,
                    "size", size,
                    "n", 1
            );
        }

        log.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        log.info("📤 DALL-E API Request:");
        log.info("   Model: {}", model);
        log.info("   Prompt: {}", prompt);
        log.info("   Size: {}", size);
        if ("dall-e-3".equals(model)) {
            log.info("   Quality: {}", quality);
            log.info("   Style: {}", style);
        }
        log.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");

        return webClient.post()
                .uri(IMAGE_GENERATIONS_ENDPOINT)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class)
                .map(rawResponse -> {
                    try {
                        ObjectMapper mapper = new ObjectMapper();
                        return mapper.readValue(rawResponse, Map.class);
                    } catch (Exception e) {
                        log.error("Failed to parse DALL-E response", e);
                        throw new RuntimeException("Failed to parse DALL-E response", e);
                    }
                });
    }

    /**
     * API 헬스 체크
     */
    public Mono<Boolean> healthCheck() {
        return Mono.just(apiKey != null && !apiKey.trim().isEmpty());
    }
}
