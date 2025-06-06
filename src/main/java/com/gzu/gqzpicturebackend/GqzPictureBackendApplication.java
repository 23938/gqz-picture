package com.gzu.gqzpicturebackend;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@MapperScan("com.gzu.gqzpicturebackend.mapper")
@EnableAspectJAutoProxy
@EnableAsync
public class GqzPictureBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(GqzPictureBackendApplication.class, args);
    }

}
