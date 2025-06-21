package com.maijin.captcha.utils.dingtalk.entity;



import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;


import com.aliyun.credentials.utils.StringUtils;
import com.maijin.captcha.utils.dingtalk.enums.DingTalkMsgEnum;
import lombok.Data;

@Data
public class TextMessage extends BaseMessage {

    /**
     * 文本消息的具体内容
     */
    private String content;

    /**
     * 可以通过群成员的绑定手机号来艾特具体的群成员
     */
    private String[] atMobiles;

    /**
     * 是否艾特所有人
     * 也可以设置isAtAll=true来艾特所有人
     */
    private boolean isAtAll;

    public TextMessage() {
    }

    public TextMessage(String content) {
        this.content = content;
    }

    public TextMessage(String content, String[] atMobiles) {
        this.content = content;
        this.atMobiles = atMobiles;
    }

    public TextMessage(String content, boolean isAtAll) {
        this.content = content;
        this.isAtAll = isAtAll;
    }


    @Override
    protected void initMsg() {
        this.msgType = DingTalkMsgEnum.TEXT.getType();
    }

    @Override
    public JSONObject toMessageMap() {
        if (StringUtils.isEmpty(this.content) || !DingTalkMsgEnum.TEXT.getType().equals(this.msgType)) {
            throw new IllegalArgumentException("please check the necessary parameters!");
        }

        JSONObject resultMap = JSONUtil.createObj();
        resultMap.put("msgtype", this.msgType);

        JSONObject textItems = JSONUtil.createObj();
        textItems.put("content", this.content);
        resultMap.put("text", textItems);

        JSONObject atItems = JSONUtil.createObj();
        atItems.put("atMobiles", this.atMobiles);
        atItems.put("isAtAll", this.isAtAll);
        resultMap.put("at", atItems);

        return resultMap;
    }
}
