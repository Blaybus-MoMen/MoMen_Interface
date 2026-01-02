package com.blaybus.infrastructure.external.openai;

import com.blaybus.infrastructure.external.openai.dto.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * OpenAI API 클라이언트 - Embeddings 및 Chat Completions API 연동
 */
@Slf4j
@Component
public class OpenAiClient {

    @Value("${openai.api.key:#{null}}")
    private String apiKey;

    @Value("${openai.api.base-url:https://api.openai.com/v1}")
    private String baseUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    // Embedding 생성
    public EmbeddingResponse createEmbedding(String text) {
        return createEmbedding(text, "text-embedding-3-small");
    }

    // Embedding 생성 (모델 지정)
    public EmbeddingResponse createEmbedding(String text, String model) {
        if (apiKey == null || apiKey.isEmpty()) {
            return createMockEmbedding();
        }

        String url = baseUrl + "/embeddings";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", model);
        requestBody.put("input", text);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

        try {
            return restTemplate.postForObject(url, request, EmbeddingResponse.class);
        } catch (Exception e) {
            return createMockEmbedding();
        }
    }

    // Chat Completion 생성 (LLM-as-Judge용)
    public ChatCompletionResponse createChatCompletion(String systemPrompt, String userPrompt) {
        return createChatCompletion(systemPrompt, userPrompt, "gpt-4o-mini", 0.3);
    }

    // Chat Completion 생성 (파라미터 지정)
    public ChatCompletionResponse createChatCompletion(String systemPrompt, String userPrompt,
                                                       String model, double temperature) {
        if (apiKey == null || apiKey.isEmpty()) {
            return createMockChatCompletion();
        }

        String url = baseUrl + "/chat/completions";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", model);
        requestBody.put("temperature", temperature);
        requestBody.put("messages", List.of(
                Map.of("role", "system", "content", systemPrompt),
                Map.of("role", "user", "content", userPrompt)
        ));

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

        try {
            return restTemplate.postForObject(url, request, ChatCompletionResponse.class);
        } catch (Exception e) {
            return createMockChatCompletion();
        }
    }

    // Mock Embedding (API 키가 없을 때 개발용)  -> *******테스트용임 지우지 말기************
    private EmbeddingResponse createMockEmbedding() {
        EmbeddingResponse response = new EmbeddingResponse();
        EmbeddingData data = new EmbeddingData();

        // 1536차원 mock embedding (모두 0.1)
        double[] embedding = new double[1536];
        for (int i = 0; i < 1536; i++) {
            embedding[i] = 0.1;
        }

        data.setEmbedding(embedding);
        response.setData(List.of(data));

        return response;
    }

    // Mock Chat Completion (API 키가 없을 때 개발용) -> *******테스트용임 지우지 말기************
    private ChatCompletionResponse createMockChatCompletion() {
        ChatCompletionResponse response = new ChatCompletionResponse();
        ChatChoice choice = new ChatChoice();
        ChatMessage message = new ChatMessage();

        message.setRole("assistant");
        message.setContent("Mock response: This is a test response when OpenAI API key is not configured.");

        choice.setMessage(message);
        response.setChoices(List.of(choice));

        return response;
    }
}
