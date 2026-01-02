package com.blaybus.infrastructure.external.openai.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * OpenAI Embedding 응답 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmbeddingResponse {
    private List<EmbeddingData> data;
}

