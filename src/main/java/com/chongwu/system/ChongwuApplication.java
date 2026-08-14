package com.chongwu.system;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 宠物寄养服务管理系统启动类
 */
@SpringBootApplication
@MapperScan("com.chongwu.system.mapper")
public class ChongwuApplication {
//ss66
    public static void main(String[] args) {
        SpringApplication.run(ChongwuApplication.class, args);
    }
}