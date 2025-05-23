package com.prtec.auth.application.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "custom.security.jwt")
public class JwtProperties {
    private String secret;
    private long expirationTimeMinutes;
}