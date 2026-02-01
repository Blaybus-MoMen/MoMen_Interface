package com.momen.infrastructure.jpa.planner;

import com.momen.domain.planner.OralTest;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface OralTestRepository extends JpaRepository<OralTest, Long> {
    List<OralTest> findByMenteeId(Long menteeId);
}
