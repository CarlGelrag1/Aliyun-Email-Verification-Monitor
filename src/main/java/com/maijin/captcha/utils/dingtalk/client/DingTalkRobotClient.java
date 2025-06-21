package com.maijin.captcha.utils.dingtalk.client;


import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSON;
import com.aliyun.credentials.utils.StringUtils;


import com.maijin.captcha.common.ServiceException;
import com.maijin.captcha.utils.dingtalk.DingTalkConfig;
import com.maijin.captcha.utils.dingtalk.entity.BaseMessage;
import com.maijin.captcha.utils.dingtalk.entity.LinkMessage;
import com.maijin.captcha.utils.dingtalk.entity.MarkdownMessage;
import com.maijin.captcha.utils.dingtalk.entity.TextMessage;
import com.maijin.captcha.utils.dingtalk.response.DingTalkResponse;

public class DingTalkRobotClient {

    /**
     * 钉钉机器人WebHook地址的access_token
     */
    private String accessToken;

    private static String DING_TALK_PATH = DingTalkConfig.webhook;

    public DingTalkRobotClient(String token) throws Exception {
        if (StringUtils.isEmpty(token)) {
            throw new ServiceException("accessToken获取失败！");
        }
        this.accessToken = token;
    }


    private DingTalkResponse sendMessage(BaseMessage message) {
        String result = HttpUtil.post(DING_TALK_PATH.replace("ACCESS_TOKEN", this.accessToken), message.toMessageMap().toString());
        DingTalkResponse dingTalkResponse = JSON.parseObject(result, DingTalkResponse.class);
        // 对DingTalkResponse为空情况做异常封装
        if (dingTalkResponse == null) {
            throw new ServiceException("请求钉钉报错！");
        }
        if (dingTalkResponse.getErrcode() != 0) {
            throw new ServiceException(String.format("错误码:%s;%s", dingTalkResponse.getErrcode(), dingTalkResponse.getErrmsg()));
        }
        return dingTalkResponse;
    }

    /**
     * 发送文本消息到钉钉
     *
     * @param message
     * @return
     */
    public DingTalkResponse sendTextMessage(TextMessage message) {
        return this.sendMessage(message);
    }

    /**
     * 发送文本消息到钉钉
     *
     * @param content
     * @return
     */
    public DingTalkResponse sendTextMessage(String content) {
        return this.sendMessage(new TextMessage(content));
    }

    /**
     * 发送文本消息到钉钉
     *
     * @param content
     * @param atMobiles
     * @return
     */
    public DingTalkResponse sendTextMessage(String content, String[] atMobiles) {
        return this.sendMessage(new TextMessage(content, atMobiles));
    }

    /**
     * 发送文本消息到钉钉
     *
     * @param content
     * @param isAtAll
     * @return
     */
    public DingTalkResponse sendTextMessage(String content, boolean isAtAll) {
        return this.sendMessage(new TextMessage(content, isAtAll));
    }

    /**
     * 发送Link消息到钉钉
     *
     * @param message
     * @return
     */
    public DingTalkResponse sendLinkMessage(LinkMessage message) {
        return this.sendMessage(message);
    }

    /**
     * 发送Link消息到钉钉
     *
     * @param title
     * @param text
     * @param messageUrl
     * @return
     */
    public DingTalkResponse sendLinkMessage(String title, String text, String messageUrl) {
        return this.sendMessage(new LinkMessage(title, text, messageUrl));
    }

    /**
     * 发送Link消息到钉钉
     *
     * @param title
     * @param text
     * @param messageUrl
     * @param picUrl
     * @return
     */
    public DingTalkResponse sendLinkMessage(String title, String text, String messageUrl, String picUrl) {
        return this.sendMessage(new LinkMessage(title, text, messageUrl, picUrl));
    }

    /**
     * 发送MarkDown消息到钉钉
     *
     * @param message
     * @return
     */
    public DingTalkResponse sendMarkdownMessage(MarkdownMessage message) {
        return this.sendMessage(message);
    }

    /**
     * 发送MarkDown消息到钉钉
     *
     * @param title
     * @param text
     * @return
     */
    public DingTalkResponse sendMarkdownMessage(String title, String text) {
        return this.sendMessage(new MarkdownMessage(title, text));
    }

    /**
     * 发送MarkDown消息到钉钉
     *
     * @param title
     * @param text
     * @param atMobiles
     * @return
     */
    public DingTalkResponse sendMarkdownMessage(String title, String text, String[] atMobiles) {
        return this.sendMessage(new MarkdownMessage(title, text, atMobiles));
    }

    /**
     * 发送MarkDown消息到钉钉
     *
     * @param title
     * @param text
     * @param isAtAll
     * @return
     */
    public DingTalkResponse sendMarkdownMessage(String title, String text, boolean isAtAll) {
        return this.sendMessage(new MarkdownMessage(title, text, isAtAll));
    }


    public static void main(String[] args) throws Exception {
        new DingTalkRobotClient("accessToken").sendMarkdownMessage("构建任务", "验证码通知\n" +
                "> 9度，西北风1级，空气良89，相对温度73%\n\n" +
                "> ![screenshot](https://gw.alicdn.com/tfs/TB1ut3xxbsrBKNjSZFpXXcXhFXa-846-786.png)\n" +
                "> ###### 10点20分发布 [天气]() \n");
    }
}
