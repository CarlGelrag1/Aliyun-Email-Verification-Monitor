package com.maijin.captcha.utils.dingtalk.enums;

import lombok.Getter;

@Getter
public enum DingTalkMsgEnum {
    LINK("link"),
    TEXT("text"),
    MARKDOWN("markdown");

    private final String type;

    DingTalkMsgEnum(String type) {
        this.type = type;
    }

}
