package com.momen.infrastructure.jpa.mentoring;

import com.momen.domain.mentoring.Mentor;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface MentorRepository extends JpaRepository<Mentor, Long> {
    Optional<Mentor> findByUserId(Long userId);
}
