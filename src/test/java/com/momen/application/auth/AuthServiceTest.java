package com.momen.application.auth;

import com.momen.application.auth.dto.LoginRequest;
import com.momen.application.auth.dto.SignupRequest;
import com.momen.application.auth.dto.TokenResponse;
import com.momen.domain.mentoring.Mentee;
import com.momen.domain.mentoring.Mentor;
import com.momen.domain.user.User;
import com.momen.domain.user.UserRepository;
import com.momen.domain.user.UserRole;
import com.momen.infrastructure.jpa.mentoring.MenteeRepository;
import com.momen.infrastructure.jpa.mentoring.MentorRepository;
import com.momen.infrastructure.redis.TokenRedisService;
import com.momen.infrastructure.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private MentorRepository mentorRepository;

    @Mock
    private MenteeRepository menteeRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private TokenRedisService tokenRedisService;

    @InjectMocks
    private AuthService authService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .loginId("testuser")
                .email("test@example.com")
                .passwordHash("encodedPassword")
                .name("테스트")
                .role(UserRole.MENTEE)
                .build();
        ReflectionTestUtils.setField(testUser, "id", 1L);
    }

    @Test
    @DisplayName("signup - 멘토 회원가입 성공")
    void signup_멘토_성공() {
        SignupRequest request = createSignupRequest("MENTOR");

        given(userRepository.existsByLoginId("newuser")).willReturn(false);
        given(passwordEncoder.encode("password123")).willReturn("encodedPassword");
        given(userRepository.save(any(User.class))).willAnswer(invocation -> {
            User user = invocation.getArgument(0);
            ReflectionTestUtils.setField(user, "id", 1L);
            return user;
        });
        given(mentorRepository.save(any(Mentor.class))).willAnswer(invocation -> invocation.getArgument(0));
        given(jwtTokenProvider.createAccessToken(anyLong(), anyString())).willReturn("access-token");
        given(jwtTokenProvider.createRefreshToken(anyLong(), anyString())).willReturn("refresh-token");

        TokenResponse result = authService.signup(request);

        assertThat(result.getAccessToken()).isEqualTo("access-token");
        assertThat(result.getRefreshToken()).isEqualTo("refresh-token");
        verify(mentorRepository).save(any(Mentor.class));
    }

    @Test
    @DisplayName("signup - 멘티 회원가입 성공")
    void signup_멘티_성공() {
        SignupRequest request = createSignupRequest("MENTEE");

        given(userRepository.existsByLoginId("newuser")).willReturn(false);
        given(passwordEncoder.encode("password123")).willReturn("encodedPassword");
        given(userRepository.save(any(User.class))).willAnswer(invocation -> {
            User user = invocation.getArgument(0);
            ReflectionTestUtils.setField(user, "id", 1L);
            return user;
        });
        given(menteeRepository.save(any(Mentee.class))).willAnswer(invocation -> invocation.getArgument(0));
        given(jwtTokenProvider.createAccessToken(anyLong(), anyString())).willReturn("access-token");
        given(jwtTokenProvider.createRefreshToken(anyLong(), anyString())).willReturn("refresh-token");

        TokenResponse result = authService.signup(request);

        assertThat(result.getAccessToken()).isEqualTo("access-token");
        verify(menteeRepository).save(any(Mentee.class));
    }

    @Test
    @DisplayName("signup - 중복 아이디 예외")
    void signup_중복아이디_예외() {
        SignupRequest request = createSignupRequest("MENTEE");

        given(userRepository.existsByLoginId("newuser")).willReturn(true);

        assertThatThrownBy(() -> authService.signup(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("이미 존재하는 아이디");
    }

    @Test
    @DisplayName("login - 로그인 성공")
    void login_성공() {
        LoginRequest request = createLoginRequest();

        given(userRepository.findByLoginId("testuser")).willReturn(Optional.of(testUser));
        given(passwordEncoder.matches("password123", "encodedPassword")).willReturn(true);
        given(jwtTokenProvider.createAccessToken(1L, "testuser")).willReturn("access-token");
        given(jwtTokenProvider.createRefreshToken(1L, "testuser")).willReturn("refresh-token");

        TokenResponse result = authService.login(request);

        assertThat(result.getAccessToken()).isEqualTo("access-token");
        assertThat(result.getRefreshToken()).isEqualTo("refresh-token");
        assertThat(result.getUserId()).isEqualTo(1L);
        verify(tokenRedisService).saveRefreshToken(1L, "refresh-token");
    }

    @Test
    @DisplayName("login - 비밀번호 불일치 예외")
    void login_비밀번호불일치_예외() {
        LoginRequest request = createLoginRequest();

        given(userRepository.findByLoginId("testuser")).willReturn(Optional.of(testUser));
        given(passwordEncoder.matches("password123", "encodedPassword")).willReturn(false);

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("아이디 또는 비밀번호가 올바르지 않습니다");
    }

    @Test
    @DisplayName("login - 아이디 없음 예외")
    void login_아이디없음_예외() {
        LoginRequest request = createLoginRequest();

        given(userRepository.findByLoginId("testuser")).willReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("아이디 또는 비밀번호가 올바르지 않습니다");
    }

    @Test
    @DisplayName("refresh - 토큰 갱신 성공")
    void refresh_성공() {
        given(tokenRedisService.existsRefreshTokenByToken("old-refresh")).willReturn(true);
        given(jwtTokenProvider.validateToken("old-refresh")).willReturn(true);
        given(jwtTokenProvider.getUserIdFromToken("old-refresh")).willReturn(1L);
        given(jwtTokenProvider.getLoginIdFromToken("old-refresh")).willReturn("testuser");
        given(jwtTokenProvider.createAccessToken(1L, "testuser")).willReturn("new-access");
        given(jwtTokenProvider.createRefreshToken(1L, "testuser")).willReturn("new-refresh");
        given(userRepository.findById(1L)).willReturn(Optional.of(testUser));

        TokenResponse result = authService.refresh("old-refresh");

        assertThat(result.getAccessToken()).isEqualTo("new-access");
        assertThat(result.getRefreshToken()).isEqualTo("new-refresh");
        verify(tokenRedisService).deleteRefreshTokenByToken("old-refresh");
        verify(tokenRedisService).saveRefreshToken(1L, "new-refresh");
    }

    @Test
    @DisplayName("logout - 로그아웃 성공")
    void logout_성공() {
        given(jwtTokenProvider.getUserIdFromToken("access-token")).willReturn(1L);

        authService.logout(1L, "access-token");

        verify(tokenRedisService).deleteRefreshToken(1L);
        verify(tokenRedisService).addToBlacklist(eq("access-token"), anyLong());
    }

    // === Helper methods ===

    private SignupRequest createSignupRequest(String role) {
        SignupRequest request = new SignupRequest();
        ReflectionTestUtils.setField(request, "loginId", "newuser");
        ReflectionTestUtils.setField(request, "email", "new@example.com");
        ReflectionTestUtils.setField(request, "password", "password123");
        ReflectionTestUtils.setField(request, "name", "새유저");
        ReflectionTestUtils.setField(request, "role", role);
        if ("MENTOR".equals(role)) {
            ReflectionTestUtils.setField(request, "intro", "멘토 소개");
        } else {
            ReflectionTestUtils.setField(request, "grade", "고1");
        }
        return request;
    }

    private LoginRequest createLoginRequest() {
        LoginRequest request = new LoginRequest();
        ReflectionTestUtils.setField(request, "loginId", "testuser");
        ReflectionTestUtils.setField(request, "password", "password123");
        return request;
    }
}
