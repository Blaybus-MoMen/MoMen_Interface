package com.momen.application.planner.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * 할일 일괄 생성 / 요일 반복 생성 응답.
 */
@Getter
@Builder
public class TodoBatchCreateResponse {
    /** 생성된 할일 ID 목록 */
    private List<Long> todoIds;
    /** 생성된 할일 개수 */
    private int count;
}
