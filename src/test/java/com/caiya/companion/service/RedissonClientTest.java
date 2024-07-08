package com.caiya.companion.service;

import org.junit.jupiter.api.Test;
import org.redisson.api.RList;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

/**
 * @author caiya
 * @description 描述
 * @create 2024-07-08 13:10
 */
@SpringBootTest
public class RedissonClientTest {

    @Resource
    private RedissonClient redissonClient;

    @Test
    void test() {
        // list
        RList<String> list = redissonClient.getList("test-list");
        list.add("caiya");
        System.out.println(list.get(0));
    }

    @Test
    void testWatchDog() {
        RLock lock = redissonClient.getLock("companion.precachejob.docache.lock");
        try {
            /**
             * 只有一个线程可以拿到锁
             * 参数1：最多等待时长
             * 参数2：上锁多久后自动解锁
             */
            if (lock.tryLock(0, -1, TimeUnit.MILLISECONDS)) {
                Thread.sleep(300000);
                System.out.println("getLock: " + Thread.currentThread().getId());
            }
        } catch (InterruptedException e) {
            System.out.println(e.getMessage());
        } finally {
            // 无论程序是否出错，都要释放锁，且只能释放自己的锁
            if (lock.isHeldByCurrentThread()) {
                System.out.println("unlock: " + Thread.currentThread().getId());
                lock.unlock();
            }
        }
    }
}
