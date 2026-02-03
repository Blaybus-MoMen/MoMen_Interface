package com.momen.application.planner.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 할일 일괄 수정 요청.
 * 여러 할일의 완료 여부, 공부 시간 등을 한 번에 수정할 때 사용.
 */
@Getter
@NoArgsConstructor
public class TodoBatchUpdateRequest {

    @NotEmpty(message = "수정할 할일 목록은 1개 이상이어야 합니다")
    @Valid
    private List<TodoBatchUpdateItem> items;

    public TodoBatchUpdateRequest(List<TodoBatchUpdateItem> items) {
        this.items = items;
    }

    @Getter
    @NoArgsConstructor
    public static class TodoBatchUpdateItem {
        @NotNull(message = "할일 ID는 필수입니다")
        private Long todoId;

        @Valid
        private TodoUpdateRequest patch;

        public TodoBatchUpdateItem(Long todoId, TodoUpdateRequest patch) {
            this.todoId = todoId;
            this.patch = patch;
        }
    }
}
