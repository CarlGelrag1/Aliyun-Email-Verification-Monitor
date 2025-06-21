package com.maijin.captcha.utils.gmail;

import com.sun.mail.imap.IMAPFolder;
import javax.mail.*;
import javax.mail.event.MessageCountAdapter;
import javax.mail.event.MessageCountEvent;
import java.util.Properties;

public class GmailMonitor {

    public static void main(String[] args) {
        try {
            // 设置IMAP连接的属性
            Properties props = new Properties();
            props.put("mail.store.protocol", "imaps");
            props.put("mail.imap.host", "imap.gmail.com");
            props.put("mail.imap.port", "993");
            props.put("mail.imap.ssl.enable", "true");
            props.put("mail.imap.connectiontimeout", "200000");
            props.put("mail.imap.timeout", "200000");

            // 用户认证信息
            String user = "clipen.wq@gmail.com";
            String password = "uizj bqdf hwco xirs";

            // 创建会话对象
            Session session = Session.getInstance(props);

            // 连接到Gmail IMAP服务器
            Store store = session.getStore("imaps");
            store.connect("imap.gmail.com", user, password);

            // 打开收件箱
            IMAPFolder inbox = (IMAPFolder) store.getFolder("INBOX");
            inbox.open(Folder.READ_WRITE); // 需要可写权限以添加监听

            // 监听新邮件到达事件
            inbox.addMessageCountListener(new MessageCountAdapter() {
                @Override
                public void messagesAdded(MessageCountEvent e) {
                    try {
                        Message[] messages = e.getMessages();
                        for (Message message : messages) {
                            String subject = message.getSubject();
                            String content = getContent(message);
                            System.out.println("主题: " + subject);
                            System.out.println("内容: " + content);
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            });

            // 进入IDLE模式
            System.out.println("监听邮件中...");
            while (true) {
                inbox.idle(); // 进入IDLE状态，等待邮件到达
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String getContent(Message message) throws Exception {
        if (message.isMimeType("text/plain")) {
            return message.getContent().toString();
        } else if (message.isMimeType("text/html")) {
            return message.getContent().toString(); // 根据需要处理HTML
        } else if (message.isMimeType("multipart/*")) {
            Multipart multipart = (Multipart) message.getContent();
            StringBuilder content = new StringBuilder();
            for (int i = 0; i < multipart.getCount(); i++) {
                BodyPart bodyPart = multipart.getBodyPart(i);
                if (bodyPart.isMimeType("text/plain")) {
                    content.append(bodyPart.getContent().toString());
                } else if (bodyPart.isMimeType("text/html")) {
                    content.append(bodyPart.getContent().toString()); // 根据需要处理HTML
                }
            }
            return content.toString();
        }
        return "";
    }
}
