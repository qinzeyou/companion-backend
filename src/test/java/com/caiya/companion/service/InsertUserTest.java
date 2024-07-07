package com.caiya.companion.service;

import com.caiya.companion.mapper.UserMapper;
import com.caiya.companion.model.domain.User;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.StopWatch;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * @author caiya
 * @description 测试插入大量用户
 * @create 2024-07-03 22:52
 */
@SpringBootTest
@RunWith(SpringRunner.class)
class InsertUserTest {

    @Resource
    private UserService userService;

    @Test
    void insertUserTest() {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        final int INSERT_NUM = 100000;
        List<User> userList = new ArrayList<>();
        for (int i = 0; i < INSERT_NUM; i++) {
            User user = new User();
            user.setUsername("假用户");
            user.setUserAccount("caicai");
            user.setPassword("12345678");
            user.setAvatarUrl("https://gd-hbimg.huaban.com/7968f776596196a8061e9ee0ee51c0606d785fc42400b-9aWWPH_fw236");
            user.setGender(0);
            user.setEmail("123@qq.com");
            user.setProfile("喜欢学习吗");
            user.setTags("[]");
            userList.add(user);
        }
        userService.saveBatch(userList, 10000);
        stopWatch.stop();
        System.out.println("insertUserTest time is：" + stopWatch.getLastTaskTimeMillis());
    }

    @Test
    void insertOccurUserTest() {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        int j = 0;
        final int batchSize = 100000;
        // 并发任务数组
        List<CompletableFuture<Void>> futureList = new ArrayList<>();
        // 获取 并发任务
        for (int i = 0; i < 4; i++) {
            List<User> userList = new ArrayList<>();
            do {
                j++;
                User user = new User();
                user.setUsername("假用户");
                user.setUserAccount("caicai");
                user.setPassword("12345678");
                user.setAvatarUrl("https://gd-hbimg.huaban.com/7968f776596196a8061e9ee0ee51c0606d785fc42400b-9aWWPH_fw236");
                user.setGender(0);
                user.setEmail("123@qq.com");
                user.setProfile("喜欢学习吗");
                user.setTags("[]");
                userList.add(user);
            } while (j % batchSize != 0);
            // 创建并发任务，PS：并发要求 执行语句先后顺序无所谓 且 不要用到非并发类的集合
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                // 输出当前线程的名称
                System.out.println("threadName：" + Thread.currentThread().getName());
                userService.saveBatch(userList, 50000);
            });
            // 存储当前并发任务
            futureList.add(future);
        }
        // 执行并发任务，join：将并发执行完成才继续执行程序
        CompletableFuture.allOf(futureList.toArray(new CompletableFuture[]{})).join();

        stopWatch.stop();
        System.out.println("insertOccurUserTest time is：" + stopWatch.getLastTaskTimeMillis());
    }
}
