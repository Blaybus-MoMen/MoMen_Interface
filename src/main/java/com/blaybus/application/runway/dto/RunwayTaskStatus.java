package com.blaybus.application.runway.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Runway 작업 상태 응답 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class RunwayTaskStatus {

    /**
     * 작업 ID
     */
    private String id;

    /**
     * 작업 상태
     * - PENDING: 대기 중
     * - RUNNING: 실행 중
     * - SUCCEEDED: 완료
     * - FAILED: 실패
     * - CANCELLED: 취소됨
     */
    private String status;

    /**
     * 진행률 (0-100)
     * Runway API는 0-1 사이의 소수점 값(예: 0.5 = 50%) 또는 0-100 사이의 정수 값을 반환할 수 있습니다.
     */
    @JsonProperty("progress")
    private Object progressRaw; // 소수점 또는 정수 모두 처리
    
    /**
     * 진행률을 정수(0-100)로 반환
     * Jackson이 이 메서드를 getter로 인식하지 않도록 @JsonIgnore 추가
     */
    @com.fasterxml.jackson.annotation.JsonIgnore
    public Integer getProgress() {
        if (progressRaw == null) {
            return null;
        }
        if (progressRaw instanceof Number) {
            Number num = (Number) progressRaw;
            // 소수점 값(0.0-1.0)인 경우 0-100으로 변환
            double doubleValue = num.doubleValue();
            if (doubleValue <= 1.0 && doubleValue >= 0.0) {
                return (int) (doubleValue * 100);
            }
            // 이미 0-100 범위인 경우
            return num.intValue();
        }
        return null;
    }

    /**
     * 에러 정보
     */
    private ErrorInfo error;

    /**
     * 생성된 비디오 정보
     * Runway API는 경우에 따라 문자열(URL) 또는 Output 객체 배열을 반환할 수 있습니다.
     */
    @JsonProperty("output")
    @JsonDeserialize(using = OutputDeserializer.class)
    private List<Output> output;

    /**
     * 작업 완료 여부
     */
    public boolean isDone() {
        return "SUCCEEDED".equalsIgnoreCase(status) ||
               "FAILED".equalsIgnoreCase(status) ||
               "CANCELLED".equalsIgnoreCase(status);
    }

    /**
     * 에러 발생 여부
     */
    public boolean hasError() {
        return error != null || "FAILED".equalsIgnoreCase(status);
    }

    /**
     * 에러 메시지 추출
     */
    public String getErrorMessage() {
        if (error != null) {
            return error.getMessage();
        }
        return "FAILED".equalsIgnoreCase(status) ? "작업이 실패했습니다" : null;
    }

    /**
     * 비디오 URL 추출 (첫 번째 비디오)
     * output이 문자열인 경우와 객체 배열인 경우를 모두 처리합니다.
     */
    public String getVideoUrl() {
        if (output != null && !output.isEmpty() && output.get(0).getUrl() != null) {
            return output.get(0).getUrl();
        }
        return null;
    }

    /**
     * 에러 정보
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ErrorInfo {
        private String code;
        private String message;
    }

    /**
     * 출력 정보
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Output {
        /**
         * 생성된 비디오 URL
         */
        private String url;

        /**
         * 비디오 길이 (초)
         */
        private Double duration;

        /**
         * 파일 크기 (바이트)
         */
        private Long size;
    }
    
    /**
     * Output 필드의 커스텀 deserializer
     * Runway API가 문자열 또는 객체 배열을 반환하는 경우를 모두 처리합니다.
     */
    public static class OutputDeserializer extends JsonDeserializer<List<Output>> {
        @Override
        public List<Output> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            JsonNode node = p.getCodec().readTree(p);
            List<Output> outputs = new ArrayList<>();
            
            if (node.isTextual()) {
                // 문자열인 경우: URL만 있는 Output 객체 생성
                Output output = new Output();
                output.setUrl(node.asText());
                outputs.add(output);
            } else if (node.isArray()) {
                // 배열인 경우: 각 요소를 Output 객체로 변환
                for (JsonNode item : node) {
                    if (item.isTextual()) {
                        // 배열 요소가 문자열인 경우
                        Output output = new Output();
                        output.setUrl(item.asText());
                        outputs.add(output);
                    } else if (item.isObject()) {
                        // 배열 요소가 객체인 경우
                        Output output = p.getCodec().treeToValue(item, Output.class);
                        outputs.add(output);
                    }
                }
            } else if (node.isObject()) {
                // 단일 객체인 경우
                Output output = p.getCodec().treeToValue(node, Output.class);
                outputs.add(output);
            }
            
            return outputs;
        }
    }
}
