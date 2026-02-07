package com.momen.application.auth;

import com.momen.application.auth.dto.LoginRequest;
import com.momen.application.auth.dto.SignupRequest;
import com.momen.application.auth.dto.TokenResponse;
import com.momen.application.user.dto.UserUpdateRequest;
import com.momen.domain.mentoring.Mentee;
import com.momen.domain.mentoring.Mentor;
import com.momen.domain.user.User;
import com.momen.domain.user.UserRole;
import com.momen.infrastructure.jpa.mentoring.MenteeRepository;
import com.momen.infrastructure.jpa.mentoring.MentorRepository;
import com.momen.domain.user.UserRepository;
import com.momen.infrastructure.redis.TokenRedisService;
import com.momen.infrastructure.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final MentorRepository mentorRepository;
    private final MenteeRepository menteeRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final TokenRedisService tokenRedisService;

    @Transactional
    public TokenResponse signup(SignupRequest request) {

        if (userRepository.existsByLoginId(request.getLoginId())) {
            throw new IllegalArgumentException("이미 존재하는 아이디입니다");
        }

        UserRole role = "MENTOR".equals(request.getRole()) ? UserRole.MENTOR : UserRole.MENTEE;

        User user = User.builder()
                .loginId(request.getLoginId())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .name(request.getName())
                .role(role)
                .build();
        userRepository.save(user);

        if (role == UserRole.MENTOR) {
            Mentor mentor = new Mentor(user, request.getIntro());
            mentorRepository.save(mentor);
        } else {
            Mentee mentee = new Mentee(user, null, request.getGrade());
            menteeRepository.save(mentee);
        }

        String accessToken = jwtTokenProvider.createAccessToken(user.getId(), user.getLoginId());
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getId(), user.getLoginId());
        tokenRedisService.saveRefreshToken(user.getId(), refreshToken);

        return TokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .userId(user.getId())
                .name(user.getName())
                .role(user.getRole().name())
                .build();
    }

    @Transactional(readOnly = true)
    public TokenResponse login(LoginRequest request) {
        User user = userRepository.findByLoginId(request.getLoginId())
                .orElseThrow(() -> new IllegalArgumentException("아이디 또는 비밀번호가 올바르지 않습니다"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("아이디 또는 비밀번호가 올바르지 않습니다");
        }

        String accessToken = jwtTokenProvider.createAccessToken(user.getId(), user.getLoginId());
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getId(), user.getLoginId());
        tokenRedisService.saveRefreshToken(user.getId(), refreshToken);

        return TokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .userId(user.getId())
                .name(user.getName())
                .role(user.getRole().name())
                .build();
    }

    @Transactional(readOnly = true)
    public TokenResponse refresh(String refreshToken) {
        if (!tokenRedisService.existsRefreshTokenByToken(refreshToken)) {
            throw new IllegalArgumentException("유효하지 않은 Refresh Token입니다");
        }

        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new IllegalArgumentException("만료되거나 유효하지 않은 Refresh Token입니다");
        }

        Long userId = jwtTokenProvider.getUserIdFromToken(refreshToken);
        String loginId = jwtTokenProvider.getLoginIdFromToken(refreshToken);

        // 기존 refresh token 삭제
        tokenRedisService.deleteRefreshTokenByToken(refreshToken);

        // 새 토큰 발급
        String newAccessToken = jwtTokenProvider.createAccessToken(userId, loginId);
        String newRefreshToken = jwtTokenProvider.createRefreshToken(userId, loginId);
        tokenRedisService.saveRefreshToken(userId, newRefreshToken);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));

        return TokenResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .userId(userId)
                .name(user.getName())
                .role(user.getRole().name())
                .build();
    }

    @Transactional
    public void logout(Long userId, String accessToken) {
        // Refresh Token 삭제
        tokenRedisService.deleteRefreshToken(userId);

        // Access Token 블랙리스트에 추가
        if (accessToken != null) {
            try {
                long expiration = jwtTokenProvider.getUserIdFromToken(accessToken);
                // 남은 만료 시간만큼 블랙리스트에 유지
                tokenRedisService.addToBlacklist(accessToken, System.currentTimeMillis() + 1800000);
            } catch (Exception e) {
                // 이미 만료된 토큰이면 블랙리스트 추가 불필요
            }
        }
    }

    @Transactional
    public void updateProfile(Long userId, UserUpdateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));

        if (request.getNewPassword() != null && !request.getNewPassword().isBlank()) {
            if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
                throw new IllegalArgumentException("현재 비밀번호가 올바르지 않습니다");
            }
            user.changePassword(passwordEncoder.encode(request.getNewPassword()));
        }

        if (request.getName() != null) {
            user.updateName(request.getName());
        }
    }
}
