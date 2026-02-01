package com.momen.presentation.planner;

import com.momen.application.planner.FocusService;
import com.momen.application.planner.OralTestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Focus & OralTest", description = "집중 세션 및 구술 테스트 API")
@RestController
@RequestMapping("/api/v1/special")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class FocusController {

    private final FocusService focusService;
    private final OralTestService oralTestService;

    @Operation(summary = "집중 세션 시작", description = "공부 집중 세션을 시작합니다 (졸음 감지)")
    @PostMapping("/focus/start")
    public ResponseEntity<Long> startSession(@RequestAttribute("userId") Long userId) {
        return ResponseEntity.ok(focusService.startFocusSession(userId));
    }

    @Operation(summary = "집중 세션 종료", description = "집중 세션을 종료하고 집중 점수를 반환합니다")
    @PostMapping("/focus/{sessionId}/end")
    public ResponseEntity<Integer> endSession(
            @Parameter(description = "세션 ID") @PathVariable Long sessionId,
            @Parameter(description = "졸음 감지 횟수") @RequestParam Integer drowsiness,
            @Parameter(description = "폰 사용 감지 횟수") @RequestParam Integer phoneUse) {
        Integer score = focusService.endFocusSession(sessionId, drowsiness, phoneUse);
        return ResponseEntity.ok(score);
    }

    @Operation(summary = "구술 테스트 제출", description = "구술 테스트 결과를 제출합니다")
    @PostMapping("/oral-test/submit")
    public ResponseEntity<Long> submitOralTest(
            @RequestAttribute("userId") Long userId,
            @Parameter(description = "테스트 주제") @RequestParam String topic,
            @Parameter(description = "음성 파일 URL") @RequestParam String audioUrl) {
        return ResponseEntity.ok(oralTestService.submitOralTest(userId, topic, audioUrl));
    }
}
