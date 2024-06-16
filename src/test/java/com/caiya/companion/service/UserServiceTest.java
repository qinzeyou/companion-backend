package com.caiya.companion.service;

import com.caiya.companion.model.domain.User;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;

/**
 * @author caiya
 * @description 描述
 * @create 2024-05-04 20:01
 */

@SpringBootTest
@RunWith(SpringRunner.class)
class UserServiceTest {

    @Resource
    private UserService userService;

    @Test
    void addUserTest() {
        User user = new User();
        user.setUsername("测试插入用户");
        user.setUserAccount("123");
        user.setPassword("111");
        user.setPhone("19978321604");
        user.setAvatarUrl("11");
        user.setEmail("123");
        boolean result = userService.save(user);
        // 断言，判断测试的结果是否与预期结果一致
        Assertions.assertTrue(result);
    }

    @Test
    void userRegister() {
        String userAccount = "";
        String password = "12345678";
        String checkPassword = "12345678";
        // 非空校验
        long saveResult = userService.userRegister(userAccount, password, checkPassword);
        Assertions.assertEquals(-1, saveResult);
        // 账号长度校验
        userAccount = "qzy";
        saveResult = userService.userRegister(userAccount, password, checkPassword);
        Assertions.assertEquals(-1, saveResult);
        // 密码长度校验
        password = "123";
        saveResult = userService.userRegister(userAccount, password, checkPassword);
        Assertions.assertEquals(-1, saveResult);
        // 特殊字符校验
        userAccount = "q zy";
        saveResult = userService.userRegister(userAccount, password, checkPassword);
        Assertions.assertEquals(-1, saveResult);
        // 用户名重复
        userAccount = "1234";
        saveResult = userService.userRegister(userAccount, password, checkPassword);
        Assertions.assertEquals(-1, saveResult);
        // 正确插入
        userAccount = "qzy11";
        password = "12345678";
        checkPassword = "12345678";
        saveResult = userService.userRegister(userAccount, password, checkPassword);
        Assertions.assertTrue(saveResult > 0);
    }

    @Test
    void testSearchUserByTag() {
        List<String> tagNameList = Arrays.asList("java", "python", "c++");
        List<User> userList = userService.searchUserByTags(tagNameList);
        Assertions.assertNotNull(userList);
    }
}