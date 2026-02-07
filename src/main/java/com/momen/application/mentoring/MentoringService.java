package com.momen.application.mentoring;

import com.momen.application.mentoring.dto.MenteeResponse;
import com.momen.domain.mentoring.Mentee;
import com.momen.domain.mentoring.Mentor;
import com.momen.domain.user.User;
import com.momen.infrastructure.jpa.mentoring.MenteeRepository;
import com.momen.infrastructure.jpa.mentoring.MentorRepository;
import com.momen.infrastructure.jpa.user.UserJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

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
        Mentee mentee = new Mentee(user, null, grade, targetUniv);
        return menteeRepository.save(mentee).getId();
    }

    // 멘토의 담당 멘티 목록 조회
    public List<MenteeResponse> getMenteeList(Long mentorUserId) {
        Mentor mentor = mentorRepository.findByUserId(mentorUserId)
                .orElseThrow(() -> new IllegalArgumentException("Mentor not found"));
        return menteeRepository.findByMentorId(mentor.getId()).stream()
                .map(MenteeResponse::from)
                .collect(Collectors.toList());
    }

    // 멘토의 담당 멘티 단건 조회
    public MenteeResponse getMentee(Long mentorUserId, Long menteeId) {
        Mentor mentor = mentorRepository.findByUserId(mentorUserId)
                .orElseThrow(() -> new IllegalArgumentException("Mentor not found"));
        Mentee mentee = menteeRepository.findById(menteeId)
                .orElseThrow(() -> new IllegalArgumentException("Mentee not found"));
        if (!mentee.getMentor().getId().equals(mentor.getId())) {
            throw new IllegalArgumentException("해당 멘티는 담당 멘티가 아닙니다");
        }
        return MenteeResponse.from(mentee);
    }
}
