package com.momen.application.common;

import com.momen.infrastructure.external.ai.AiClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.regex.Pattern;

/**
 * 프롬프트 번역 서비스
 * 한글 프롬프트를 영어로 자동 번역하여 Veo API에 전달
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PromptTranslationService {

    private final AiClient aiClient;

    // 한글 문자 패턴 (한글, 한자 포함)
    private static final Pattern KOREAN_PATTERN = Pattern.compile(".*[\\uAC00-\\uD7A3\\u4E00-\\u9FFF].*");

    // 프롬프트가 한글인지 확인
    public boolean containsKorean(String prompt) {
        if (prompt == null || prompt.isBlank()) {
            return false;
        }
        return KOREAN_PATTERN.matcher(prompt).matches();
    }

    // 한글 프롬프트를 영어로 번역
    public String translateToEnglish(String koreanPrompt) {
        if (koreanPrompt == null || koreanPrompt.isBlank()) {
            return koreanPrompt;
        }

        // 한글이 없으면 그대로 반환
        if (!containsKorean(koreanPrompt)) {
            return koreanPrompt;
        }

        try {
            log.info("Translating Korean prompt to English: {}", koreanPrompt);

            String systemPrompt = """
                    You are a professional translator specializing in video generation prompts.
                    Translate the given Korean prompt into natural, descriptive English that is suitable for AI video generation.
                    Keep the meaning and tone accurate, and make it cinematic and visually descriptive.
                    Only return the translated English text, without any additional explanation or quotation marks.
                    """;

            String translated = aiClient.chat(systemPrompt, koreanPrompt);

            if (translated == null || translated.isBlank()) {
                log.warn("Translation failed: AI API returned empty response. Using original prompt.");
                return koreanPrompt;
            }

            // Mock 응답 감지 (API 키가 없을 때)
            if (translated.contains("Mock response") || translated.contains("test response") ||
                translated.contains("API key is not configured")) {
                log.warn("AI API 키가 설정되지 않아 Mock 응답을 받았습니다. 원본 프롬프트를 그대로 사용합니다.");
                return koreanPrompt;
            }

            // 따옴표 제거 (GPT가 따옴표로 감싸서 반환하는 경우)
            if (translated.startsWith("\"") && translated.endsWith("\"")) {
                translated = translated.substring(1, translated.length() - 1);
            }
            if (translated.startsWith("'") && translated.endsWith("'")) {
                translated = translated.substring(1, translated.length() - 1);
            }

            log.info("Translation completed: {} -> {}", koreanPrompt, translated);
            return translated;

        } catch (Exception e) {
            log.error("Error translating prompt: {}", e.getMessage(), e);
            return koreanPrompt;
        }
    }

    // 프롬프트를 처리 (한글이면 번역, 아니면 그대로 반환)
    public String processPrompt(String prompt) {
        if (prompt == null || prompt.isBlank()) {
            return prompt;
        }

        if (containsKorean(prompt)) {
            return translateToEnglish(prompt);
        }

        return prompt;
    }
}
