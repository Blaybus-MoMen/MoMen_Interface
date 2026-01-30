package com.momen.application.auth;

import com.momen.core.error.enums.ErrorCode;
import com.momen.core.exception.BusinessException;
import com.momen.domain.user.UserRepository;
import com.momen.infrastructure.redis.EmailVerificationRedisService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EmailVerificationService {

    private final UserRepository userRepository;
    private final JavaMailSender mailSender;
    private final EmailVerificationRedisService redisService;
    private final TemplateEngine templateEngine;
    private static final SecureRandom RANDOM = new SecureRandom();

    @Value("${spring.mail.from:support@hyperwise.co.kr}")
    private String mailFrom;

    // 이메일 인증 코드 발송
    @Transactional
    public void sendVerificationCode(String email) {
        // 이미 가입된 이메일인지 확인 (회원가입 전 인증이므로 가입된 이메일은 불가)
        if (userRepository.existsByEmail(email)) {
            throw new BusinessException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }

        // 1분 이내 재발송 방지
        if (redisService.isRecentlySent(email)) {
            throw new BusinessException(ErrorCode.VERIFICATION_CODE_ALREADY_SENT);
        }

        // 6자리 인증 코드 생성
        String code = String.format("%06d", RANDOM.nextInt(1000000));

        // Redis에 인증번호 저장 (TTL: 10분)
        redisService.saveVerificationCode(email, code);

        // 이메일 발송
        try {
            // Thymeleaf 컨텍스트 설정
            Context context = new Context();
            context.setVariable("verificationCode", code);
            context.setVariable("verifyUrl", "https://sandwich.hyperwise.co.kr");

            // 만료 시간 계산 (현재 시간 + 10분)
            LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(10);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            context.setVariable("expiresAt", expiresAt.format(formatter));

            // HTML 템플릿 렌더링
            String htmlContent = templateEngine.process("email-verification", context);

            // MimeMessage 생성
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            helper.setFrom(mailFrom);
            helper.setTo(email);
            helper.setSubject("[SandWitch] 이메일 인증 코드");
            helper.setText(htmlContent, true); // true = HTML 형식

            // 이메일 발송
            mailSender.send(mimeMessage);

            log.info("이메일 인증 코드 발송 완료: {}", email);
        } catch (MessagingException e) {
            log.error("이메일 발송 실패: {}", email, e);
            throw new BusinessException(ErrorCode.EMAIL_SEND_FAILED);
        }
    }

    // 이메일 인증 코드 확인
    @Transactional
    public void verifyCode(String email, String code) {
        // Redis에서 인증번호 확인
        if (!redisService.existsVerificationCode(email)) {
            throw new BusinessException(ErrorCode.VERIFICATION_CODE_NOT_FOUND);
        }

        // 시도 횟수 초과 확인
        if (redisService.isAttemptsExceeded(email)) {
            throw new BusinessException(ErrorCode.VERIFICATION_ATTEMPTS_EXCEEDED);
        }

        // 인증번호 검증
        if (!redisService.verifyCode(email, code)) {
            // 시도 횟수 증가
            redisService.incrementAttempt(email);
            throw new BusinessException(ErrorCode.VERIFICATION_CODE_INVALID);
        }

        // 인증 성공 - 인증번호 및 시도 횟수 삭제, 인증 완료 플래그 설정
        redisService.deleteVerificationCode(email);
        redisService.resetAttempt(email);
        redisService.setVerified(email);

        // 사용자가 이미 존재하는 경우 (회원가입 후 인증하는 경우) 이메일 인증 상태 업데이트
        userRepository.findByEmail(email).ifPresent(user -> {
            user.verifyEmail();
            userRepository.save(user);
        });
    }

    // 인증 코드 재발송
    @Transactional
    public void resendVerificationCode(String email) {
        // 1분 이내 재발송 방지 (sendVerificationCode에서 처리)
        sendVerificationCode(email);
    }
}
