package com.momen.core.mapper;

import com.momen.application.user.dto.UserResponse;
import com.momen.domain.user.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * User Entity → UserResponse DTO 변환 (공통 Mapper)
 */
@Mapper(config = com.momen.core.config.GlobalMapperConfig.class)
public interface UserMapper extends EntityToResponseMapper<User, UserResponse> {

    @Override
    @Mapping(source = "createDt", target = "createdAt")
    @Mapping(target = "oauthProvider", expression = "java(user.getOauthProvider() != null ? user.getOauthProvider().getProviderName() : null)")
    @Mapping(target = "oauthConnected", expression = "java(user.isOAuthUser())")
    UserResponse toResponse(User user);
}
