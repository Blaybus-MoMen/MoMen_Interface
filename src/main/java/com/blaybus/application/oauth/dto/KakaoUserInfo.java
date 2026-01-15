package com.blaybus.application.oauth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 카카오 사용자 정보 DTO
 * 카카오 API로부터 받는 사용자 정보
 */
@Getter
@Setter
@NoArgsConstructor
public class KakaoUserInfo {

    @JsonProperty("id")
    private Long id;

    @JsonProperty("connected_at")
    private String connectedAt;

    @JsonProperty("kakao_account")
    private KakaoAccount kakaoAccount;

    @Getter
    @Setter
    @NoArgsConstructor
    public static class KakaoAccount {

        @JsonProperty("profile_needs_agreement")
        private Boolean profileNeedsAgreement;

        @JsonProperty("profile")
        private Profile profile;

        @JsonProperty("email_needs_agreement")
        private Boolean emailNeedsAgreement;

        @JsonProperty("is_email_valid")
        private Boolean isEmailValid;

        @JsonProperty("is_email_verified")
        private Boolean isEmailVerified;

        @JsonProperty("email")
        private String email;

        @JsonProperty("name_needs_agreement")
        private Boolean nameNeedsAgreement;

        @JsonProperty("name")
        private String name;

        @Getter
        @Setter
        @NoArgsConstructor
        public static class Profile {
            @JsonProperty("nickname")
            private String nickname;

            @JsonProperty("thumbnail_image_url")
            private String thumbnailImageUrl;

            @JsonProperty("profile_image_url")
            private String profileImageUrl;

            @JsonProperty("is_default_image")
            private Boolean isDefaultImage;
        }
    }

    /**
     * 이메일 추출
     */
    public String getEmail() {
        if (kakaoAccount != null) {
            return kakaoAccount.getEmail();
        }
        return null;
    }

    /**
     * 닉네임 추출 (이름이 없으면 닉네임 사용)
     */
    public String getName() {
        if (kakaoAccount != null) {
            // 실명이 있으면 실명 사용
            if (kakaoAccount.getName() != null && !kakaoAccount.getName().isEmpty()) {
                return kakaoAccount.getName();
            }
            // 프로필의 닉네임 사용
            if (kakaoAccount.getProfile() != null && kakaoAccount.getProfile().getNickname() != null) {
                return kakaoAccount.getProfile().getNickname();
            }
        }
        return null;
    }

    /**
     * 프로필 이미지 URL 추출
     */
    public String getProfileImageUrl() {
        if (kakaoAccount != null && kakaoAccount.getProfile() != null) {
            return kakaoAccount.getProfile().getProfileImageUrl();
        }
        return null;
    }

    /**
     * 카카오 고유 ID를 문자열로 반환
     */
    public String getOAuthId() {
        return String.valueOf(id);
    }
}
