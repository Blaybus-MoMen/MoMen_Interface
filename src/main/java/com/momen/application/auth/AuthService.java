package com.momen.application.auth;

import com.momen.application.auth.dto.LoginRequest;
import com.momen.application.auth.dto.SignupRequest;
import com.momen.application.auth.dto.TokenResponse;
import com.momen.core.error.enums.ErrorCode;
import com.momen.core.exception.BusinessException;
import com.momen.domain.user.User;
import com.momen.domain.user.UserRepository;
import com.momen.infrastructure.redis.EmailVerificationRedisService;
import com.momen.infrastructure.redis.TokenRedisService;
import com.momen.infrastructure.security.JwtTokenProvider;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 인증 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final TokenRedisService tokenRedisService;
    private final EmailVerificationRedisService emailVerificationRedisService;

    // 회원가입
    @Transactional
    public TokenResponse signup(SignupRequest request) {

        // 이메일 중복 체크
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }

        // 이메일 인증 완료 여부 확인 (Redis에서 확인)
        if (!emailVerificationRedisService.isVerified(request.getEmail())) {
            throw new BusinessException(ErrorCode.EMAIL_NOT_VERIFIED);
        }

        // 비밀번호 해시화
        String passwordHash = passwordEncoder.encode(request.getPassword());

        // User 생성
        User user = User.builder()
                .email(request.getEmail())
                .passwordHash(passwordHash)
                .name(request.getName())
                .phone(request.getPhone())
                .role(request.getRole()) // null이면 User 엔티티에서 STUDENT로 기본값 설정됨
                .build();

        // 이메일 인증 완료 상태로 설정
        user.verifyEmail();

        // 저장
        User savedUser = userRepository.save(user);

        // JWT 토큰 생성
        String accessToken = jwtTokenProvider.createAccessToken(savedUser.getId(), savedUser.getEmail());
        String refreshToken = jwtTokenProvider.createRefreshToken(savedUser.getId(), savedUser.getEmail());

        // Redis에 Refresh Token 저장 (기존 토큰은 자동으로 덮어씌워짐)
        tokenRedisService.saveRefreshToken(savedUser.getId(), refreshToken);

        return new TokenResponse(accessToken, refreshToken, savedUser.getId(), savedUser.getEmail(), savedUser.getName(), savedUser.getRole().name());
    }

    // 로그인
    @Transactional
    public TokenResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_CREDENTIALS));

        // 비활성화된 계정 체크
        if (!user.getIsActive()) {
            throw new BusinessException(ErrorCode.ACCOUNT_INACTIVE);
        }

        // 비밀번호 검증
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        }

        // JWT 토큰 생성
        String accessToken = jwtTokenProvider.createAccessToken(user.getId(), user.getEmail());
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getId(), user.getEmail());

        // Redis에 Refresh Token 저장 (기존 토큰은 자동으로 덮어씌워짐)
        tokenRedisService.saveRefreshToken(user.getId(), refreshToken);

        return new TokenResponse(accessToken, refreshToken, user.getId(), user.getEmail(), user.getName(), user.getRole().name());
    }

    // Refresh Token을 사용해 Access Token 재발급
    @Transactional
    public TokenResponse refresh(String refreshToken) {
        // 토큰 유효성 검사 (서명/만료)
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        }

        // Redis에 저장된 Refresh Token인지 확인
        Long userId = tokenRedisService.getUserIdByRefreshToken(refreshToken);
        if (userId == null || !tokenRedisService.existsRefreshToken(userId)) {
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        }

        User user = userRepository.findById(userId).orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 토큰 재발급
        String newAccessToken = jwtTokenProvider.createAccessToken(user.getId(), user.getEmail());

        return new TokenResponse(newAccessToken, refreshToken, user.getId(), user.getEmail(), user.getName(), user.getRole().name());
    }

    // 로그아웃
    @Transactional
    public void logout(Long userId, String accessToken) {
        // 사용자 확인
        User user = userRepository.findById(userId).orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // Redis에서 Refresh Token 삭제
        tokenRedisService.deleteRefreshToken(userId);

        // Access Token을 블랙리스트에 추가 (만료 시간까지)
        if (accessToken != null && jwtTokenProvider.validateToken(accessToken)) {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(jwtTokenProvider.getSecretKey())
                    .build()
                    .parseClaimsJws(accessToken)
                    .getBody();
            long expirationTime = claims.getExpiration().getTime();
            tokenRedisService.addToBlacklist(accessToken, expirationTime);
        }
    }
}
