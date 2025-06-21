# 验证码邮件监控系统
这是一个基于 Java 的 Spring Boot 项目，主要用于监听邮箱中的验证码邮件，并通过钉钉机器人将验证码发送到指定群组。该项目支持多线程监控多个邮箱账号，并具备自动重连、定时保持连接活跃等功能。

# 目录结构
.
├── main
│   ├── java
│   │   └── com
│   │       └── maijin
│   │           └── captcha
│   │               ├── common                  # 公共异常类
│   │               │   └── ServiceException.java
│   │               ├── component               # 组件模块
│   │               │   ├── EmailMonitorStarter.java    # 多线程启动邮件监控任务
│   │               │   └── ProfileService.java         # 环境配置服务
│   │               ├── config                  # 配置模块
│   │               │   ├── AsyncConfig.java    # 异步任务配置
│   │               │   └── EmailConfig.java    # 邮件账户配置信息
│   │               ├── controller              # 控制器模块
│   │               │   └── AliEmailController.java     # Web API 接口
│   │               ├── domain                  # 数据模型
│   │               │   └── Account.java        # 用户账号实体类
│   │               ├── service                 # 核心业务逻辑
│   │               │   ├── AliEmailMonitor.java      # 阿里云邮箱监听器（主实现）
│   │               │   ├── AliEmailMonitor2.java     # 阿里云邮箱监听器（备用实现）
│   │               │   └── MultiEmailMonitor.java    # 多邮箱统一转发监听器
│   │               ├── utils                   # 工具类模块
│   │               │   ├── dingtalk            # 钉钉消息推送相关工具
│   │               │   │   ├── client
│   │               │   │   │   └── DingTalkRobotClient.java  # 钉钉机器人客户端
│   │               │   │   ├── entity          # 消息实体类
│   │               │   │   │   ├── BaseMessage.java
│   │               │   │   │   ├── LinkMessage.java
│   │               │   │   │   ├── MarkdownMessage.java
│   │               │   │   │   └── TextMessage.java
│   │               │   │   ├── enums           # 消息类型枚举
│   │               │   │   │   └── DingTalkMsgEnum.java
│   │               │   │   ├── response        # 响应对象
│   │               │   │   │   └── DingTalkResponse.java
│   │               │   │   └── DingTalkConfig.java   # 钉钉配置类
│   │               │   ├── gmail               # Gmail 示例监听器
│   │               │   │   └── GmailMonitor.java
│   │               │   └── ExtractVC.java      # 验证码提取工具类
│   │               └── CaptchaApplication.java # 启动类
│   └── resources
│       ├── application.yml                     # 应用配置文件
│       └── log4j2.xml                          # 日志配置文件
└── test
    └── java
        └── com
            └── maijin
                └── captcha
                    └── CaptchaApplicationTests.java  # 单元测试
## 主要功能模块说明
📦 common/ServiceException.java
自定义运行时异常类，用于封装全局异常信息。
支持携带错误信息与异常堆栈。
 
# ⚙️ component/EmailMonitorStarter.java
使用 TaskExecutor 启动多个邮箱监听任务。
可同时监控多个阿里云邮箱账号，每个账号独立线程运行。
 
# 🌐 config/AsyncConfig.java
配置异步任务执行器，使用 ThreadPoolTaskExecutor 提供线程池支持。
最大支持 50 个并发线程。
 
# 📨 controller/AliEmailController.java
提供两个 RESTful 接口：
/aliemail/start：启动邮箱验证码监听
/aliemail/startem：启动多邮箱统一转发监听
 
# 🧠 service/AliEmailMonitor.java
核心邮箱监听类，使用 IMAP 协议监听阿里云邮箱。
支持 IDLE 模式实时监听新邮件。
自动处理连接中断并尝试重连。
解析邮件内容，提取验证码并通过钉钉通知。
 
# 📲 utils/dingtalk/*
集成钉钉机器人，支持多种消息格式（Markdown、文本、链接）。
包含消息实体、响应封装、配置加载等组件。
 
# 🔍 utils/ExtractVC.java
从 HTML 或文本邮件中提取六位数字验证码。
支持 <span>、<td>、<strong> 标签包裹的内容解析。
 
# 📧 service/MultiEmailMonitor.java
多邮箱统一监听模块。
所有邮箱收到验证码后统一转发至一个目标邮箱。
支持 SMTP 转发邮件。
 
# 📄 resources/application.yml
数据库配置、服务器端口、MyBatis Plus 配置等基础设置。
 
 
# 🧪 test/CaptchaApplicationTests.java
Spring Boot 单元测试入口，验证应用上下文是否正常加载。
 
# 如何运行
修改 application.yml 中数据库配置。
在 AliEmailMonitor.java 和 MultiEmailMonitor.java 中填写邮箱账号和密码。
配置钉钉机器人的 accessToken。
启动 Spring Boot 应用。
访问接口 /aliemail/start 开始监听验证码邮件。

JDK 17+
Maven 3.8+
MySQL 数据库（根据 application.yml 配置）

## ⚠️ 免责声明

-   本工具仅供学习和技术研究使用。
-   请勿用于任何商业或非法用途。
-   用户需自行承担因使用本工具而产生的一切风险和责任。