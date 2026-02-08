package com.momen.application.mentoring.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(description = "AI 튜터 채팅 요청")
public class ChatRequest {

    @Schema(description = "채팅 메시지", example = "이 문제 풀이 방법을 알려줘")
    private String message;
}
