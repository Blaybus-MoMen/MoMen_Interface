package com.momen.application.user.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UserUpdateRequest {
    private String name;
    private String currentPassword;
    private String newPassword;

    // Mentor
    private String intro;

    // Mentee
    private String grade;
}
