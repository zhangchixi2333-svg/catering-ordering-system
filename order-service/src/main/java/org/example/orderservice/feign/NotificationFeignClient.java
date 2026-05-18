package org.example.orderservice.feign;

import org.example.orderservice.common.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 通知服务Feign客户端
 * 用于order-service调用notification-service发送订单相关通知
 */
@FeignClient(name = "notification-service", path = "/api/notification/ws", fallback = NotificationFeignClientFallback.class)
public interface NotificationFeignClient {

    /**
     * 推送订单相关通知
     * @param request 通知请求
     * @return 推送结果
     */
    @PostMapping("/push/order")
    Result<Boolean> pushOrderNotification(@RequestBody OrderNotificationRequest request);

    /**
     * 订单通知请求DTO
     */
    class OrderNotificationRequest {
        private Long userId;              // 用户ID
        private String notificationType;  // 通知类型：ORDER_CREATED-订单创建, ORDER_CANCELLED-订单取消, ORDER_COMPLETED-订单完成
        private Object data;              // 通知数据（订单信息）

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
