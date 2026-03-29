package com.icss;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.icss.mapper")
public class IcssApplication {
    public static void main(String[] args) {
        SpringApplication.run(IcssApplication.class, args);
    }
}
