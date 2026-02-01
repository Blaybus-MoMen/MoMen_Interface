package com.momen.application.planner;

import com.momen.domain.mentoring.Mentee;
import com.momen.domain.planner.FocusSession;
import com.momen.infrastructure.jpa.mentoring.MenteeRepository;
import com.momen.infrastructure.jpa.planner.FocusSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FocusService {

    private final FocusSessionRepository focusSessionRepository;
    private final MenteeRepository menteeRepository;

    // 공부 시작 (세션 생성)
    @Transactional
    public Long startFocusSession(Long userId) {
        Mentee mentee = menteeRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Mentee not found"));
        
        FocusSession session = new FocusSession(mentee);
        return focusSessionRepository.save(session).getId();
    }

    // 공부 종료 (감지된 결과 리포트 저장)
    @Transactional
    public Integer endFocusSession(Long sessionId, Integer drowsinessCount, Integer phoneUseCount) {
        FocusSession session = focusSessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Session not found"));
        
        session.endSession(drowsinessCount, phoneUseCount);
        return session.getFocusScore();
    }
}
