package com.ruoyi.imaging;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import com.ruoyi.common.security.annotation.EnableCustomConfig;
import com.ruoyi.common.security.annotation.EnableRyFeignClients;

@EnableCustomConfig
@EnableRyFeignClients
@SpringBootApplication
@MapperScan("com.ruoyi.imaging.mapper")
public class RuoYiImagingApplication
{
    public static void main(String[] args)
    {
        SpringApplication.run(RuoYiImagingApplication.class, args);
        System.out.println("RuoYi Imaging module started.");
    }
}
