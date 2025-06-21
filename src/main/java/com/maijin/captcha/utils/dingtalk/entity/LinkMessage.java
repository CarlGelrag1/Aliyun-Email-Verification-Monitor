package com.maijin.captcha.utils.dingtalk.entity;


import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.aliyun.credentials.utils.StringUtils;
import com.maijin.captcha.utils.dingtalk.enums.DingTalkMsgEnum;
import lombok.Data;

@Data
public class LinkMessage extends BaseMessage {
    /**
     * 消息简介
     */
    private String text;

    /**
     * 消息标题
     */
    private String title;

    /**
     * 封面图片URL
     */
    private String picUrl;

    /**
     * 消息跳转URL
     */
    private String messageUrl;

    public LinkMessage() {
    }

    public LinkMessage(String title, String text, String messageUrl) {
        this.text = text;
        this.title = title;
        this.messageUrl = messageUrl;
    }

    public LinkMessage(String title, String text, String messageUrl, String picUrl) {
        this.text = text;
        this.title = title;
        this.picUrl = picUrl;
        this.messageUrl = messageUrl;
    }

    @Override
    protected void initMsg() {
        this.msgType = DingTalkMsgEnum.LINK.getType();
    }

    @Override
    public JSONObject toMessageMap() {
        if (StringUtils.isEmpty(this.text) || !DingTalkMsgEnum.LINK.getType().equals(this.msgType)) {
            throw new IllegalArgumentException("please check the necessary parameters!");
        }

        JSONObject resultMap = JSONUtil.createObj();
        resultMap.put("msgtype", this.msgType);
        JSONObject linkItems = JSONUtil.createObj();
        linkItems.put("title", this.title);
        linkItems.put("text", this.text);
        linkItems.put("picUrl", this.picUrl);
        linkItems.put("messageUrl", this.messageUrl);
        resultMap.put("link", linkItems);

        return resultMap;
    }
}
