package com.maijin.captcha.controller;

import com.maijin.captcha.component.EmailMonitorStarter;
import com.maijin.captcha.service.AliEmailMonitor;
import com.maijin.captcha.service.MultiEmailMonitor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AliEmailController {

    @Autowired
    AliEmailMonitor aliEmailMonitor;
    @Autowired
    MultiEmailMonitor multiEmailMonitor;
    @Autowired
    EmailMonitorStarter emailMonitorStarter;
    @GetMapping("/aliemail/start")
    public String start() throws InterruptedException {
//        aliEmailMonitor.startMonitoring();

        emailMonitorStarter.startMonitoring();
        return "启动成功";
    }
    @GetMapping("/aliemail/startem")

    public String startEM(){
        multiEmailMonitor.startMtEmail();

        return "启动成功";
    }
}
