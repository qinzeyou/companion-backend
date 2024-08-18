package com.caiya.companion.job;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.caiya.companion.common.PageResponse;
import com.caiya.companion.constant.SystemConstant;
import com.caiya.companion.model.domain.User;
import com.caiya.companion.model.vo.UserVO;
import com.caiya.companion.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author caiya
 * @description 缓存预热任务
 * @create 2024-07-07 21:46
 */
@Component
@Slf4j
public class PreCacheJob {

    @Resource
    private UserService userService;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Resource
    private RedissonClient redissonClient;

    // 预热缓存对象用户，因为用户量大，不可能为所有用户都预热，所以选择重点用户进行预热
    List<Long> mianUserLIst = Collections.singletonList(4L);

    // 每天执行，预热推荐用户 cron表达式：秒 分 时 天 月 星期
    @Scheduled(cron = "0 40 0 * * *")
    public void doCacheRecommendUsers() {
        RLock lock = redissonClient.getLock("companion.precachejob.docache.lock");
        try {
            /**
             * 只有一个线程可以拿到锁
             * 参数1：最多等待时长
             * 参数2：上锁多久后自动解锁，-1是启动看门狗机制，如果没有手动释放锁，则redisson续期，直到任务手动释放锁
             */
            if (lock.tryLock(0, -1, TimeUnit.MILLISECONDS)) {
                log.info("getLock: " + Thread.currentThread().getId());
                // 遍历用户id，为每个用户预热推荐用户列表
                for (Long userId : mianUserLIst) {
                    // redis存储key
                    String prefixRedisKey = SystemConstant.PREFIX_REDIS_KEY + "recommend:%s";
                    String redisKey = String.format(prefixRedisKey, userId);
                    // redis操作对象
                    ValueOperations<String, Object> opsForValue = redisTemplate.opsForValue();

                    // 分页查询用户
                    QueryWrapper<User> queryWrapper = new QueryWrapper<>();
                    Page<User> page = new Page<>(1, 10);
                    Page<User> userPage = userService.page(page, queryWrapper);
                    // 处理分页，用户信息脱敏，查询用户自身的标签，然后存储到自定义分页结果集中
                    PageResponse<List<UserVO>> pageResponse = userService.getListPageResponse(userPage);
                    // 写缓存
                    opsForValue.set(redisKey, pageResponse, 1, TimeUnit.DAYS);
                }

            }
        } catch (InterruptedException e) {
            log.error("doCacheRecommendUsers error：", e);
        } finally {
            // 无论程序是否出错，都要释放锁，且只能释放自己的锁
            if (lock.isHeldByCurrentThread()) {
                log.info("unlock: " + Thread.currentThread().getId());
                lock.unlock();
            }
        }
    }

    /**
     * 预热匹配用户列表
     */
    @Scheduled(cron = "4 40 0 * * *")
    public void doCacheMatchUsers() {
        RLock lock = redissonClient.getLock("companion.precachejob.docache.lock");
        try {
            /**
             * 只有一个线程可以拿到锁
             * 参数1：最多等待时长
             * 参数2：上锁多久后自动解锁，-1是启动看门狗机制，如果没有手动释放锁，则redisson续期，直到任务手动释放锁
             */
            if (lock.tryLock(0, -1, TimeUnit.MILLISECONDS)) {
                log.info("getLock: " + Thread.currentThread().getId());
                // 获取所有用户的id
                List<User> userList = userService.list();
                // 遍历用户id，为每个用户预热推荐用户列表
                for (User user : userList) {
                    // redis存储key
                    String prefixRedisKey = SystemConstant.PREFIX_REDIS_KEY + "match:%s";
                    String redisKey = String.format(prefixRedisKey, user.getId());
                    // redis操作对象
                    ValueOperations<String, Object> opsForValue = redisTemplate.opsForValue();

                    // 获取存储数据：算法匹配该用户的所有相似用户（按相似度从高到低）
                    List<UserVO> userVOList = userService.matchUsers(50, user);
                    // 写缓存
                    opsForValue.set(redisKey, userVOList, 1, TimeUnit.DAYS);
                }

            }
        } catch (InterruptedException e) {
            log.error("doCacheRecommendUsers error：", e);
        } finally {
            // 无论程序是否出错，都要释放锁，且只能释放自己的锁
            if (lock.isHeldByCurrentThread()) {
                log.info("unlock: " + Thread.currentThread().getId());
                lock.unlock();
            }
        }
    }
}
