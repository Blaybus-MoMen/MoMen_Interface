package com.momen.application.planner.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class TodoCreateRequest {
    private String title;
    private String subject; // KOREAN, MATH, ENGLISH
    private String goalDescription;
    private Boolean isFixed;
    private String worksheetFileUrl;
}
