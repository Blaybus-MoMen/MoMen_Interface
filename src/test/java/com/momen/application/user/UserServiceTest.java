package com.momen.application.user;

import com.momen.application.user.dto.UpdateEmailRequest;
import com.momen.application.user.dto.UpdatePasswordRequest;
import com.momen.application.user.dto.UpdateProfileRequest;
import com.momen.application.user.dto.UserResponse;
import com.momen.core.error.enums.ErrorCode;
import com.momen.core.exception.BusinessException;
import com.momen.core.mapper.UserMapper;
import com.momen.domain.user.User;
import com.momen.domain.user.UserRepository;
import com.momen.domain.user.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private UserResponse testUserResponse;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .loginId("testuser")
                .email("test@example.com")
                .passwordHash("encodedPassword")
                .name("테스트")
                .role(UserRole.MENTEE)
                .build();

        testUserResponse = UserResponse.builder()
                .id(1L)
                .email("test@example.com")
                .name("테스트")
                .role(UserRole.MENTEE)
                .isActive(true)
                .build();
    }

    @Test
    @DisplayName("getUserInfo - 유저 조회 성공")
    void getUserInfo_성공() {
        given(userRepository.findById(1L)).willReturn(Optional.of(testUser));
        given(userMapper.toResponse(testUser)).willReturn(testUserResponse);

        UserResponse result = userService.getUserInfo(1L);

        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo("test@example.com");
        assertThat(result.getName()).isEqualTo("테스트");
        verify(userRepository).findById(1L);
    }

    @Test
    @DisplayName("getUserInfo - 유저 없음 예외")
    void getUserInfo_유저없음_예외() {
        given(userRepository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserInfo(999L))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.USER_NOT_FOUND));
    }

    @Test
    @DisplayName("updateProfile - 프로필 업데이트 성공")
    void updateProfile_성공() {
        UpdateProfileRequest request = new UpdateProfileRequest("새이름", "010-1234-5678");

        given(userRepository.findById(1L)).willReturn(Optional.of(testUser));
        given(userRepository.save(any(User.class))).willReturn(testUser);
        given(userMapper.toResponse(any(User.class))).willReturn(testUserResponse);

        UserResponse result = userService.updateProfile(1L, request);

        assertThat(result).isNotNull();
        verify(userRepository).save(testUser);
    }

    @Test
    @DisplayName("updateEmail - 이메일 변경 성공")
    void updateEmail_성공() {
        UpdateEmailRequest request = new UpdateEmailRequest("new@example.com");

        given(userRepository.findById(1L)).willReturn(Optional.of(testUser));
        given(userRepository.existsByEmail("new@example.com")).willReturn(false);
        given(userRepository.save(any(User.class))).willReturn(testUser);
        given(userMapper.toResponse(any(User.class))).willReturn(testUserResponse);

        UserResponse result = userService.updateEmail(1L, request);

        assertThat(result).isNotNull();
        verify(userRepository).save(testUser);
    }

    @Test
    @DisplayName("updateEmail - 중복 이메일 예외")
    void updateEmail_중복이메일_예외() {
        UpdateEmailRequest request = new UpdateEmailRequest("existing@example.com");

        given(userRepository.findById(1L)).willReturn(Optional.of(testUser));
        given(userRepository.existsByEmail("existing@example.com")).willReturn(true);

        assertThatThrownBy(() -> userService.updateEmail(1L, request))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.EMAIL_ALREADY_EXISTS));
    }

    @Test
    @DisplayName("updatePassword - 비밀번호 변경 성공")
    void updatePassword_성공() {
        UpdatePasswordRequest request = new UpdatePasswordRequest("currentPw", "newPw");

        given(userRepository.findById(1L)).willReturn(Optional.of(testUser));
        given(passwordEncoder.matches("currentPw", "encodedPassword")).willReturn(true);
        given(passwordEncoder.encode("newPw")).willReturn("newEncodedPassword");

        userService.updatePassword(1L, request);

        verify(userRepository).save(testUser);
        verify(passwordEncoder).encode("newPw");
    }

    @Test
    @DisplayName("updatePassword - 현재 비밀번호 불일치 예외")
    void updatePassword_현재비밀번호불일치_예외() {
        UpdatePasswordRequest request = new UpdatePasswordRequest("wrongPw", "newPw");

        given(userRepository.findById(1L)).willReturn(Optional.of(testUser));
        given(passwordEncoder.matches("wrongPw", "encodedPassword")).willReturn(false);

        assertThatThrownBy(() -> userService.updatePassword(1L, request))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.INVALID_CREDENTIALS));
    }
}
