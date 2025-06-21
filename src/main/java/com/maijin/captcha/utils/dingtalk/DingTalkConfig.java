package com.maijin.captcha.utils.dingtalk;

import com.maijin.captcha.utils.dingtalk.client.DingTalkRobotClient;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@ConfigurationProperties(prefix = "dingtalk")
@Configuration
@Data
public class DingTalkConfig {

    private String accessToken;

    public static final String webhook="https://oapi.dingtalk.com/robot/send?access_token=";

    public static final String webhooktest="https://oapi.dingtalk.com/robot/send?access_token=";

    public static final Map<String, String> PHONE_BOOK = Map.of(
            "", "",
            "", "",
            "", ""
    );

    public static final List<String> ohAutoUsernames = Collections.unmodifiableList(
            Arrays.asList("")
    );
    @Lazy
    @Bean
    public DingTalkRobotClient dingTalkRobotClient() throws Exception {
        return new DingTalkRobotClient(accessToken);
    }
}
