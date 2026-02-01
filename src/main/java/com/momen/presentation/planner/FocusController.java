package com.momen.presentation.planner;

import com.momen.application.planner.FocusService;
import com.momen.application.planner.OralTestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/special")
@RequiredArgsConstructor
public class FocusController {

    private final FocusService focusService;
    private final OralTestService oralTestService;

    // --- Focus Watch (졸음 감지) ---
    
    @PostMapping("/focus/start")
    public ResponseEntity<Long> startSession(@RequestAttribute("userId") Long userId) {
        return ResponseEntity.ok(focusService.startFocusSession(userId));
    }

    @PostMapping("/focus/{sessionId}/end")
    public ResponseEntity<Integer> endSession(@PathVariable Long sessionId, @RequestParam Integer drowsiness, @RequestParam Integer phoneUse) {
        Integer score = focusService.endFocusSession(sessionId, drowsiness, phoneUse);
        return ResponseEntity.ok(score);
    }

    // --- Oral Test (구술 테스트) ---
    @PostMapping("/oral-test/submit")
    public ResponseEntity<Long> submitOralTest(
            @RequestAttribute("userId") Long userId,
            @RequestParam String topic,
            @RequestParam String audioUrl) {
        return ResponseEntity.ok(oralTestService.submitOralTest(userId, topic, audioUrl));
    }
}
