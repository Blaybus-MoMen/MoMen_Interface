package com.momen.domain.user;

import com.momen.core.entity.BaseTimeEntity;
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

    @Column(name = "LOGIN_ID", unique = true, nullable = false, length = 50)
    private String loginId;

    @Column(name = "EMAIL", length = 255)
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

    @Column(name = "PROFILE_IMAGE_URL", length = 500)
    private String profileImageUrl;

    @Builder
    public User(String loginId, String email, String passwordHash, String name, String phone, UserRole role) {
        this.loginId = loginId;
        this.email = email;
        this.passwordHash = passwordHash;
        this.name = name;
        this.phone = phone;
        this.role = role != null ? role : UserRole.MENTEE;
        this.isActive = true;
        this.emailVerified = false;
    }

    public void changePassword(String newPasswordHash) {
        this.passwordHash = newPasswordHash;
    }

    public void deactivate() {
        this.isActive = false;
    }

    public void activate() {
        this.isActive = true;
    }

    public void verifyEmail() {
        this.emailVerified = true;
        this.emailVerifiedAt = LocalDateTime.now();
    }

    public void agreeToTerms() {
        this.termsAgreedAt = LocalDateTime.now();
    }

    public void updatePhone(String phone) {
        this.phone = phone;
    }

    public void updateName(String name) {
        this.name = name;
    }

    public void updateEmail(String email) {
        this.email = email;
        this.emailVerified = false;
        this.emailVerifiedAt = null;
    }

    public void updateProfile(String name, String phone) {
        if (name != null && !name.trim().isEmpty()) {
            this.name = name;
        }
        if (phone != null) {
            this.phone = phone;
        }
    }

    public void changeRole(UserRole role) {
        this.role = role;
    }

    public void updateProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }
}
