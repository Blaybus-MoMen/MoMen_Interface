package com.momen.core.mapper;

import com.momen.application.oauth.dto.OAuthConnectionsResponse;
import com.momen.domain.user.OAuthProvider;
import com.momen.domain.user.User;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * User Entity → OAuthConnectionsResponse 변환 (공통 Mapper)
 * 소셜 연동 상태는 제공자별 루프 로직이 있어 수동 구현
 */
@Component
public class OAuthConnectionMapper implements EntityToResponseMapper<User, OAuthConnectionsResponse> {

    @Override
    public OAuthConnectionsResponse toResponse(User user) {
        Map<String, OAuthConnectionsResponse.ProviderConnection> connections = new HashMap<>();

        for (OAuthProvider provider : OAuthProvider.values()) {
            String key = provider.getProviderName();
            boolean connected = user.getOauthProvider() == provider;
            OAuthConnectionsResponse.ProviderConnection pc = OAuthConnectionsResponse.ProviderConnection.builder()
                    .connected(connected)
                    .connectedAt(connected ? user.getOauthConnectedAt() : null)
                    .build();
            connections.put(key, pc);
        }

        return OAuthConnectionsResponse.builder()
                .connections(connections)
                .build();
    }
}
