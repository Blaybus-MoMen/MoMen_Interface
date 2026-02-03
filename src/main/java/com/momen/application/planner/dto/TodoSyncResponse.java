package com.momen.application.planner.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class TodoSyncResponse {
    private Long plannerId;
    private int created;
    private int updated;
    private int deleted;
    private List<TodoResponse> todos;
}
