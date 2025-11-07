package com.example.common_libraries.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;

@Configuration
@ConditionalOnProperty(prefix = "spring.data.redis", name = "host")
@EnableRedisRepositories(basePackages = "com.example.common_libraries")
public class RedisRepositoryConfig {
}