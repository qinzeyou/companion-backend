package com.caiya.companion.job;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.caiya.companion.model.domain.User;
import com.caiya.companion.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author 缓存预热任务
 * @description 描述
 * @create 2024-07-07 21:46
 */
@Component
@Slf4j
public class PreCacheJob {

    @Resource
    private UserService userService;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    // 预热缓存对象用户，因为用户量大，不可能为所有用户都预热，所以选择重点用户进行预热
    List<Long> mianUserLIst = Arrays.asList(4L);

    // 每天执行，预热推荐用户 cron表达式：秒 分 时 天 月 星期
    @Scheduled(cron = "0 59 21 * * *")
    public void doCacheRecommendUsers() {
        String redisKey = String.format("companion:user:recommend:%s", mianUserLIst.get(0));
        ValueOperations<String, Object> opsForValue = redisTemplate.opsForValue();

        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        Page<User> page = new Page<>(1, 10);
        Page<User> userPage = userService.page(page, queryWrapper);
        // 存入redis
        try {
            opsForValue.set(redisKey, userPage, 30000, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            log.error("set redis key error：", e);
        }
    }
}
