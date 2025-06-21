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
import java.util.Date;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service

public class AliEmailMonitor2 {


    private static IMAPFolder inbox;
    private static IMAPStore store;
    private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日 HH:mm");
    private static final Logger logger = LogManager.getLogger(AliEmailMonitor2.class);
    public  void startMonitoring(){
        try {
            String host = "imap.mxhichina.com";
            String userName = "@maejin.cn";
            String password = ""; // 授权码

            Properties properties = new Properties();
            properties.put("mail.store.protocol", "imap");
            properties.put("mail.imap.host", host);
            properties.put("mail.imap.port", "993");
            properties.put("mail.imap.ssl.enable", "true");

            Session session = Session.getDefaultInstance(properties);
            connectToMailServer(session, userName, password);

            // 定时发送 NOOP 命令保持连接活跃
            ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
            scheduler.scheduleAtFixedRate(() -> {
                try {
                    sendNoopCommand();
                    logger.info("保持连接活跃...");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }, 0, 3, TimeUnit.MINUTES);

            // 进入IDLE模式
            System.out.println("监听邮件中...");
            int retryCount = 0;
            final int maxRetries = 20;
            boolean isListening = true;
            while (isListening) {
                try {
                    inbox.idle();
                    retryCount = 0;
                } catch (FolderClosedException e) {
                    retryCount ++;
                    logger.info(sdf.format(new Date())+"连接已关闭，正在重新连接...");
                    reconnect(userName, password, session);
                    if (retryCount >= maxRetries) {
                        logger.error("达到最大重试次数，停止尝试连接。");
                        isListening = false; // 停止循环
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        logger.info("Test");
    }



    private static void connectToMailServer(Session session, String userName, String password) throws Exception {
        store = (IMAPStore) session.getStore("imap");
        store.connect(userName, password);
        inbox = (IMAPFolder) store.getFolder("INBOX");
        inbox.open(Folder.READ_ONLY);
        inbox.addMessageCountListener(new MessageCountAdapter() {
            @Override
            public void messagesAdded(MessageCountEvent e) {
                handleNewMessages(e);
            }
        });
    }

    private static void sendNoopCommand() throws IOException {
        Socket socket = new Socket("imap.mxhichina.com", 993);
        OutputStream outputStream = socket.getOutputStream();
        outputStream.write("NOOP\r\n".getBytes(StandardCharsets.UTF_8));
        outputStream.flush();
        socket.close();
    }

    private static void reconnect(String userName, String password, Session session) {
        try {
            if (inbox != null && inbox.isOpen()) {
                inbox.close();
            }
            store.close();
            connectToMailServer(session, userName, password);
            logger.info("成功重新连接到邮件服务器。");
        } catch (Exception reconnectionEx) {
            reconnectionEx.printStackTrace();
            logger.info("重新连接失败。");

        }
    }

    private static void handleNewMessages(MessageCountEvent e) {




        try {
            Message[] messages = e.getMessages();
            for (Message message : messages) {
                String subject = message.getSubject();
                for (Address address : message.getFrom()) {
                    String from = MimeUtility.decodeText(address.toString());
                    // 解码邮件地址
                    String decodedAddress = MimeUtility.decodeText(from);

                    // 正则表达式提取邮件地址中的域名部分
                    String emailPattern = "<(.*?)>";
                    Pattern pattern = Pattern.compile(emailPattern);
                    Matcher matcher = pattern.matcher(decodedAddress);
                    String domain = null;
                    if (matcher.find()) {
                        // 提取邮件地址部分
                        String email = matcher.group(1);

                        // 使用正则提取域名部分
                        String domainWithSuffix = email.substring(email.indexOf('@') + 1);

                        // 去除后缀部分 (例如 .com, .net 等)
                        String domainName = domainWithSuffix.split("\\.")[0];
                        domain = domainName;
                    }

                    if(domain==null){
                         return;
                    }
                    String senderName = domain; // 获取与发件人对应的值
                    String verificationCode = ExtractVC.getVC(getContent(message));
                    if (verificationCode != null && !verificationCode.isEmpty()) {

                        new DingTalkRobotClient("accessToken").sendMarkdownMessage(ExtractVC.getVC(getContent(message)), "#### 验证码：" + ExtractVC.getVC(getContent(message))+"\n" +
                                        ">标题：" + subject + "\n\n" +
                                        ">发件人：" + senderName+"\n\n"+
                                        ">时间：" + sdf.format(new Date()) + "\n"
                        );
                        logger.info("发件人："+from+"/验证码："+ExtractVC.getVC(getContent(message)));
                        return;
                    }
                    if(verificationCode == null){
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






    private static void saveAttachment(BodyPart bodyPart) throws Exception {
        InputStream is = bodyPart.getInputStream();
        File file = new File(bodyPart.getFileName());
        try (OutputStream os = new FileOutputStream(file)) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
        }
        System.out.println("附件已保存: " + file.getAbsolutePath());
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
