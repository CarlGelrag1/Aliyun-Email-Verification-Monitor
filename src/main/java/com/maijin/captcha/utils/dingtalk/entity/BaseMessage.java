package com.maijin.captcha.utils.dingtalk.entity;


import cn.hutool.json.JSONObject;

public abstract class BaseMessage {

    public BaseMessage() {
        initMsg();
    }

    protected String msgType;

    public String getMsgType() {
        return msgType;
    }

    protected abstract void initMsg();


    /**
     * 返回Message对象组装出来的Map对象，供后续JSON序列化
     *
     * @return Map
     */
    public abstract JSONObject toMessageMap();
}
