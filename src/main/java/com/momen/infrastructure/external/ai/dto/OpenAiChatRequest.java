package com.momen.infrastructure.external.ai.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class OpenAiChatRequest {
    private String model;
    private List<Message> messages;
    private int max_tokens;
    private double temperature;

    @Getter
    @Builder
    public static class Message {
        private String role;
        private Object content; // String or List<Content>
    }

    @Getter
    @Builder
    public static class Content {
        private String type;
        private String text;
        private ImageUrl image_url;
    }

    @Getter
    @Builder
    public static class ImageUrl {
        private String url;
    }
}
