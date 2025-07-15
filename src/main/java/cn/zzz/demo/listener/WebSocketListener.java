package cn.zzz.demo.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.broker.BrokerAvailabilityEvent;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.*;

@Component
public class WebSocketListener {

    private static final Logger log = LoggerFactory.getLogger(WebSocketListener.class);

    /**
     * 指示代理何时可用或不可用。虽然 “简单” 代理在启动时立即可用，并在应用程序运行期间保持可用，但 STOMP “代理中继” 可能会失去与全功能代理的连接（例如，如果代理重新启动）。代理中继具有重新连接逻辑，并在全功能代理恢复时重新建立与代理的 “系统” 连接。因此，每当状态从已连接变为已断开或反之亦然时，都会发布此事件。使用 SimpMessagingTemplate 的组件应订阅此事件，并在代理不可用时避免发送消息。无论如何，它们在发送消息时应准备好处理 MessageDeliveryException。
     *
     * @param event
     */
    @EventListener(BrokerAvailabilityEvent.class)
    public void handleBrokerAvailabilityEvent(BrokerAvailabilityEvent event) {
        log.info("Broker availability: " + event.isBrokerAvailable());
    }

    /**
     * 当接收到新的STOMP CONNECT时发布，以指示新客户端会话的开始。该事件包含表示连接的消息，包括会话ID、用户信息（如果有）以及客户端发送的任何自定义标头。这对于跟踪客户端会话很有用。订阅此事件的组件可以使用 SimpMessageHeaderAccessor 或 StompMessageHeaderAccessor 包装所包含的消息。
     *
     * @param event
     */
    @EventListener(SessionConnectEvent.class)
    public void handleSessionConnectEvent(SessionConnectEvent event) {
        log.info("Session connect: " + event.getMessage());
    }

    /**
     * 在 SessionConnectEvent 发生后不久发布，此时代理已发送一个STOMP CONNECTED帧作为对CONNECT的响应。此时，可以认为STOMP会话已完全建立。
     *
     * @param event
     */
    @EventListener(SessionConnectedEvent.class)
    public void handleSessionConnectedEvent(SessionConnectedEvent event) {
        log.info("Session connected: " + event.getMessage());
    }

    /**
     * 在STOMP会话结束时发布。DISCONNECT 可能由客户端发送，也可能在WebSocket会话关闭时自动生成。在某些情况下，每个会话可能会多次发布此事件。对于多个断开连接事件，组件应具有幂等性。
     *
     * @param event
     */
    @EventListener(SessionDisconnectEvent.class)
    public void handleSessionDisconnectEvent(SessionDisconnectEvent event) {
        log.info("Session disconnected: " + event.getMessage());
    }

    /**
     * 在收到新的STOMP订阅时发布。
     *
     * @param event
     */
    @EventListener(SessionSubscribeEvent.class)
    public void handleSessionSubscribeEvent(SessionSubscribeEvent event) {
        log.info("Session subscribe: " + event.getMessage());
    }

    /**
     * 在接收到新的STOMP取消订阅请求时发布。
     *
     * @param event
     */
    @EventListener(SessionUnsubscribeEvent.class)
    public void handleSessionUnsubscribeEvent(SessionUnsubscribeEvent event) {
        log.info("Session unsubscribe: " + event.getMessage());
    }

}
