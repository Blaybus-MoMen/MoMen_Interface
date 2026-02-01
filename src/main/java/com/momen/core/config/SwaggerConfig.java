package com.momen.core.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class SwaggerConfig {

    private static final String BEARER_SCHEME = "bearerAuth";

    @Bean
    public OpenAPI customOpenAPI() {
        Components components = new Components()
                .addSecuritySchemes(BEARER_SCHEME, new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .in(SecurityScheme.In.HEADER)
                        .name("Authorization")
                        .description("JWT 토큰을 입력하세요. Bearer 접두사 없이 토큰만 입력하세요."));

        Info info = new Info()
                .title("Momen API")
                .description("Momen 학습 플랫폼 REST API 문서\n\n"
                        + "- 인증: JWT 기반 (Bearer Token)\n"
                        + "- 회원가입/로그인 후 발급받은 Access Token을 Authorize에 입력하세요")
                .version("1.0.0");

        SecurityRequirement securityRequirement = new SecurityRequirement()
                .addList(BEARER_SCHEME);

        return new OpenAPI()
                .info(info)
                .components(components)
                .addSecurityItem(securityRequirement);
    }
}
