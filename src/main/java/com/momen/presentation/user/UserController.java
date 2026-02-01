package com.momen.presentation.user;

import com.momen.application.auth.AuthService;
import com.momen.application.user.dto.UserUpdateRequest;
import com.momen.infrastructure.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "User", description = "사용자 관리 API")
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final AuthService authService;

    @Operation(summary = "내 정보 수정", description = "비밀번호, 이름 등 내 프로필 정보를 수정합니다")
    @PatchMapping("/me")
    public ResponseEntity<Void> updateProfile(@AuthenticationPrincipal CustomUserDetails userDetails, @RequestBody UserUpdateRequest request) {
        authService.updateProfile(userDetails.getUserId(), request);
        return ResponseEntity.ok().build();
    }
}
