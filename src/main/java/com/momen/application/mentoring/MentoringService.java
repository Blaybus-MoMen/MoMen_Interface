package com.momen.application.mentoring;

import com.momen.domain.mentoring.Mentee;
import com.momen.domain.mentoring.Mentor;
import com.momen.domain.user.User;
import com.momen.infrastructure.jpa.mentoring.MenteeRepository;
import com.momen.infrastructure.jpa.mentoring.MentorRepository;
import com.momen.infrastructure.jpa.user.UserJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MentoringService {

    private final MentorRepository mentorRepository;
    private final MenteeRepository menteeRepository;
    private final UserJpaRepository userRepository;

    @Transactional
    public Long registerMentor(Long userId, String intro) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        Mentor mentor = new Mentor(user, intro);
        return mentorRepository.save(mentor).getId();
    }

    @Transactional
    public Long registerMentee(Long userId, String grade, String targetUniv) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        // 멘토는 나중에 매칭 (null 허용)
        Mentee mentee = new Mentee(user, null, grade, targetUniv);
        return menteeRepository.save(mentee).getId();
    }
}
