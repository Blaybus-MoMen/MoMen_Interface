package com.momen.infrastructure.external.image.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImageGenerationResponse {
    private List<ImageData> data;
}


