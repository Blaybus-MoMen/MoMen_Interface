package com.blaybus.infrastructure.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * JPA Auditing 설정
 */
@Configuration
@EnableJpaAuditing
public class JpaAuditingConfig {
}
