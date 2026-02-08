package com.momen.infrastructure.jpa.planner;

import com.momen.domain.planner.SubmissionFile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SubmissionFileRepository extends JpaRepository<SubmissionFile, Long> {
    List<SubmissionFile> findBySubmissionId(Long submissionId);
    void deleteBySubmissionId(Long submissionId);
}
