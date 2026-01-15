package com.blaybus.domain.user;

import com.blaybus.core.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 사용자 엔티티
 */
@Entity
@Table(name = "tbl_user")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "USER_ID")
    private Long id;

    @Column(name = "EMAIL", unique = true, length = 255)
    private String email;

    @Column(name = "PASSWORD_HASH", length = 255)
    private String passwordHash;

    @Column(name = "NAME", length = 255)
    private String name;

    @Column(name = "IS_ACTIVE", nullable = false, columnDefinition = "TINYINT(1) DEFAULT 1")
    private Boolean isActive;

    @Column(name = "PHONE", length = 20)
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(name = "ROLE", nullable = false, length = 20)
    private UserRole role;

    @Column(name = "EMAIL_VERIFIED", nullable = false, columnDefinition = "TINYINT(1) DEFAULT 0")
    private Boolean emailVerified;

    @Column(name = "EMAIL_VERIFIED_AT")
    private LocalDateTime emailVerifiedAt;

    @Column(name = "TERMS_AGREED_AT")
    private LocalDateTime termsAgreedAt;

    // OAuth 관련 필드
    @Enumerated(EnumType.STRING)
    @Column(name = "OAUTH_PROVIDER", length = 20)
    private OAuthProvider oauthProvider;

    @Column(name = "OAUTH_ID", length = 255)
    private String oauthId;

    @Column(name = "OAUTH_CONNECTED_AT")
    private LocalDateTime oauthConnectedAt;

    @Column(name = "PROFILE_IMAGE_URL", length = 500)
    private String profileImageUrl;

    @Builder
    public User(String email, String passwordHash, String name, String phone, UserRole role) {
        this.email = email;
        this.passwordHash = passwordHash;
        this.name = name;
        this.phone = phone;
        this.role = role != null ? role : UserRole.STUDENT; // 기본값: STUDENT
        this.isActive = true;
        this.emailVerified = false;
    }

    // 비밀번호 변경
    public void changePassword(String newPasswordHash) {
        this.passwordHash = newPasswordHash;
    }

    // 사용자 비활성화
    public void deactivate() {
        this.isActive = false;
    }

    // 사용자 활성화
    public void activate() {
        this.isActive = true;
    }

    // 이메일 인증 완료
    public void verifyEmail() {
        this.emailVerified = true;
        this.emailVerifiedAt = LocalDateTime.now();
    }

    // 약관 동의
    public void agreeToTerms() {
        this.termsAgreedAt = LocalDateTime.now();
    }

    // 연락처 업데이트
    public void updatePhone(String phone) {
        this.phone = phone;
    }

    // 이름 변경
    public void updateName(String name) {
        this.name = name;
    }

    /**
     * 이메일 변경
     */
    public void updateEmail(String email) {
        this.email = email;
        this.emailVerified = false; // 이메일 변경 시 재인증 필요
        this.emailVerifiedAt = null;
    }

    /**
     * 회원 정보 업데이트
     */
    public void updateProfile(String name, String phone) {
        if (name != null && !name.trim().isEmpty()) {
            this.name = name;
        }
        if (phone != null) {
            this.phone = phone;
        }
    }

    // 역할 변경
    public void changeRole(UserRole role) {
        this.role = role;
    }

    // OAuth 사용자 생성을 위한 정적 팩토리 메서드
    public static User createOAuthUser(String email, String name, OAuthProvider provider,
                                        String oauthId, String profileImageUrl) {
        User user = new User();
        user.email = email;
        user.name = name;
        user.oauthProvider = provider;
        user.oauthId = oauthId;
        user.oauthConnectedAt = LocalDateTime.now();
        user.profileImageUrl = profileImageUrl;
        user.role = UserRole.STUDENT;
        user.isActive = true;
        user.emailVerified = true;  // 소셜 로그인은 이메일 인증 완료로 간주
        user.emailVerifiedAt = LocalDateTime.now();
        return user;
    }

    // OAuth 연결
    public void connectOAuth(OAuthProvider provider, String oauthId, String profileImageUrl) {
        this.oauthProvider = provider;
        this.oauthId = oauthId;
        this.oauthConnectedAt = LocalDateTime.now();
        if (profileImageUrl != null) {
            this.profileImageUrl = profileImageUrl;
        }
    }

    // OAuth 연결 해제
    public void disconnectOAuth() {
        this.oauthProvider = null;
        this.oauthId = null;
        this.oauthConnectedAt = null;
    }

    // 프로필 이미지 업데이트
    public void updateProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    // OAuth 사용자인지 확인
    public boolean isOAuthUser() {
        return this.oauthProvider != null && this.oauthId != null;
    }
}
