package com.blaybus.infrastructure.external.openai.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * OpenAI Chat Completion 응답 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatCompletionResponse {
    private List<ChatChoice> choices;
}

