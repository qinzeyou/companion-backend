package com.caiya.companion.config;

import io.lettuce.core.RedisClient;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author caiya
 * @description Redisson 配置类
 * @create 2024-07-08 13:03
 */
@Configuration
@ConfigurationProperties(prefix = "spring.redis")
@Slf4j
@Data
public class RedissonConfig {

    private String host;
    private String port;

    @Bean
    public RedissonClient redisClient() {
        // 1. 创建配置
        Config config = new Config();
        String redisAddress = String.format("redis://%s:%s", host, port);
        config.useSingleServer().setAddress(redisAddress).setDatabase(3);

        // 2. 创建Redisson实例
        RedissonClient redisson = Redisson.create(config);

        return redisson;
    }
}