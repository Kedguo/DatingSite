package com.y.datingsite.service;

import com.y.datingsite.model.domain.User;
import org.junit.Assert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;

/**
 * @author : Yuan
 * @date :2024/4/17
 */
@SpringBootTest
class UserServiceTest {
    @Resource
    private UserService userService;

    @Test
    public void testAddUser(){
        User user = new User();

        user.setUserName("阿黄");
        user.setUserAccount("do");
        user.setAvatarUrl("123");
        user.setGender(0);
        user.setUserPassword("123456789");
        user.setPhone("123456789");
        user.setEmail("1");
        user.setUserStatus(0);


        boolean result = userService.save(user);
        System.out.println(user.getId());
        Assertions.assertTrue(result);
    }

    //测试用户注册
    @Test
    void userRegister() {

        //测试密码为空
        String userAccount = "ahuang";
        String userPassword = "123456789";
        String checkPassword = "123456789";
        String planetCode = "5";
        long result = userService.userRegister(userAccount, userPassword, checkPassword,planetCode);


        result = userService.userRegister(userAccount, userPassword, checkPassword,planetCode);
        Assertions.assertTrue(result > 0);

    }


    @Test
    void searchUserByTags() {
        List<String> tag = Arrays.asList("男");
        List<User> userlist = userService.SearchUserByTags(tag);
        Assert.assertNotNull(userlist);
    }
}