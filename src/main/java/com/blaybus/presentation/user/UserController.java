package com.blaybus.presentation.user;

import com.blaybus.application.user.UserService;
import com.blaybus.application.user.dto.UpdateEmailRequest;
import com.blaybus.application.user.dto.UpdatePasswordRequest;
import com.blaybus.application.user.dto.UpdateProfileRequest;
import com.blaybus.application.user.dto.UserResponse;
import com.blaybus.core.dto.response.ApiResponse;
import com.blaybus.infrastructure.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * 사용자 관리 API Controller
 */
@Tag(name = "User", description = "사용자 관리 API")
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "내 정보 조회", description = "현재 로그인한 사용자의 정보를 조회합니다")
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> getMyInfo(@AuthenticationPrincipal CustomUserDetails userDetails) {

        UserResponse response = userService.getUserInfo(userDetails.getUserId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "회원 정보 수정", description = "이름, 연락처, 학번을 수정합니다")
    @PutMapping("/me/profile")
    public ResponseEntity<ApiResponse<UserResponse>> updateProfile(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                                   @RequestBody UpdateProfileRequest request) {

        UserResponse response = userService.updateProfile(userDetails.getUserId(), request);
        return ResponseEntity.ok(ApiResponse.success(response, "회원 정보가 수정되었습니다"));
    }

    @Operation(summary = "이메일 변경", description = "이메일을 변경합니다 (재인증 필요)")
    @PutMapping("/me/email")
    public ResponseEntity<ApiResponse<UserResponse>> updateEmail(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                                 @Valid @RequestBody UpdateEmailRequest request) {

        UserResponse response = userService.updateEmail(userDetails.getUserId(), request);
        return ResponseEntity.ok(ApiResponse.success(response, "이메일이 변경되었습니다. 재인증이 필요합니다"));
    }

    @Operation(summary = "비밀번호 변경", description = "비밀번호를 변경합니다")
    @PutMapping("/me/password")
    public ResponseEntity<ApiResponse<Void>> updatePassword(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                            @Valid @RequestBody UpdatePasswordRequest request) {

        userService.updatePassword(userDetails.getUserId(), request);
        return ResponseEntity.ok(ApiResponse.success(null, "비밀번호가 변경되었습니다"));
    }
}
