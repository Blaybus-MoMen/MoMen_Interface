package com.blaybus.infrastructure.external.image;

import com.blaybus.infrastructure.external.image.dto.ImageGenerationResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * 이미지 생성 API 클라이언트
 */
@Slf4j
@Component
public class ImageGenerationClient {

    @Value("${openai.api.key:#{null}}")
    private String apiKey;

    @Value("${openai.api.base-url:https://api.openai.com/v1}")
    private String baseUrl;

    // 기본 이미지 생성 모델 (예: dall-e-3)
    @Value("${openai.model.image:dall-e-3}")
    private String imageModel;

    private final RestTemplate restTemplate = new RestTemplate();

    public String generateImage(String prompt, Long seed, String size, String style, String quality) {

        // API 키 체크
        if (apiKey == null || apiKey.isEmpty()) {
            return createMockImageUrl(prompt, seed);
        }

        String url = baseUrl + "/images/generations";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", imageModel);
        requestBody.put("prompt", prompt);
        requestBody.put("n", 1);
        requestBody.put("size", (size != null && !size.isEmpty()) ? size : "1024x1024");                // 기본값 1024x1024, 입력받은 값이 있으면 사용
        requestBody.put("style", (style != null && !style.isEmpty()) ? style : "vivid");                // 스타일 추가 (vivid or natural)
        requestBody.put("quality", (quality != null && !quality.isEmpty()) ? quality : "standard");     // 품질 추가 (standard or hd)
        if (seed != null) {
            requestBody.put("seed", seed);
        }

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
        ImageGenerationResponse response = restTemplate.postForObject(url, request, ImageGenerationResponse.class);

        if (response != null && response.getData() != null && !response.getData().isEmpty()) {
            return response.getData().get(0).getUrl();
        }

        return createMockImageUrl(prompt, seed);
    }

    // API 키가 없거나 오류가 발생했을 때 사용할 Mock 이미지 URL
    private String createMockImageUrl(String prompt, Long seed) {
        String safePrompt = prompt != null ? prompt.replaceAll("\\s+", "_") : "mock";
        String safeSeed = seed != null ? seed.toString() : "0";
        return "https://example.com/mock-image/" + safePrompt + "?seed=" + safeSeed;
    }
}

