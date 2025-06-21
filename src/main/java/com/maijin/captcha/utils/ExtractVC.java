package com.maijin.captcha.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExtractVC {
    public static String getVC(String content) {
        // 匹配六位数字，且必须位于 <span> 或 <td> 标签内，允许标签内有空白字符或其他属性
        String regexNaver = "<(span|td|strong)\\b[^>]*>\\s*(\\d{6})\\s*<\\/\\1>";
        Pattern pattern = Pattern.compile(regexNaver, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(content);

        if (matcher.find()) {
            String verifyCode = matcher.group(2); // 提取验证码（第二个捕获组）
            return verifyCode;
        }
        return null;
    }




}
