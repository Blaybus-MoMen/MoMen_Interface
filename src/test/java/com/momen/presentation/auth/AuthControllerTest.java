package com.momen.presentation.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.momen.application.auth.AuthService;
import com.momen.application.auth.dto.TokenResponse;
import com.momen.infrastructure.redis.TokenRedisService;
import com.momen.infrastructure.security.CustomUserDetailsService;
import com.momen.infrastructure.security.JwtTokenProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @MockitoBean
    private TokenRedisService tokenRedisService;

    private final TokenResponse tokenResponse = TokenResponse.builder()
            .accessToken("access-token")
            .refreshToken("refresh-token")
            .userId(1L)
            .name("테스트")
            .role("MENTEE")
            .build();

    @Test
    @DisplayName("POST /api/v1/auth/signup - 201 Created")
    void signup_성공() throws Exception {
        given(authService.signup(any())).willReturn(tokenResponse);

        Map<String, String> request = Map.of(
                "loginId", "newuser",
                "email", "new@example.com",
                "password", "password123",
                "name", "새유저",
                "role", "MENTEE"
        );

        mockMvc.perform(post("/api/v1/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").value("access-token"))
                .andExpect(jsonPath("$.data.refreshToken").value("refresh-token"));
    }

    @Test
    @DisplayName("POST /api/v1/auth/login - 200 OK")
    void login_성공() throws Exception {
        given(authService.login(any())).willReturn(tokenResponse);

        Map<String, String> request = Map.of(
                "loginId", "testuser",
                "password", "password123"
        );

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").value("access-token"))
                .andExpect(jsonPath("$.data.userId").value(1));
    }

    @Test
    @DisplayName("POST /api/v1/auth/signup - @Valid 검증 실패 시 400")
    void signup_검증실패_400() throws Exception {
        Map<String, String> request = Map.of(
                "email", "invalid-email"
        );

        mockMvc.perform(post("/api/v1/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/v1/auth/refresh - 토큰 재발급")
    void refresh_성공() throws Exception {
        given(authService.refresh(any())).willReturn(tokenResponse);

        Map<String, String> request = Map.of(
                "refreshToken", "old-refresh-token"
        );

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").value("access-token"));
    }
}
