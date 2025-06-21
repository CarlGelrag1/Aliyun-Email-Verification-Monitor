package com.maijin.captcha.service;

import com.sun.mail.imap.IMAPFolder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import javax.mail.*;
import javax.mail.event.MessageCountAdapter;
import javax.mail.event.MessageCountEvent;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
@Service
public class MultiEmailMonitor {

    private static final Logger logger = LogManager.getLogger(MultiEmailMonitor.class);
    private static final String FORWARD_TO_EMAIL = "@maejin.cn"; // 统一转发目标邮箱

    // 存储多个邮箱账号的配置信息
    private static final List<Map<String, String>> emailAccounts = Arrays.asList(
        Map.of("host", "imap.mxhichina.com", "username", "@maejin.cn", "password", ""),
        Map.of("host", "imap.mxhichina.com", "username", "@maejin.cn", "password", "")
        // 添加更多邮箱信息
    );

    public void startMtEmail(){
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(emailAccounts.size());

        // 为每个邮箱创建一个监听任务
        for (Map<String, String> account : emailAccounts) {
            scheduler.execute(() -> startMonitoring(account));
        }
    }

    public static void startMonitoring(Map<String, String> account) {
        try {
            Properties properties = new Properties();
            properties.put("mail.store.protocol", "imap");
            properties.put("mail.imap.host", account.get("host"));
            properties.put("mail.imap.port", "993");
            properties.put("mail.imap.ssl.enable", "true");

            Session session = Session.getDefaultInstance(properties);
            Store store = session.getStore("imap");
            store.connect(account.get("username"), account.get("password"));

            IMAPFolder inbox = (IMAPFolder) store.getFolder("INBOX");
            inbox.open(Folder.READ_WRITE); // 需要可写权限以添加监听

            // 添加新邮件监听器
            inbox.addMessageCountListener(new MessageCountAdapter() {
                @Override
                public void messagesAdded(MessageCountEvent event) {
                    for (Message message : event.getMessages()) {
                        try {

                            if(message.getSubject().contains("인증")){
                                System.out.println("新邮件到达-转发: " + message.getSubject());
                                forwardEmail(message, account);                                 continue;

                            }
                            System.out.println("新邮件到达-不转发: " + message.getSubject());



                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            });

            // 定时发送 NOOP 命令保持连接活跃
            ScheduledExecutorService keepAliveScheduler = Executors.newSingleThreadScheduledExecutor();
            keepAliveScheduler.scheduleAtFixedRate(() -> sendNoopCommand(inbox), 0, 5, TimeUnit.MINUTES);

            // 进入 IDLE 模式监听
            System.out.println("开始监听邮箱: " + account.get("username"));
            while (true) {
                try {
                    inbox.idle();
                } catch (FolderClosedException e) {
                    System.out.println("连接关闭，正在重新连接邮箱: " + account.get("username"));
                    store.close();
                    store.connect(account.get("username"), account.get("password"));
                    inbox.open(Folder.READ_ONLY);

                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void forwardEmail(Message message, Map<String, String> account) throws Exception {
        String smtpHost = "smtp.qiye.aliyun.com";  // 替换为正确的 SMTP 主机
        String smtpPort = "465";  // SMTP 端口，465 用于 SSL，587 用于 TLS

        // 使用当前邮箱的账号信息动态设置 SMTP 会话
        String smtpUsername = account.get("username");
        String smtpPassword = account.get("password");



        // 设置 SMTP 连接的属性
        Properties smtpProperties = new Properties();
        smtpProperties.put("mail.smtp.host", smtpHost);
        smtpProperties.put("mail.smtp.port", smtpPort);
        smtpProperties.put("mail.smtp.auth", "true");
        smtpProperties.put("mail.smtp.ssl.enable", "true");  // 如果使用 SSL，设置为 true

        // 使用当前邮箱的认证信息
        Session smtpSession = Session.getInstance(smtpProperties, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(smtpUsername, smtpPassword);  // 使用动态邮箱的账号和密码
            }
        });
        System.out.println(smtpUsername+smtpPassword);
        MimeMessage forward = new MimeMessage(smtpSession);
        forward.setSubject("转发：" + message.getSubject());
        forward.setFrom(smtpUsername);
        forward.addRecipient(Message.RecipientType.TO, new InternetAddress(FORWARD_TO_EMAIL));
        forward.setContent(message.getContent(), message.getContentType());

        // 发送邮件
        Transport.send(forward, forward.getAllRecipients());
        System.out.println("邮件已成功转发到: " + FORWARD_TO_EMAIL);
    }


    private static void sendNoopCommand(Folder inbox) {
        try {
            if (inbox.isOpen()) {
                inbox.getMessageCount(); // NOOP 命令
            }
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

    private static String getContent(Message message) throws MessagingException, IOException {
        StringBuilder contentBuilder = new StringBuilder();

        // 处理邮件内容
        if (message.isMimeType("multipart/*")) {
            Multipart multipart = (Multipart) message.getContent();
            for (int i = 0; i < multipart.getCount(); i++) {
                processBody(contentBuilder, multipart.getBodyPart(i));
            }
        } else {
            // 直接获取Message的内容
            contentBuilder.append("内容: ").append(message.getContent()).append("\n");
        }

//        System.out.println("----------------------------------------------------");
        return contentBuilder.toString();
    }

    private static void processBody(StringBuilder contentBuilder, BodyPart bodyPart) throws MessagingException, IOException {
        if (bodyPart.isMimeType("text/plain")) {
            contentBuilder.append("内容: ").append(bodyPart.getContent()).append("\n");
        } else if (bodyPart.isMimeType("text/html")) {
            contentBuilder.append("HTML 内容: ").append(bodyPart.getContent()).append("\n");
        } else if (bodyPart.isMimeType("multipart/*")) {
            Multipart multipart = (Multipart) bodyPart.getContent();
            for (int i = 0; i < multipart.getCount(); i++) {
                processBody(contentBuilder, multipart.getBodyPart(i));
            }
        } else if (Part.ATTACHMENT.equalsIgnoreCase(bodyPart.getDisposition())) {
            System.out.println("附件: " + bodyPart.getFileName());
            // saveAttachment(bodyPart);
        } else if (bodyPart.isMimeType("image/*")) {
//            System.out.println("忽略嵌入图片: " + bodyPart.getHeader("Content-ID")[0]);
        }
    }
}
