package com.caiya.companion.service;

import com.caiya.companion.model.domain.User;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import javax.annotation.Resource;

/**
 * @author caiya
 * @description 描述
 * @create 2024-07-07 19:35
 */
@SpringBootTest
public class RedisOperationTest {

    @Resource
    private RedisTemplate redisTemplate;

    @Test
    void test() {
        ValueOperations ops = redisTemplate.opsForValue();

        // 增
        ops.set("caiyaString", "cai");
        ops.set("caiyaInt", 1);
        ops.set("caiyaDouble", 0.21);
        User user = new User();
        user.setUsername("hhhh");
        user.setUserAccount("admin");
        user.setPassword("12345678");
        ops.set("caiyaUser", user);

        // 查
        Object caiyaString = ops.get("caiyaString");
        Assertions.assertEquals("cai", caiyaString);

        caiyaString = ops.get("caiyaInt");
        Assertions.assertEquals(1, caiyaString);

        caiyaString = ops.get("caiyaDouble");
        Assertions.assertEquals(0.21, caiyaString);

        caiyaString = ops.get("caiyaUser");
        System.out.println(caiyaString);
    }
}
