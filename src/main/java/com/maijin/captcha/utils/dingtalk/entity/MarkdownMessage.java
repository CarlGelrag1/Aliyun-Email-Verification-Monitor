package com.maijin.captcha.utils.dingtalk.entity;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;

import com.aliyun.credentials.utils.StringUtils;
import com.maijin.captcha.utils.dingtalk.enums.DingTalkMsgEnum;
import lombok.Data;


@Data
public class MarkdownMessage extends BaseMessage {
    /**
     * 消息简介
     */
    private String text;

    /**
     * 消息标题
     */
    private String title;

    /**
     * 可以通过群成员的绑定手机号来艾特具体的群成员
     */
    private String[] atMobiles;

    /**
     * 是否艾特所有人
     * 也可以设置isAtAll=true来艾特所有人
     */
    private boolean isAtAll;

    public MarkdownMessage() {
    }

    public MarkdownMessage(String title, String text) {
        this.text = text;
        this.title = title;
    }

    public MarkdownMessage(String title, String text, String[] atMobiles) {
        this.text = text;
        this.title = title;
        this.atMobiles = atMobiles;
    }

    public MarkdownMessage(String title, String text, boolean isAtAll) {
        this.text = text;
        this.title = title;
        this.isAtAll = isAtAll;
    }

    @Override
    protected void initMsg() {
        this.msgType = DingTalkMsgEnum.MARKDOWN.getType();
    }

    @Override
    public JSONObject toMessageMap() {
        if (StringUtils.isEmpty(this.text) || !DingTalkMsgEnum.MARKDOWN.getType().equals(this.msgType)) {
            throw new IllegalArgumentException("please check the necessary parameters!");
        }
        JSONObject resultMap = JSONUtil.createObj();
        resultMap.put("msgtype", this.msgType);

        JSONObject markdownItems = JSONUtil.createObj();
        markdownItems.put("title", this.title);
        markdownItems.put("text", this.text);
        resultMap.put("markdown", markdownItems);

        JSONObject atItems = JSONUtil.createObj();
        atItems.put("atMobiles", this.atMobiles);
        atItems.put("isAtAll", this.isAtAll);
        resultMap.put("at", atItems);
        return resultMap;
    }
}
