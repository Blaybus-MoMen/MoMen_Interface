package com.momen.application.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SignupRequest {
    @NotBlank(message = "아이디는 필수입니다")
    private String loginId;

    @Email(message = "올바른 이메일 형식이 아닙니다")
    private String email;

    @NotBlank(message = "비밀번호는 필수입니다")
    private String password;

    @NotBlank(message = "이름은 필수입니다")
    private String name;

    @NotBlank(message = "역할은 필수입니다")
    @Pattern(regexp = "MENTOR|MENTEE", message = "역할은 MENTOR 또는 MENTEE만 가능합니다")
    private String role;

    // Mentor 전용
    private String intro;

    // Mentee 전용
    private String grade;
    private String targetUniversity;

    public boolean isMentor() {
        return "MENTOR".equals(role);
    }

    public boolean isMentee() {
        return "MENTEE".equals(role);
    }
}