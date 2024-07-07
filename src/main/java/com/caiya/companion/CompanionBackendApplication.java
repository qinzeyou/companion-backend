package com.caiya.companion;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@MapperScan("com.caiya.companion.mapper")
@EnableScheduling // 开启 spring 定时任务 支持
public class CompanionBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(CompanionBackendApplication.class, args);
    }
}
