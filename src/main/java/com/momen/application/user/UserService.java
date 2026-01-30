package com.momen.application.user;

import com.momen.application.user.dto.OnboardingRequest;
import com.momen.application.user.dto.UpdateEmailRequest;
import com.momen.application.user.dto.UpdatePasswordRequest;
import com.momen.application.user.dto.UpdateProfileRequest;
import com.momen.application.user.dto.UserResponse;
import com.momen.core.error.enums.ErrorCode;
import com.momen.core.exception.BusinessException;
import com.momen.core.mapper.UserMapper;
import com.momen.domain.user.User;
import com.momen.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 사용자 관리 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    // 사용자 정보 조회
    public UserResponse getUserInfo(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        return userMapper.toResponse(user);
    }

    // 회원 정보 수정 (이름, 연락처, 학번)
    @Transactional
    public UserResponse updateProfile(Long userId, UpdateProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        user.updateProfile(request.getName(), request.getPhone());

        User updatedUser = userRepository.save(user);

        return userMapper.toResponse(updatedUser);
    }

    /**
     * 이메일 변경 (재인증 필요)
     */
    @Transactional
    public UserResponse updateEmail(Long userId, UpdateEmailRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 이메일 중복 체크
        if (userRepository.existsByEmail(request.getNewEmail())) {
            throw new BusinessException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }

        user.updateEmail(request.getNewEmail());
        User updatedUser = userRepository.save(user);

        return userMapper.toResponse(updatedUser);
    }

    /**
     * 비밀번호 변경
     */
    @Transactional
    public void updatePassword(Long userId, UpdatePasswordRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 현재 비밀번호 확인
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        }

        // 새 비밀번호 해시화
        String newPasswordHash = passwordEncoder.encode(request.getNewPassword());
        user.changePassword(newPasswordHash);

        userRepository.save(user);
    }

    /**
     * 신규 OAuth 사용자 온보딩 (추가 정보 입력)
     * 전화번호, 서비스 이용약관 동의 등을 한 번에 받을 때 사용
     */
    @Transactional
    public UserResponse completeOnboarding(Long userId, OnboardingRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (Boolean.TRUE.equals(request.getTermsAgreed())) {
            user.agreeToTerms();
        }

        if (request.getPhone() != null && !request.getPhone().isBlank()) {
            user.updatePhone(request.getPhone().trim());
        }

        User updatedUser = userRepository.save(user);
        return userMapper.toResponse(updatedUser);
    }
}
