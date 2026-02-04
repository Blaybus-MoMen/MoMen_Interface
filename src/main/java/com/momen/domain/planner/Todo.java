package com.momen.domain.planner;

import com.momen.core.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "todos")
public class Todo extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "todo_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "planner_id", nullable = false)
    private Planner planner;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, length = 20)
    private String subject; // KOREAN, MATH, ENGLISH, ETC

    @Column(name = "goal_description", columnDefinition = "TEXT")
    private String goalDescription;

    @Column(name = "study_time")
    private Integer studyTime; // 공부 시간 (분)

    @Column(name = "is_completed")
    private Boolean isCompleted = false;

    @Column(name = "is_fixed")
    private Boolean isFixed = false; // 멘토가 지정한 과제 여부

    @Column(name = "created_by", nullable = false)
    private Long createdBy; // 작성자 ID

    @Column(name = "worksheet_file_url", length = 500)
    private String worksheetFileUrl; // 학습지파일 URL, nullable

    @Column(name = "mentor_confirmed")
    private Boolean mentorConfirmed = false; // 멘토확인여부

    @Column(name = "repeat_group_id", length = 36)
    private String repeatGroupId; // 반복 그룹 UUID, nullable

    @Column(name = "repeat_days", length = 100)
    private String repeatDays; // "MONDAY,WEDNESDAY", nullable

    public Todo(Planner planner, String title, String subject, String goalDescription,
                Boolean isFixed, Long createdBy, String worksheetFileUrl) {
        this.planner = planner;
        this.title = title;
        this.subject = subject;
        this.goalDescription = goalDescription;
        this.isFixed = isFixed;
        this.createdBy = createdBy;
        this.worksheetFileUrl = worksheetFileUrl;
    }

    public void complete() {
        this.isCompleted = true;
    }

    public void uncomplete() {
        this.isCompleted = false;
    }

    public void updateStudyTime(Integer studyTime) {
        this.studyTime = studyTime;
    }

    public void updateContent(String title, String subject, String goalDescription,
                              String worksheetFileUrl, Boolean mentorConfirmed) {
        this.title = title;
        this.subject = subject;
        this.goalDescription = goalDescription;
        this.worksheetFileUrl = worksheetFileUrl;
        this.mentorConfirmed = mentorConfirmed;
    }

    public void reassignPlanner(Planner newPlanner) {
        this.planner = newPlanner;
    }

    public void assignRepeatGroup(String repeatGroupId, String repeatDays) {
        this.repeatGroupId = repeatGroupId;
        this.repeatDays = repeatDays;
    }

    public void detachFromRepeatGroup() {
        this.repeatGroupId = null;
        this.repeatDays = null;
    }
}
