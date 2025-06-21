package com.maijin.captcha.config;

import java.util.Map;

public class EmailConfig {
    public static final String ZhangAO_FoxMail = " <>";
    public static final String Coupang ="<>";
    public static final Map<String, String> EmailInfo= Map.of(
            " <>", "",
            " <>", ""
    );
    public static final Map<String, String> EmailInfoDoMain= Map.of(
            "coupang.com", " Coupang",
            "navercorp.com", ""

    );


}
