package com.momen.application.oauth;

import com.momen.application.oauth.dto.OAuthConnectionsResponse;
import com.momen.core.error.enums.ErrorCode;
import com.momen.core.exception.BusinessException;
import com.momen.core.mapper.OAuthConnectionMapper;
import com.momen.domain.user.User;
import com.momen.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 소셜 연동 상태 조회 서비스
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OAuthConnectionService {

    private final UserRepository userRepository;
    private final OAuthConnectionMapper oauthConnectionMapper;

    /**
     * 현재 사용자의 소셜 연동 상태 조회
     */
    public OAuthConnectionsResponse getConnections(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        return oauthConnectionMapper.toResponse(user);
    }
}
