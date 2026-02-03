package com.momen.application.planner.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 할일 일괄 생성 요청.
 * 한 플래너(또는 멘티+날짜)에 여러 할일을 한 번에 등록할 때 사용.
 */
@Getter
@NoArgsConstructor
public class TodoBatchCreateRequest {

    @NotEmpty(message = "할일 목록은 1개 이상이어야 합니다")
    @Valid
    private List<TodoCreateRequest> items;

    public TodoBatchCreateRequest(List<TodoCreateRequest> items) {
        this.items = items;
    }
}
