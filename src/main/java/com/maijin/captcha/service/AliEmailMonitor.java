package com.maijin.captcha.service;

import com.maijin.captcha.utils.ExtractVC;
import com.maijin.captcha.utils.dingtalk.client.DingTalkRobotClient;
import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.IMAPStore;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import javax.mail.*;
import javax.mail.event.MessageCountAdapter;
import javax.mail.event.MessageCountEvent;
import javax.mail.internet.MimeUtility;
import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class AliEmailMonitor {

    private static final Logger logger = LogManager.getLogger(AliEmailMonitor.class);
    private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日 HH:mm");

    public static class EmailMonitorTask implements Runnable {
        private final String host;
        private final String userName;
        private final String password;

        private IMAPStore store;  // 保持为局部变量
        private IMAPFolder inbox;  // 保持为局部变量
        public EmailMonitorTask(String host, String userName, String password) {
            this.host = host;
            this.userName = userName;
            this.password = password;
        }

        @Override
        public void run() {
            // 通过创建新的Session、IMAPStore、IMAPFolder确保线程安全
            Properties properties = new Properties();
            properties.put("mail.store.protocol", "imap");
            properties.put("mail.imap.host", host);
            properties.put("mail.imap.port", "993");
            properties.put("mail.imap.ssl.enable", "true");

            Session session = Session.getDefaultInstance(properties);
            try {
                store = (IMAPStore) session.getStore("imap");
            } catch (NoSuchProviderException e) {
                throw new RuntimeException(e);
            }
            logger.info("正在连接到邮件服务器... 邮箱："+ userName);
            try {
                store.connect(userName, password);
            } catch (MessagingException e) {
                System.out.println(userName);
                throw new RuntimeException(e);
            }
            try {
                inbox = (IMAPFolder) store.getFolder("INBOX");
            } catch (MessagingException e) {
                throw new RuntimeException(e);
            }
            try {
                inbox.open(Folder.READ_ONLY);
            } catch (MessagingException e) {
                throw new RuntimeException(e);
            }

            inbox.addMessageCountListener(new MessageCountAdapter() {
                @Override
                public void messagesAdded(MessageCountEvent e) {
                    handleNewMessages(e);
                }
            });

            // 定时发送 NOOP 命令保持连接活跃
            ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
            scheduler.scheduleAtFixedRate(() -> {
                try {
                    sendNoopCommand(host);
                    logger.info("保持连接活跃... 邮箱: " + userName);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }, 0, 3, TimeUnit.MINUTES);

            // 进入IDLE模式
            logger.info(" 开始监听邮件... 邮箱: "+ userName);
            int retryCount = 0;
            final int maxRetries =200;
            boolean isListening = true;

            while (isListening) {


                try {
                    inbox.idle();
                    retryCount = 0;
                } catch (MessagingException e) {
                    retryCount ++;

                    logger.info(sdf.format(new Date()) + " 邮箱 " + userName + " 的连接已关闭，正在重新连接...");
                    reconnect(); // 调用时不传递参数
                    if (retryCount >= maxRetries) {
                        logger.error("达到最大重试次数，停止尝试连接。");
                        isListening = false; // 停止循环
                    }
                }
            }

        }

        private void reconnect() {
            try {
                if (inbox != null && inbox.isOpen()) {
                    inbox.close();
                }
                store.close();
                logger.info("连接已关闭，重新尝试连接... 邮箱：" + userName);

                // 重新连接并获取新的 inbox
                store = connectToMailServer(userName, password);  // 重新建立连接并返回 inbox
                inbox = (IMAPFolder) store.getFolder("INBOX");
                inbox.open(Folder.READ_ONLY);  // 打开文件夹

                // 重新注册消息监听器
                inbox.addMessageCountListener(new MessageCountAdapter() {
                    @Override
                    public void messagesAdded(MessageCountEvent e) {
                        handleNewMessages(e);
                    }
                });

                // 重新进入IDLE模式
                logger.info("成功重新连接到邮件服务器并开始监听邮件。 邮箱：" + userName);
                inbox.idle();

            } catch (Exception reconnectionEx) {
                reconnectionEx.printStackTrace();
                logger.info("重新连接失败。 邮箱：" + userName);
            }
        }

        private IMAPStore connectToMailServer(String userName, String password) throws Exception {
            Properties properties = new Properties();
            properties.put("mail.store.protocol", "imap");
            properties.put("mail.imap.host", host);
            properties.put("mail.imap.port", "993");
            properties.put("mail.imap.ssl.enable", "true");

            Session session = Session.getDefaultInstance(properties);
            IMAPStore newStore = (IMAPStore) session.getStore("imap");
            newStore.connect(userName, password);

            IMAPFolder newInbox = (IMAPFolder) newStore.getFolder("INBOX");
            newInbox.open(Folder.READ_ONLY);  // 打开文件夹

            return newStore;  // 返回新的 IMAPStore 实例
        }


        private static void handleNewMessages(MessageCountEvent e) {
            try {
                Message[] messages = e.getMessages();
                for (Message message : messages) {
                    String subject = message.getSubject();
                    if(!subject.contains("인증")){
                        logger.info("非验证码邮件");
                        continue;
                    }
                    for (Address address : message.getFrom()) {
                        String from = MimeUtility.decodeText(address.toString());
                        String decodedAddress = MimeUtility.decodeText(from);
                        String domain = extractDomainFromEmail(decodedAddress);

                        if (domain == null) {
                            return;
                        }

                        String senderName = domain;
                        String verificationCode = ExtractVC.getVC(getContent(message));
                        if (verificationCode != null && !verificationCode.isEmpty()) {
                            new DingTalkRobotClient("accessToken").sendMarkdownMessage(
                                    verificationCode, "#### 验证码：" + verificationCode + "\n" +
                                            ">标题：" + subject + "\n\n" +
                                            ">发件人：" + senderName + "\n\n" +
                                            ">时间：" + sdf.format(new Date()) + "\n"
                            );
                            logger.info("发件人：" + from + "/验证码：" + verificationCode);
                            return;
                        }

                        if (verificationCode == null) {
                            logger.info("空验证码");
                            return;
                        }

                        logger.info("非注册邮箱");
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        private static String extractDomainFromEmail(String decodedAddress) {
            String emailPattern = "<(.*?)>";
            Pattern pattern = Pattern.compile(emailPattern);
            Matcher matcher = pattern.matcher(decodedAddress);
            if (matcher.find()) {
                String email = matcher.group(1);
                String domainWithSuffix = email.substring(email.indexOf('@') + 1);
                return domainWithSuffix.split("\\.")[0]; // 提取域名
            }
            return null;
        }

        private static void sendNoopCommand(String host) throws IOException {
            try (Socket socket = new Socket(host, 993);
                 OutputStream outputStream = socket.getOutputStream()) {
                outputStream.write("NOOP\r\n".getBytes(StandardCharsets.UTF_8));
                outputStream.flush();
            }
        }

        private static String getContent(Message message) throws MessagingException, IOException {
            StringBuilder contentBuilder = new StringBuilder();

            if (message.isMimeType("multipart/*")) {
                Multipart multipart = (Multipart) message.getContent();
                for (int i = 0; i < multipart.getCount(); i++) {
                    processBody(contentBuilder, multipart.getBodyPart(i));
                }
            } else {
                contentBuilder.append("内容: ").append(message.getContent()).append("\n");
            }

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
            } else if (bodyPart.isMimeType("image/*")) {
                // Ignore embedded images
            }
        }
    }



}
