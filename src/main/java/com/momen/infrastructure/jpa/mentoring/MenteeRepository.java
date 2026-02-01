package com.momen.infrastructure.jpa.mentoring;

import com.momen.domain.mentoring.Mentee;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface MenteeRepository extends JpaRepository<Mentee, Long> {
    Optional<Mentee> findByUserId(Long userId);
    List<Mentee> findByMentorId(Long mentorId);
}
