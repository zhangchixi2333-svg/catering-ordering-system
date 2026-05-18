package org.example.notificationservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.notificationservice.common.Result;
import org.example.notificationservice.websocket.NotificationWebSocket;
import org.springframework.web.bind.annotation.*;

/**
 * WebSocket通知控制器
 * 用于其他服务调用，触发WebSocket推送
 */
@Slf4j
@Tag(name = "WebSocket通知", description = "WebSocket实时推送接口")
@RestController
@RequestMapping("/api/notification/ws")
@RequiredArgsConstructor
public class WebSocketNotificationController {

    @Operation(
        summary = "推送排队相关通知",
        description = "<font color='red'>【内部接口】</font><br/>" +
                "供queue-service调用，向指定用户推送排队相关的WebSocket通知<br/><br/>" +
                "<font color='green'>支持的通知类型：</font><br/>" +
                "- QUEUE_CREATED: 取号成功通知<br/>" +
                "- QUEUE_CALLED: 叫号通知<br/>" +
                "- QUEUE_COMPLETED: 排队完成通知<br/>" +
                "- QUEUE_CANCELLED: 排队取消通知"
    )
    @PostMapping("/push/queue")
    public Result<Boolean> pushQueueNotification(@RequestBody QueueNotificationRequest request) {
        log.info("收到推送请求 - 用户ID: {}, 通知类型: {}", request.getUserId(), request.getNotificationType());
        
        // 检查用户是否在线
        if (!NotificationWebSocket.isUserOnline(request.getUserId())) {
            log.warn("用户不在线，无法推送通知 - 用户ID: {}, 当前在线用户数: {}", 
                    request.getUserId(), NotificationWebSocket.getOnlineCount());
            return Result.success(false, "用户不在线，无法推送通知");
        }
        
        log.info("开始推送WebSocket通知 - 用户ID: {}, 通知类型: {}", request.getUserId(), request.getNotificationType());
        
        // 推送通知
        NotificationWebSocket.pushQueueNotification(
            request.getUserId(), 
            request.getNotificationType(), 
            request.getData()
        );
        
        log.info("✅ WebSocket通知推送成功 - 用户ID: {}", request.getUserId());
        return Result.success(true, "通知推送成功");
    }

    @Operation(
        summary = "推送订单相关通知",
        description = "<font color='red'>【内部接口】</font><br/>" +
                "供order-service调用，向指定用户推送订单相关的WebSocket通知<br/><br/>" +
                "<font color='green'>支持的通知类型：</font><br/>" +
                "- ORDER_CREATED: 订单创建成功通知<br/>" +
                "- ORDER_CANCELLED: 订单取消通知<br/>" +
                "- ORDER_COMPLETED: 订单完成通知<br/>" +
                "- ORDER_PAID: 订单支付成功通知"
    )
    @PostMapping("/push/order")
    public Result<Boolean> pushOrderNotification(@RequestBody OrderNotificationRequest request) {
        log.info("收到订单推送请求 - 用户ID: {}, 通知类型: {}", request.getUserId(), request.getNotificationType());
        
        // 检查用户是否在线
        if (!NotificationWebSocket.isUserOnline(request.getUserId())) {
            log.warn("用户不在线，无法推送订单通知 - 用户ID: {}, 当前在线用户数: {}", 
                    request.getUserId(), NotificationWebSocket.getOnlineCount());
            return Result.success(false, "用户不在线，无法推送通知");
        }
        
        log.info("开始推送订单WebSocket通知 - 用户ID: {}, 通知类型: {}", request.getUserId(), request.getNotificationType());
        
        // 推送通知
        NotificationWebSocket.pushQueueNotification(
            request.getUserId(), 
            request.getNotificationType(), 
            request.getData()
        );
        
        log.info("✅ 订单WebSocket通知推送成功 - 用户ID: {}", request.getUserId());
        return Result.success(true, "通知推送成功");
    }

    @Operation(
        summary = "获取在线用户数",
        description = "查看当前有多少用户通过WebSocket连接"
    )
    @GetMapping("/online/count")
    public Result<Integer> getOnlineCount() {
        return Result.success(NotificationWebSocket.getOnlineCount());
    }

    /**
     * 排队通知请求DTO
     */
    static class QueueNotificationRequest {
        private Long userId;              // 用户ID
        private String notificationType;  // 通知类型
        private Object data;              // 通知数据

        public Long getUserId() {
            return userId;
        }

        public void setUserId(Long userId) {
            this.userId = userId;
        }

        public String getNotificationType() {
            return notificationType;
        }

        public void setNotificationType(String notificationType) {
            this.notificationType = notificationType;
        }

        public Object getData() {
            return data;
        }

        public void setData(Object data) {
            this.data = data;
        }
    }

    /**
     * 订单通知请求DTO
     */
    static class OrderNotificationRequest {
        private Long userId;              // 用户ID
        private String notificationType;  // 通知类型
        private Object data;              // 通知数据

        public Long getUserId() {
            return userId;
        }

        public void setUserId(Long userId) {
            this.userId = userId;
        }

        public String getNotificationType() {
            return notificationType;
        }

        public void setNotificationType(String notificationType) {
            this.notificationType = notificationType;
        }

        public Object getData() {
            return data;
        }

        public void setData(Object data) {
            this.data = data;
        }
    }
}
