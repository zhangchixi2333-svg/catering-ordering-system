package org.example.queueservice.feign;

import org.example.queueservice.common.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 通知服务Feign客户端
 * 用于queue-service调用notification-service推送WebSocket通知
 */
@FeignClient(name = "notification-service", path = "/api/notification/ws", fallback = NotificationFeignClientFallback.class)
public interface NotificationFeignClient {

    /**
     * 推送排队相关通知
     * @param request 通知请求
     * @return 推送结果
     */
    @PostMapping("/push/queue")
    Result<Boolean> pushQueueNotification(@RequestBody QueueNotificationRequest request);

    /**
     * 排队通知请求DTO
     */
    class QueueNotificationRequest {
        private Long userId;          // 用户ID
        private String notificationType; // 通知类型：QUEUE_CREATED, QUEUE_CALLED, QUEUE_COMPLETED, QUEUE_CANCELLED
        private Object data;          // 通知数据（排队信息）

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
