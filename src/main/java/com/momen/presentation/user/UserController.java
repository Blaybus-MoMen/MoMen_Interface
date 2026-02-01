package com.momen.presentation.user;

import com.momen.application.auth.AuthService;
import com.momen.application.user.dto.UserUpdateRequest;
import com.momen.infrastructure.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final AuthService authService;

    @PatchMapping("/me")
    public ResponseEntity<Void> updateProfile(@AuthenticationPrincipal CustomUserDetails userDetails, @RequestBody UserUpdateRequest request) {
        authService.updateProfile(userDetails.getUserId(), request);
        return ResponseEntity.ok().build();
    }
}
