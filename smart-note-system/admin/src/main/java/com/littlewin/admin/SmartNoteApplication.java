package com.littlewin.admin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.client.RestTemplate;
import org.springframework.context.annotation.ComponentScan;
import org.mybatis.spring.annotation.MapperScan;

@ComponentScan(basePackages = "com.littlewin")
@MapperScan("com.littlewin.**.mapper")
@EnableAsync
@SpringBootApplication
public class SmartNoteApplication {
    public static void main(String[] args) {
        SpringApplication.run(SmartNoteApplication.class, args);
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}