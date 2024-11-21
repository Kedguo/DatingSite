package com.y.datingsite.service;

import com.y.datingsite.model.domain.User;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import javax.annotation.Resource;

@SpringBootTest
public class RedisTest {
    @Resource
    private RedisTemplate redisTemplate;

    @Test
    void test() {
        ValueOperations valueOperations = redisTemplate.opsForValue();
        // 插
        valueOperations.set("yString","dog");
        valueOperations.set("yInt", 1);
        valueOperations.set("yDouble", 2.0);
        User user = new User();
        user.setId(1L);
        user.setUserName("y");
        valueOperations.set("yUser", user);
        // 查
        Object yu = valueOperations.get("yString");
        Assertions.assertTrue("dog".equals((String)yu));
        yu = valueOperations.get("yInt");
        Assertions.assertTrue(1 == (Integer) yu);
        yu=valueOperations.get("yDouble");
        Assertions.assertTrue(2.0==(Double)yu);
        System.out.println(valueOperations.get("yUser"));
        // 删
        /*redisTemplate.delete("yString");
        redisTemplate.delete("yInt");
        redisTemplate.delete("yDouble");
        redisTemplate.delete("yUser");*/
    }
}