package com.ruoyi.emr;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import com.ruoyi.common.security.annotation.EnableCustomConfig;
import com.ruoyi.common.security.annotation.EnableRyFeignClients;

@EnableCustomConfig
@EnableRyFeignClients
@SpringBootApplication
@MapperScan("com.ruoyi.emr.mapper")
public class RuoYiEmrApplication
{
    public static void main(String[] args)
    {
        SpringApplication.run(RuoYiEmrApplication.class, args);
        System.out.println("RuoYi EMR module started.");
    }
}
