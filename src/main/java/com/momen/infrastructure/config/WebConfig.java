package com.momen.infrastructure.config;

import org.apache.catalina.connector.Connector;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.security.task.DelegatingSecurityContextAsyncTaskExecutor;
import org.springframework.web.servlet.config.annotation.AsyncSupportConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 설정
 * CORS 설정 및 비동기 SecurityContext 전파 포함
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    // CORS 설정은 SecurityConfig.corsConfigurationSource()에서 관리

    /**
     * 비동기 요청 처리 설정
     * SecurityContext를 비동기 스레드로 전파하기 위한 TaskExecutor 설정
     */
    @Override
    public void configureAsyncSupport(AsyncSupportConfigurer configurer) {
        configurer.setTaskExecutor(asyncTaskExecutor());
        configurer.setDefaultTimeout(300000); // 5분 타임아웃
    }

    /**
     * SecurityContext를 자동으로 전파하는 AsyncTaskExecutor
     * Reactor Mono/Flux 반환 시 비동기 디스패치에서 SecurityContext 유지
     */
    @Bean
    public AsyncTaskExecutor asyncTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(50);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("async-security-");
        executor.initialize();

        // SecurityContext를 비동기 작업으로 전파
        return new DelegatingSecurityContextAsyncTaskExecutor(executor);
    }

    /**
     * Tomcat Connector 커스터마이징
     * URL 인코딩된 슬래시 허용 (Veo API의 operationName에 슬래시 포함)
     */
    @Bean
    public WebServerFactoryCustomizer<TomcatServletWebServerFactory> tomcatCustomizer() {
        return factory -> factory.addConnectorCustomizers((Connector connector) -> {
            connector.setEncodedSolidusHandling("decode");
        });
    }
}
