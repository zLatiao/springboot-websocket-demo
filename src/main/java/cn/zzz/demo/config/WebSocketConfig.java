package cn.zzz.demo.config;

import cn.zzz.demo.listener.WebSocketListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.ExecutorChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketTransportRegistration;

@Configuration
// @EnableWebSocketMessageBroker: 启用 WebSocket 消息处理功能，并基于消息代理实现支持
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private static final Logger log = LoggerFactory.getLogger(WebSocketConfig.class);

    private TaskScheduler messageBrokerTaskScheduler;

    @Autowired
    public void setMessageBrokerTaskScheduler(@Lazy TaskScheduler taskScheduler) {
        this.messageBrokerTaskScheduler = taskScheduler;
    }

    /**
     * 重写 WebSocketMessageBrokerConfigurer 的默认方法，用于配置消息代理：
     *
     * @param registry
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // 启用简单的内存消息代理，负责将消息传递到目标地址以 /topic 为前缀的客户端（例如：@SendTo("/topic/greetings")，广播消息到 /topic/greetings）。
        registry.enableSimpleBroker("/topic")
                // 如果配置了任务调度器，简单的代理服务器支持STOMP心跳。要配置调度器，你可以声明自己的 TaskScheduler bean，并通过 MessageBrokerRegistry 进行设置。或者，你可以使用内置WebSocket配置中自动声明的调度器，不过，你需要使用 @Lazy 来避免内置WebSocket配置和你的 WebSocketMessageBrokerConfigurer 之间出现循环。
                .setHeartbeatValue(new long[]{10000, 20000})
                .setTaskScheduler(this.messageBrokerTaskScheduler);
        // 设置应用目标前缀，所有通过 @MessageMapping 注解映射的消息（如 @MessageMapping("/hello")）的实际路径需以 /app 开头（完整路径为 /app/hello）
        registry.setApplicationDestinationPrefixes("/app");
        // 启用用户消息代理，负责将消息传递给特定用户（例如：@SendToUser("/queue/errors")，发送消息到 /queue/errors）。
        registry.setUserDestinationPrefix("/user");
        // 使用AntPathMatcher的话，控制器可以在@MessageMapping方法中使用点号（.）作为分隔符，而不是/
//        registry.setPathMatcher(new AntPathMatcher("."));

        // 启用有序发布：
        // 来自代理的消息会发布到 clientOutboundChannel，并从该通道写入WebSocket会话。
        // 由于该通道由 ThreadPoolExecutor 支持，消息会在不同线程中处理，因此客户端接收到的最终顺序可能与发布的确切顺序不匹配。
        // 当该标志被设置时，同一客户端会话内的消息将一次一个地发布到clientOutboundChannel，从而保证发布顺序。请注意，这会带来少量性能开销，因此仅在有需要时启用它。
        registry.setPreservePublishOrder(true);
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // 定义 STOMP 协议的连接端点为 /gs-guide-websocket，客户端需通过此端点建立 WebSocket 连接。（暴露 STOMP 连接端点，客户端通过此 URL 建立 WebSocket 连接）
        registry.addEndpoint("/gs-guide-websocket");

        // 同样的情况也适用于来自客户端的消息，这些消息被发送到 clientInboundChannel，并根据其目标前缀在该通道进行处理。由于该通道由 ThreadPoolExecutor 提供支持，消息会在不同的线程中处理，因此处理的顺序可能与接收的顺序不完全一致。
        // 要启用有序接收，请按如下方式设置 setPreserveReceiveOrder 标志：
        registry.setPreserveReceiveOrder(true);
    }

    /**
     * 配置客户端入站通道
     *
     * @param registration
     */
    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
                if (StompCommand.CONNECT.equals(accessor.getCommand())) {
                    // 这里可以做认证
                    MessageHeaders messageHeaders = accessor.getMessageHeaders();
                    System.out.println();
                }
                return message;
            }
        }).interceptors(new ExecutorChannelInterceptor() {
            @Override
            public void afterMessageHandled(Message<?> message, MessageChannel channel, MessageHandler handler, Exception ex) {
                log.info("afterMessageHandled");
            }
        });
    }

    @Override
    public void configureWebSocketTransport(WebSocketTransportRegistration registry) {
        // endTimeLimit和sendBufferSizeLimit。你可以使用这些方法来配置允许一次发送花费多长时间，以及在向客户端发送消息时可以缓冲多少数据。
        registry.setSendTimeLimit(15 * 1000).setSendBufferSizeLimit(512 * 1024);

        // Spring的基于WebSocket的STOMP支持就是这样做的，因此应用程序可以配置STOMP消息的最大大小，而无需考虑特定于WebSocket服务器的消息大小。请记住，如有必要，WebSocket消息大小会自动调整，以确保它们至少能够承载16K的WebSocket消息。
        registry.setMessageSizeLimit(128 * 1024);
    }
}
