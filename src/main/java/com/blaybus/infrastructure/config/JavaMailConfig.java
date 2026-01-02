package com.blaybus.infrastructure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

@Configuration
public class JavaMailConfig {

    @Value("${spring.mail.host:outbound.daouoffice.com}")
    private String host;

    @Value("${spring.mail.port:25}")
    private int port;

    @Value("${spring.mail.username:support@hyperwise.co.kr}")
    private String username;

    @Value("${spring.mail.password:hw0908!@}")
    private String password;

    @Value("${spring.mail.properties.mail.smtp.auth:true}")
    private String auth;

    @Value("${spring.mail.properties.mail.smtp.starttls.enable:false}")
    private String starttlsEnable;

    @Value("${spring.mail.properties.mail.smtp.ssl.enable:false}")
    private String sslEnable;

    @Bean
    public JavaMailSender getJavaMailSender() {
        JavaMailSenderImpl ms = new JavaMailSenderImpl();
        ms.setHost(host);
        ms.setPort(port);
        ms.setUsername(username);
        ms.setPassword(password);

        Properties props = ms.getJavaMailProperties();
        props.put("mail.smtp.auth", auth);
        props.put("mail.smtp.starttls.enable", starttlsEnable);
        props.put("mail.smtp.ssl.enable", sslEnable);
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.port", String.valueOf(port));

        return ms;
    }
}
