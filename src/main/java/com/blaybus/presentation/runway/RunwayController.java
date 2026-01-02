package com.blaybus.presentation.runway;

import com.blaybus.application.runway.RunwayService;
import com.blaybus.application.runway.dto.RunwayGenerateRequest;
import com.blaybus.application.runway.dto.RunwayGenerateResponse;
import com.blaybus.application.runway.dto.RunwayTaskStatus;
import com.blaybus.core.controller.BaseController;
import com.blaybus.core.dto.response.ApiResponse;
import com.blaybus.infrastructure.security.CustomUserDetails;
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

import java.util.Map;

/**
 * Runway Gen-3 비디오 생성 API 컨트롤러
 *
 * Runway의 Gen-3 Alpha/Turbo 모델을 사용하여 텍스트 프롬프트 기반 비디오 생성 기능 제공
 */
@Slf4j
@Tag(name = "Runway", description = "Runway Gen-3 비디오 생성 API")
@RestController
@RequestMapping("/api/v1/runway")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class RunwayController extends BaseController {

    private final RunwayService runwayService;

    /**
     * 비디오 생성 요청 (비동기)
     * 요청을 접수하고 작업 ID를 즉시 반환합니다.
     * 클라이언트는 반환된 작업 ID로 상태를 폴링해야 합니다.
     */
    @Operation(
            summary = "비디오 생성 요청 (비동기)",
            description = "텍스트 프롬프트를 기반으로 비디오 생성을 요청합니다. " +
                    "작업 ID를 반환하며, /runway/status/{taskId}로 상태를 조회할 수 있습니다."
    )
    @PostMapping("/generate")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public Mono<ApiResponse<RunwayGenerateResponse>> generateVideo(
            @Valid @RequestBody RunwayGenerateRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        // 인증된 사용자가 있으면 userId 설정
        if (userDetails != null) {
            request.setUserId(userDetails.getUserId());
            log.info("Video generation request from user {}: {}", userDetails.getUserId(), request.getPromptText());
        } else {
            log.info("Video generation request (anonymous): {}", request.getPromptText());
        }

        SecurityContext securityContext = SecurityContextHolder.getContext();

        return runwayService.generateVideo(request)
                .map(response -> ApiResponse.success(response, "비디오 생성 요청이 접수되었습니다"))
                .contextWrite(Context.of(SecurityContext.class, securityContext));
    }

    /**
     * 작업 상태 조회
     * 생성 요청 후 받은 작업 ID로 현재 진행 상태를 확인합니다.
     */
    @Operation(
            summary = "작업 상태 조회",
            description = "비디오 생성 작업의 현재 상태를 조회합니다. " +
                    "status가 SUCCEEDED이고 error가 없으면 완료된 것입니다."
    )
    @GetMapping("/status/{taskId}")
    public Mono<ApiResponse<RunwayTaskStatus>> getStatus(
            @Parameter(description = "작업 ID (UUID)")
            @PathVariable String taskId
    ) {
        log.info("Checking status for task: {}", taskId);

        SecurityContext securityContext = SecurityContextHolder.getContext();

        return runwayService.getTaskStatus(taskId)
                .map(status -> ApiResponse.success(status, "작업 상태 조회 완료"))
                .contextWrite(Context.of(SecurityContext.class, securityContext));
    }

    /**
     * 비디오 생성 및 완료까지 대기 (동기)
     * 비디오가 완료될 때까지 대기한 후 결과를 반환합니다.
     * 최대 10분까지 대기하며, 타임아웃 시 에러를 반환합니다.
     */
    @Operation(
            summary = "비디오 생성 및 완료 대기 (동기)",
            description = "비디오 생성을 요청하고 완료될 때까지 대기합니다. " +
                    "최대 10분까지 대기하며, 완료되면 비디오 URL을 포함한 결과를 반환합니다."
    )
    @PostMapping("/generate-sync")
    public Mono<ApiResponse<RunwayTaskStatus>> generateAndWait(
            @Valid @RequestBody RunwayGenerateRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        if (userDetails != null) {
            request.setUserId(userDetails.getUserId());
        }

        SecurityContext securityContext = SecurityContextHolder.getContext();

        return runwayService.generateAndWait(request)
                .map(status -> ApiResponse.success(status, "비디오 생성이 완료되었습니다"))
                .contextWrite(Context.of(SecurityContext.class, securityContext));
    }

    /**
     * 비디오 URL 추출
     */
    @Operation(
            summary = "비디오 URL 추출",
            description = "완료된 작업에서 생성된 비디오의 다운로드 URL을 추출합니다."
    )
    @GetMapping("/video-url/{taskId}")
    public Mono<ApiResponse<String>> getVideoUrl(
            @Parameter(description = "작업 ID")
            @PathVariable String taskId
    ) {
        SecurityContext securityContext = SecurityContextHolder.getContext();

        return runwayService.getTaskStatus(taskId)
                .map(status -> {
                    String videoUrl = runwayService.extractVideoUrl(status);
                    return ApiResponse.success(videoUrl, "비디오 URL 추출 완료");
                })
                .contextWrite(Context.of(SecurityContext.class, securityContext));
    }

    /**
     * 간단한 비디오 생성 (프롬프트만)
     * 기본 설정으로 비디오를 생성 (1280:720, 6초, veo3.1_fast)
     */
    @Operation(
            summary = "간단한 비디오 생성",
            description = "프롬프트만으로 기본 설정(1280:720, 6초, veo3.1_fast)으로 비디오를 생성합니다."
    )
    @PostMapping("/generate-simple")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public Mono<ApiResponse<RunwayGenerateResponse>> generateSimple(
            @Parameter(description = "비디오 설명 프롬프트")
            @RequestParam String prompt,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        RunwayGenerateRequest request = RunwayGenerateRequest.builder()
                .promptText(prompt)
                .userId(userDetails != null ? userDetails.getUserId() : null)
                .build();

        SecurityContext securityContext = SecurityContextHolder.getContext();

        return runwayService.generateVideo(request)
                .map(response -> ApiResponse.success(response, "비디오 생성 요청이 접수되었습니다"))
                .contextWrite(Context.of(SecurityContext.class, securityContext));
    }

    /**
     * 작업 취소
     */
    @Operation(
            summary = "작업 취소",
            description = "진행 중인 비디오 생성 작업을 취소합니다."
    )
    @PostMapping("/cancel/{taskId}")
    public Mono<ApiResponse<Boolean>> cancelTask(
            @Parameter(description = "작업 ID")
            @PathVariable String taskId
    ) {
        log.info("Cancelling task: {}", taskId);

        SecurityContext securityContext = SecurityContextHolder.getContext();

        return runwayService.cancelTask(taskId)
                .map(success -> {
                    if (success) {
                        return ApiResponse.success(true, "작업이 취소되었습니다");
                    } else {
                        return ApiResponse.success(false, "작업 취소에 실패했습니다");
                    }
                })
                .contextWrite(Context.of(SecurityContext.class, securityContext));
    }

    /**
     * API 헬스 체크
     */
    @Operation(
            summary = "API 헬스 체크",
            description = "Runway API와의 연결 상태를 확인합니다."
    )
    @GetMapping("/health")
    public Mono<ApiResponse<Map<String, Object>>> healthCheck() {
        return runwayService.healthCheck()
                .map(isHealthy -> {
                    Map<String, Object> health = Map.of(
                            "status", isHealthy ? "UP" : "DOWN",
                            "service", "Runway Gen-3 API",
                            "timestamp", System.currentTimeMillis()
                    );
                    return ApiResponse.success(health,
                            isHealthy ? "API 연결 정상" : "API 연결 실패");
                });
    }

    /**
     * 사용 가능한 비디오 설정 조회
     */
    @Operation(
            summary = "지원 설정 조회",
            description = "Runway Gen-3이 지원하는 모델, 화면 비율, 길이 등의 설정 옵션을 조회합니다."
    )
    @GetMapping("/options")
    public ApiResponse<Map<String, Object>> getOptions() {
        Map<String, Object> options = Map.of(
                "models", Map.of(
                        "veo3.1_fast", "Veo 3.1 Fast (빠른 속도)",
                        "veo3.1", "Veo 3.1 (높은 품질)",
                        "veo3", "Veo 3 (이전 버전)"
                ),
                "ratios", new String[]{"1280:720", "720:1280", "1080:1920", "1920:1080"},
                "durations", new Integer[]{4, 6, 8},
                "durationUnit", "seconds",
                "features", new String[]{
                        "텍스트 프롬프트 기반 생성",
                        "네이티브 오디오 생성",
                        "고화질 (최대 1920x1080)",
                        "다양한 화면 비율 지원",
                        "작업 취소 기능"
                },
                "pricing", Map.of(
                        "veo3.1_fast", "$0.05 per second",
                        "veo3.1", "$0.10 per second"
                )
        );

        return ApiResponse.success(options, "Runway Gen-3 설정 옵션");
    }
}
