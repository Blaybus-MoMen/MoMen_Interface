package com.momen.infrastructure.jpa.planner;

import com.momen.domain.planner.MistakeNote;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MistakeNoteRepository extends JpaRepository<MistakeNote, Long> {
    List<MistakeNote> findByMenteeId(Long menteeId);
}
