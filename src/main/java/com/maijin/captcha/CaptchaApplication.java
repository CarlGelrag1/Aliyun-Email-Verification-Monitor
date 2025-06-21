package com.maijin.captcha;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication

public class CaptchaApplication {

    public static void main(String[] args) {
        SpringApplication.run(CaptchaApplication.class, args);

    }



}
