package com.y.datingsite.service;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * @author : Yuan
 * @date :2024/8/11
 */
@SpringBootTest
public class Test {
    private int count = 0;
    @org.junit.jupiter.api.Test
    @Scheduled(cron = "0/10 * * * * ?")
    public void doCacheRecommendUser() {
        System.out.println("执行次数"+count++);
    }


}
