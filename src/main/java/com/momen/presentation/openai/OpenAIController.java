package com.momen.presentation.openai;

import com.momen.application.openai.DalleGenerationService;
import com.momen.application.openai.OpenAIChatService;
import com.momen.application.openai.dto.*;
import com.momen.core.controller.BaseController;
import com.momen.core.dto.response.ApiResponse;
import com.momen.domain.openai.DalleGenerationLog;
import com.momen.domain.openai.OpenAIChatLog;
import com.momen.infrastructure.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;

import java.util.List;

/**
 * OpenAI API 컨트롤러
 * ChatGPT 및 DALL-E 이미지 생성 기능 제공
 */
@Slf4j
@Tag(name = "OpenAI", description = "OpenAI ChatGPT 및 DALL-E API")
@RestController
@RequestMapping("/api/v1/openai")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class OpenAIController extends BaseController {

    private final OpenAIChatService chatService;
    private final DalleGenerationService dalleService;

    // ============================================
    // ChatGPT 엔드포인트
    // ============================================

    // 간단한 ChatGPT 테스트 (인증 불필요, DB 저장 없음)
    @Operation(summary = "ChatGPT 테스트", description = "간단한 프롬프트로 ChatGPT를 테스트합니다. 인증이 필요 없으며 DB에 저장되지 않습니다.")
    @PostMapping("/test/chat")
    @ResponseStatus(HttpStatus.OK)
    public Mono<ApiResponse<SimpleChatResponse>> testChat(@Valid @RequestBody SimpleChatRequest request) {
        return chatService.sendSimpleChatRequest(request)
                .map(response -> ApiResponse.success(response,
                    response.isSuccess() ? "ChatGPT 테스트 완료" : "ChatGPT 테스트 실패"));
    }

    // ChatGPT 대화 요청
    @Operation(summary = "ChatGPT 대화 요청", description = "ChatGPT API를 통해 대화를 생성합니다. 응답과 함께 작업 ID가 반환됩니다.")
    @PostMapping("/chat")
    @ResponseStatus(HttpStatus.OK)
    public Mono<ApiResponse<ChatGPTResponse>> chat(@Valid @RequestBody ChatGPTRequest request, @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails) {
        // 인증된 사용자가 있으면 userId 설정
        if (userDetails != null) {
            request.setUserId(userDetails.getUserId());
        }

        SecurityContext securityContext = SecurityContextHolder.getContext();

        return chatService.sendChatRequest(request)
                .map(response -> ApiResponse.success(response, "ChatGPT 응답이 생성되었습니다"))
                .contextWrite(Context.of(SecurityContext.class, securityContext));
    }

    // ChatGPT 로그 조회 (Job ID)
    @Operation(summary = "ChatGPT 로그 조회", description = "작업 ID로 ChatGPT 대화 로그를 조회합니다.")
    @GetMapping("/chat/{jobId}")
    public ApiResponse<OpenAIChatLog> getChatLog(@Parameter(description = "작업 ID (UUID)") @PathVariable String jobId) {
        OpenAIChatLog chatLog = chatService.getChatLog(jobId);
        return ApiResponse.success(chatLog, "ChatGPT 로그 조회 완료");
    }

    // 사용자별 ChatGPT 로그 목록 조회
    @Operation(summary = "사용자별 ChatGPT 로그 목록", description = "특정 사용자의 모든 ChatGPT 대화 로그를 조회합니다.")
    @GetMapping("/chat/user/{userId}")
    public ApiResponse<List<OpenAIChatLog>> getChatLogsByUser(@Parameter(description = "사용자 ID") @PathVariable Long userId) {
        List<OpenAIChatLog> logs = chatService.getChatLogsByUser(userId);
        return ApiResponse.success(logs, "사용자별 ChatGPT 로그 조회 완료");
    }

    // ============================================
    // DALL-E 엔드포인트
    // ============================================

    // DALL-E 이미지 생성 요청
    @Operation(summary = "DALL-E 이미지 생성", description = "DALL-E API를 통해 이미지를 생성합니다. 생성된 이미지 URL과 작업 ID가 반환됩니다.")
    @PostMapping("/dalle/generate")
    @ResponseStatus(HttpStatus.OK)
    public Mono<ApiResponse<DalleGenerateResponse>> generateImage(@Valid @RequestBody DalleGenerateRequest request,
                                                                  @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        // 인증된 사용자가 있으면 userId 설정
        if (userDetails != null) {
            request.setUserId(userDetails.getUserId());
        }

        SecurityContext securityContext = SecurityContextHolder.getContext();

        return dalleService.generateImage(request)
                .map(response -> ApiResponse.success(response, "이미지가 생성되었습니다"))
                .contextWrite(Context.of(SecurityContext.class, securityContext));
    }

    // DALL-E 로그 조회 (Job ID)
    @Operation(summary = "DALL-E 로그 조회", description = "작업 ID로 DALL-E 이미지 생성 로그를 조회합니다.")
    @GetMapping("/dalle/{jobId}")
    public ApiResponse<DalleGenerationLog> getDalleLog(@Parameter(description = "작업 ID (UUID)") @PathVariable String jobId) {
        DalleGenerationLog log = dalleService.getGenerationLog(jobId);
        return ApiResponse.success(log, "DALL-E 로그 조회 완료");
    }

    // 사용자별 DALL-E 로그 목록 조회
    @Operation(summary = "사용자별 DALL-E 로그 목록", description = "특정 사용자의 모든 DALL-E 이미지 생성 로그를 조회합니다.")
    @GetMapping("/dalle/user/{userId}")
    public ApiResponse<List<DalleGenerationLog>> getDalleLogsByUser(@Parameter(description = "사용자 ID") @PathVariable Long userId) {
        List<DalleGenerationLog> logs = dalleService.getGenerationLogsByUser(userId);
        return ApiResponse.success(logs, "사용자별 DALL-E 로그 조회 완료");
    }
}
