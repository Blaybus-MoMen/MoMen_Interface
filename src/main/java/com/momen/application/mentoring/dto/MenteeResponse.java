package com.momen.application.mentoring.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.momen.domain.mentoring.Mentee;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class MenteeResponse {
    private Long menteeId;
    private Long userId;
    private String name;
    private String profileImageUrl;
    private String grade;
    private List<String> cards;
    private List<String> subjects;

    private String cheerMessage;

    public static MenteeResponse from(Mentee mentee) {
        return MenteeResponse.builder()
                .menteeId(mentee.getId())
                .userId(mentee.getUser().getId())
                .name(mentee.getUser().getName())
                .profileImageUrl(mentee.getUser().getProfileImageUrl())
                .grade(mentee.getGrade())
                .cards(mentee.getCards())
                .subjects(mentee.getSubjects())
                .cheerMessage(mentee.getCheerMessage())
                .build();
    }

    @Getter
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class MenteeForMentorResponse {
        private Long menteeId;
        private Long userId;
        private String name;
        private String profileImageUrl;
        private String grade;
        private List<String> cards;
        private List<String> subjects;

        public static MenteeForMentorResponse from(Mentee mentee) {
            return MenteeForMentorResponse.builder()
                    .menteeId(mentee.getId())
                    .userId(mentee.getUser().getId())
                    .name(mentee.getUser().getName())
                    .profileImageUrl(mentee.getUser().getProfileImageUrl())
                    .grade(mentee.getGrade())
                    .cards(mentee.getCards())
                    .subjects(mentee.getSubjects())
                    .build();
        }
    }
}
