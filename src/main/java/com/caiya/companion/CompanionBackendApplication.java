package com.caiya.companion;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.caiya.companion.mapper")
public class CompanionBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(CompanionBackendApplication.class, args);
    }
}
