package com.momen.infrastructure.external.ai;

import com.momen.domain.planner.AnalysisStatus;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("!prod") // 프로덕션이 아닐 때(개발/테스트) 사용
public class MockAiClient implements AiClient {

    @Override
    public AiVisionResult analyzeImage(String imageUrl) {
        // [Mock] 이미지 분석 시뮬레이션
        try { Thread.sleep(500); } catch (InterruptedException e) {} // 0.5초 딜레이
        return new AiVisionResult(AnalysisStatus.COMPLETED, 88, "풀이 과정이 논리적이며, 필기 밀도가 높습니다. 2번 문제는 다시 확인이 필요합니다.");
    }

    @Override
    public String generateText(String prompt) {
        // [Mock] 프롬프트에 따른 가짜 응답 생성
        if (prompt.contains("feedback")) {
            return "오늘 수학 공부 시간이 목표보다 30분 부족했지만, 영어 단어 암기는 완벽했어! 내일은 수학 함수 파트에 좀 더 집중해보자. 화이팅! 🦊";
        } else if (prompt.contains("mistake")) {
            return "[변형 문제] 함수 f(x) = 3x^2 + 6x + 5 의 최솟값을 구하시오. (정답: 2)";
        }
        return "AI 생성 텍스트입니다.";
    }

    @Override
    public String chat(String systemRole, String userMessage) {
        return "그 문제는 '근의 공식'을 사용하면 쉽게 풀려! x = (-b ± √(b²-4ac)) / 2a 공식을 대입해볼래? 😊";
    }

    @Override
    public AiOralResult analyzeSpeech(String audioUrl, String topic) {
        return new AiOralResult(
                "미분계수는 곡선 위의 특정 점에서의 접선의 기울기를 나타냅니다.",
                92,
                "핵심 키워드인 '접선의 기울기'를 정확히 언급했습니다. 아주 좋아요!"
        );
    }
}
