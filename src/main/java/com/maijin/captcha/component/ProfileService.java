package com.maijin.captcha.component;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
public class ProfileService {

    @Autowired
    private Environment environment;

    public boolean isTestEnvironment() {
        // 获取当前激活的 profiles
        String[] activeProfiles = environment.getActiveProfiles();
        // 判断是否包含 "test" profile
        return Arrays.asList(activeProfiles).contains("test");
    }
}
