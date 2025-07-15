package cn.zzz.demo.controller;

import cn.zzz.demo.model.Greeting;
import cn.zzz.demo.model.HelloMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.util.HtmlUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@Controller
public class GreetingController {

    private SimpMessagingTemplate template;

    @Autowired
    public GreetingController(SimpMessagingTemplate template) {
        this.template = template;
    }

    /**
     * SimpMessagingTemplate 发消息，客户端订阅/topic/greeting可接收
     * @param greeting
     */
    @RequestMapping(path="/greetings", method=GET)
    @ResponseBody
    public void greet(String greeting) {
        String text = "[" + getTimestamp() + "]:" + greeting;
        this.template.convertAndSend("/topic/greeting", text);
    }


    /**
     * 客户端发送目标为/app/hello的SEND帧会到这里
     * 客户端定于/topic/greetings会接受这里的返回值
     *
     * @param message
     * @return
     * @throws Exception
     */
    @MessageMapping("/hello")
    @SendTo("/topic/greetings")
    public Greeting greeting(HelloMessage message, SimpMessageHeaderAccessor headerAccessor) throws Exception {
//        int a = 1 / 0;
        Map<String, Object> sessionAttributes = headerAccessor.getSessionAttributes();
        Thread.sleep(1000);
        return new Greeting("Hello, " + HtmlUtils.htmlEscape(message.getName()) + "!");
    }


    /**
     * Spring官方文档：
     * <a href="https://docs.spring.io/spring-framework/reference/web/websocket/stomp/message-flow.html">...</a>
     * <p>
     * 流程：
     * <p>
     * 1. 客户端连接到localhost:8080/gs-guide-websocket,
     * 一旦WebSocket连接建立，STOMP帧就开始在其上传输。
     * 即{@link cn.zzz.demo.config.WebSocketConfig#registerStompEndpoints(StompEndpointRegistry)}里面配置的endPoint
     * <p>
     * 2. 客户端发送一个带有目标头为 /topic/greeting 的 SUBSCRIBE 帧。
     * 一旦接收到并解码，该消息将被发送到 clientInboundChannel，然后被路由到消息代理，消息代理会存储客户端订阅。
     * {@link cn.zzz.demo.config.WebSocketConfig#configureMessageBroker(MessageBrokerRegistry)}的enableSimpleBroker
     * <p>
     * 3. 客户端向 /app/greeting 发送一个 SEND 帧。
     * /app 前缀有助于将其路由到带注释的控制器。
     * 去除 /app 前缀后，目的地剩余的 /greeting 部分将映射到该方法。
     * {@link cn.zzz.demo.config.WebSocketConfig#configureMessageBroker(MessageBrokerRegistry)}setApplicationDestinationPrefixes
     * <p>
     * 4. 方法返回的值会被转换为一个Spring Message，其负载基于返回值，默认目标头为/topic/greeting（通过将输入目标中的 /app 替换为 /topic 得到）。
     * 生成的消息会被发送到 brokerChannel 并由消息代理处理。
     * <p>
     * 5. 消息代理会找到所有匹配的订阅者，并通过clientOutboundChannel向每个订阅者发送一个MESSAGE帧，消息从这里被编码为STOMP帧，并通过WebSocket连接发送出去。
     *
     * @param greeting String
     * @return String
     */
    @MessageMapping("/greeting")
    public String handle(String greeting) {
        return "[" + getTimestamp() + ": " + greeting;
    }

    private String getTimestamp() {
        return new SimpleDateFormat("MM/dd/yyyy h:mm:ss a").format(new Date());
    }

    /**
     * 客户端订阅/app/greeting会路由到这里
     *
     * @return
     */
    @SubscribeMapping("/greeting")
    public String handleSubscribe() {
        return "[" + getTimestamp() + ": " + "Welcome to Spring Web]";
    }

    /**
     * 应用程序可以使用 @MessageExceptionHandler 方法来处理 @MessageMapping 方法抛出的异常。
     * 如果想访问异常实例，可以在注解本身中声明异常，也可以通过方法参数来声明。
     * <p>
     * 注解 @SendToUser("/queue/errors") 需要客户端订阅/user/queue/errors，
     * 其中/user是{@link cn.zzz.demo.config.WebSocketConfig#configureMessageBroker(MessageBrokerRegistry)}的setUserDestinationPrefix配置的
     *
     * todo 用不了， @SendToUser好像要配合Spring Security才能用，或者自定义重写DefaultHandshakeHandler#determineUser来返回Principal
     *
     * @param e
     * @return
     */
    @MessageExceptionHandler(Throwable.class)
    @SendToUser(value = "/queue/errors", broadcast = false)
    public String handleException(Throwable e) {
        return "new Result().setCode(500).setMsg(e.getMessage());";
    }

    public static class Result {
        private Integer code;

        private String msg;

        public Integer getCode() {
            return code;
        }

        public Result setCode(Integer code) {
            this.code = code;
            return this;
        }

        public String getMsg() {
            return msg;
        }

        public Result setMsg(String msg) {
            this.msg = msg;
            return this;
        }
    }
}
