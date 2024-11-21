package com.y.datingsite;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@MapperScan("com.y.datingsite.mapper") //扫描包
@SpringBootApplication
@EnableScheduling
public class DatingSiteApplication {

    public static void main(String[] args) {
        SpringApplication.run(DatingSiteApplication.class, args);
    }

}
