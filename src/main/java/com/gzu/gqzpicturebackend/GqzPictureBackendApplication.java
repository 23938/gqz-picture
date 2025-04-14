package com.gzu.gqzpicturebackend;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication
@MapperScan("com.gzu.gqzpicturebackend.mapper")
@EnableAspectJAutoProxy
public class GqzPictureBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(GqzPictureBackendApplication.class, args);
    }

}
