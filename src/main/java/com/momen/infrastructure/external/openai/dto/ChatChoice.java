package com.momen.infrastructure.external.openai.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * OpenAI Chat Choice DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatChoice {
    private ChatMessage message;
}

