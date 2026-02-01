package com.momen.infrastructure.external.ai;

import com.momen.domain.planner.AnalysisStatus;
import com.momen.infrastructure.external.ai.dto.OpenAiChatRequest;
import com.momen.infrastructure.external.ai.dto.OpenAiChatResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@Profile("prod") // 'prod' 프로파일일 때만 활성화 (기본은 Mock)
@RequiredArgsConstructor
public class OpenAiClient implements AiClient {

    @Value("${OPENAI_API_KEY}")
    private String apiKey;

    private final WebClient.Builder webClientBuilder;
    private static final String OPENAI_API_URL = "https://api.openai.com/v1/chat/completions";

    @Override
    public AiVisionResult analyzeImage(String imageUrl) {
        log.info("Calling OpenAI Vision API for image: {}", imageUrl);

        String prompt = "이 학습 자료(노트 필기 또는 문제 풀이)를 분석해줘. " +
                "1. 필기 밀도(Study Density)를 0~100 사이의 숫자로 평가해줘. " +
                "2. 학습에 대한 격려나 부족한 점에 대한 짧은 코멘트를 한국어로 작성해줘. " +
                "형식: Score: {점수} / Comment: {내용}";

        String response = callGptVision(imageUrl, prompt);
        
        // 파싱 로직 (간단하게 구현)
        int score = 0;
        String comment = response;
        
        try {
            if (response.contains("Score:")) {
                String scorePart = response.split("/")[0];
                score = Integer.parseInt(scorePart.replaceAll("[^0-9]", ""));
            }
        } catch (Exception e) {
            score = 50; // 기본값
        }

        return new AiVisionResult(AnalysisStatus.COMPLETED, score, comment);
    }

    @Override
    public String generateText(String prompt) {
        log.info("Calling OpenAI Text API");
        return callGptText(prompt);
    }

    @Override
    public String chat(String systemRole, String userMessage) {
        log.info("Calling OpenAI Chat API");
        return callGptChat(systemRole, userMessage);
    }

    @Override
    public AiOralResult analyzeSpeech(String audioUrl, String topic) {
        // [Note] Whisper API requires file upload (multipart). 
        // For now, we simulate this using GPT to evaluate a 'transcription' assuming STT is done via another service or simplified here.
        // In a real scenario, you would use WebClient to POST the audio file to https://api.openai.com/v1/audio/transcriptions
        
        log.info("Simulating STT & Eval for audio: {}", audioUrl);
        
        // 가상의 STT 결과 (실제 구현 시 Whisper 호출 필요)
        String transcription = "미분 계수에 대해 설명하겠습니다. 곡선의 접선 기울기를 의미합니다."; 
        
        String prompt = "주제: " + topic + "\n" +
                "학생의 답변: " + transcription + "\n" +
                "이 답변이 주제를 얼마나 잘 설명했는지 0~100점 점수와 피드백을 줘.";
        
        String eval = callGptText(prompt);
        
        return new AiOralResult(transcription, 85, eval);
    }

    // --- Private Helper Methods ---

    private String callGptText(String prompt) {
        return callGptChat("You are a helpful AI assistant.", prompt);
    }

    private String callGptChat(String systemRole, String userMessage) {
        List<OpenAiChatRequest.Message> messages = new ArrayList<>();
        messages.add(OpenAiChatRequest.Message.builder().role("system").content(systemRole).build());
        messages.add(OpenAiChatRequest.Message.builder().role("user").content(userMessage).build());

        return sendRequest(messages);
    }

    private String callGptVision(String imageUrl, String textPrompt) {
        List<OpenAiChatRequest.Content> contents = new ArrayList<>();
        contents.add(OpenAiChatRequest.Content.builder().type("text").text(textPrompt).build());
        contents.add(OpenAiChatRequest.Content.builder().type("image_url")
                .image_url(OpenAiChatRequest.ImageUrl.builder().url(imageUrl).build()).build());

        OpenAiChatRequest.Message userMsg = OpenAiChatRequest.Message.builder()
                .role("user")
                .content(contents)
                .build();

        List<OpenAiChatRequest.Message> messages = new ArrayList<>();
        messages.add(userMsg);

        return sendRequest(messages);
    }

    private String sendRequest(List<OpenAiChatRequest.Message> messages) {
        OpenAiChatRequest request = OpenAiChatRequest.builder()
                .model("gpt-4o") // GPT-4o 사용
                .messages(messages)
                .max_tokens(1000)
                .temperature(0.7)
                .build();

        OpenAiChatResponse response = WebClient.create()
                .post()
                .uri(OPENAI_API_URL)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(OpenAiChatResponse.class)
                .block(); // Blocking for simplicity in service layer

        if (response != null && !response.getChoices().isEmpty()) {
            return response.getChoices().get(0).getMessage().getContent();
        }
        return "AI 응답을 불러오지 못했습니다.";
    }
}
