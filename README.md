# Spring Boot WebSocket Demo

一个基于 Spring Boot 和 STOMP 协议的 WebSocket 实时通信演示项目。

## 项目简介

本项目演示了如何使用 Spring Boot 构建 WebSocket 应用程序，实现客户端与服务器之间的实时双向通信。项目采用 STOMP（Simple Text Oriented Messaging Protocol）协议，提供了完整的消息订阅、发布和广播功能。

## 技术栈

- **后端框架**: Spring Boot 3.5.3
- **Java 版本**: Java 21
- **WebSocket**: Spring WebSocket + STOMP
- **前端**: HTML + JavaScript + Bootstrap 3
- **消息协议**: STOMP over WebSocket
- **构建工具**: Maven

## 项目结构

```
src/
├── main/
│   ├── java/cn/zzz/demo/
│   │   ├── SpringbootWebsocketDemoApplication.java  # 主启动类
│   │   ├── config/
│   │   │   └── WebSocketConfig.java                 # WebSocket配置
│   │   ├── controller/
│   │   │   └── GreetingController.java              # 消息处理控制器
│   │   ├── listener/
│   │   │   └── WebSocketListener.java               # WebSocket事件监听器
│   │   └── model/
│   │       ├── Greeting.java                       # 问候消息模型
│   │       └── HelloMessage.java                   # 输入消息模型
│   └── resources/
│       ├── static/
│       │   ├── index.html                           # 前端页面
│       │   ├── app.js                               # 前端JavaScript逻辑
│       │   └── main.css                             # 样式文件
│       └── application.properties                   # 应用配置
└── doc/
    ├── STOMP外部消息代理工作流程.png
    └── STOMP简单内置消息代理工作流程.png
```

## 核心功能

### 1. WebSocket 连接管理
- 支持客户端连接/断开连接
- 连接状态实时监控
- 会话事件监听（连接、断开、订阅等）

### 2. 消息通信模式
- **点对点通信**: 客户端发送消息到服务器
- **广播通信**: 服务器向所有订阅客户端广播消息
- **用户专属消息**: 支持向特定用户发送消息

### 3. STOMP 协议支持
- 消息订阅/取消订阅
- 消息发布
- 心跳检测
- 异常处理

## 快速开始

### 环境要求
- JDK 21+
- Maven 3.6+

### 运行步骤

1. **克隆项目**
   ```bash
   git clone <repository-url>
   cd springboot-websocket-demo
   ```

2. **编译项目**
   ```bash
   mvn clean compile
   ```

3. **启动应用**
   ```bash
   mvn spring-boot:run
   ```
   
   或者运行主类：
   ```bash
   java -jar target/springboot-websocket-demo-0.0.1-SNAPSHOT.jar
   ```

4. **访问应用**
   
   打开浏览器访问：http://localhost:8081

### 使用说明

1. **建立连接**
   - 点击「连接」按钮建立 WebSocket 连接
   - 连接成功后会自动订阅消息频道

2. **发送消息**
   - 在输入框中输入名称
   - 点击「发送」按钮发送消息
   - 消息会广播给所有连接的客户端

3. **查看消息**
   - 所有接收到的消息会显示在页面下方的表格中
   - 支持实时消息推送

## API 端点

### WebSocket 端点
- **连接端点**: `ws://localhost:8081/gs-guide-websocket`

### 消息映射
- **发送消息**: `/app/hello` - 客户端发送问候消息
- **订阅广播**: `/topic/greetings` - 接收广播的问候消息
- **订阅消息**: `/topic/greeting` - 接收一般消息
- **用户消息**: `/user/queue/errors` - 接收错误消息

### REST 端点
- **GET** `/greetings?greeting={message}` - 通过 HTTP 发送广播消息

## 配置说明

### WebSocket 配置特性
- **消息代理**: 启用简单内存消息代理
- **心跳检测**: 10秒发送，20秒接收
- **有序发布**: 保证同一客户端消息发布顺序
- **有序接收**: 保证消息接收处理顺序
- **消息大小限制**: 128KB
- **发送缓冲区**: 512KB
- **发送超时**: 15秒

### 应用配置
```properties
server.port=8081
spring.application.name=springboot-websocket-demo
```

## 开发说明

### 消息流程
1. 客户端连接到 WebSocket 端点
2. 客户端订阅消息频道
3. 客户端发送 STOMP 消息到应用端点
4. 服务器处理消息并返回响应
5. 消息代理将响应广播给订阅者
6. 客户端接收并显示消息

### 扩展开发
- 添加用户认证：在 `WebSocketConfig.configureClientInboundChannel()` 中实现
- 自定义消息处理：扩展 `GreetingController` 添加新的 `@MessageMapping`
- 添加持久化：集成数据库存储聊天记录
- 集成外部消息代理：如 RabbitMQ、ActiveMQ 等

## 许可证

本项目仅用于学习和演示目的。

## 参考资料

- [Spring WebSocket 官方文档](https://docs.spring.io/spring-framework/reference/web/websocket/)
- [STOMP 协议规范](https://stomp.github.io/)
- [Spring Boot WebSocket 指南](https://spring.io/guides/gs/messaging-stomp-websocket/)