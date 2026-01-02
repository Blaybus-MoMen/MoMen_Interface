package com.blaybus.domain.runway;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RunwayJobRepository extends JpaRepository<RunwayJob, Long> {
    Optional<RunwayJob> findByJobId(String jobId);
    
    Optional<RunwayJob> findByTaskId(String taskId);
    
    // 특정 상태의 작업들 조회
    List<RunwayJob> findByStatus(RunwayJobStatus status);
}

