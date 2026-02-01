package com.momen.application.mentoring.dto;

import com.momen.domain.mentoring.Mentee;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MenteeResponse {
    private Long menteeId;
    private Long userId;
    private String name;
    private String grade;
    private String targetUniversity;

    public static MenteeResponse from(Mentee mentee) {
        return MenteeResponse.builder()
                .menteeId(mentee.getId())
                .userId(mentee.getUser().getId())
                .name(mentee.getUser().getName())
                .grade(mentee.getGrade())
                .targetUniversity(mentee.getTargetUniversity())
                .build();
    }
}
