package com.maijin.captcha.component;

import com.maijin.captcha.service.AliEmailMonitor;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Profile;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class EmailMonitorStarter {


    @Autowired
    private AliEmailMonitor aliEmailMonitor;

    @Qualifier("taskExecutor")
    @Autowired
    private TaskExecutor taskExecutor;
    private List<AliEmailMonitor.EmailMonitorTask> tasks;

    public void startMonitoring() throws InterruptedException {




        // 创建一系列监控任务
        tasks = Arrays.asList(
                new AliEmailMonitor.EmailMonitorTask("imap.mxhichina.com", "@maejin.cn", ""),
                new AliEmailMonitor.EmailMonitorTask("imap.mxhichina.com", "@maejin.cn", "")

                // 可以添加更多邮件帐户
        );

        // 使用 TaskExecutor 来异步执行每个任务
        for (AliEmailMonitor.EmailMonitorTask task : tasks) {
            taskExecutor.execute(task);  // 使用 TaskExecutor 的 execute 方法
        }
    }



}
