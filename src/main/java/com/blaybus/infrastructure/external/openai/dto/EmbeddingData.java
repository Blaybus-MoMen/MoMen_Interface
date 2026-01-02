package com.blaybus.infrastructure.external.openai.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * OpenAI Embedding 데이터 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmbeddingData {
    private double[] embedding;
}

