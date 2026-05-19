package org.example.orderservice.feign;

import org.example.orderservice.common.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

/**
 * Queue服务Feign客户端
 * 用于调用queue-service的排队相关接口
 */
@FeignClient(name = "queue-service", fallback = QueueFeignClientFallback.class)
public interface QueueFeignClient {

    /**
     * 获取店铺等待中的排队列表
     * @param shopId 店铺ID
     * @return 排队列表
     */
    @GetMapping("/api/queue/shop/{shopId}/waiting")
    Result<List<QueueInfoDTO>> getWaitingQueues(@PathVariable("shopId") Long shopId);

    /**
     * 根据ID获取排队信息
     * @param id 排队ID
     * @return 排队信息
     */
    @GetMapping("/api/queue/{id}")
    Result<QueueInfoDTO> getQueueById(@PathVariable("id") Long id);

    /**
     * 根据排队号码获取排队信息
     * @param queueNo 排队号码（如：A001、B002）
     * @return 排队信息
     */
    @GetMapping("/api/queue/no/{queueNo}")
    Result<QueueInfoDTO> getQueueByNo(@PathVariable("queueNo") String queueNo);

    /**
     * 排队信息DTO
     */
    class QueueInfoDTO {
        private Long id;
        private String queueNo;
        private Long shopId;
        private Integer queueStatus; // 0-等待中，1-已叫号，2-已完成，3-已取消

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getQueueNo() {
            return queueNo;
        }

        public void setQueueNo(String queueNo) {
            this.queueNo = queueNo;
        }

        public Long getShopId() {
            return shopId;
        }

        public void setShopId(Long shopId) {
            this.shopId = shopId;
        }

        public Integer getQueueStatus() {
            return queueStatus;
        }

        public void setQueueStatus(Integer queueStatus) {
            this.queueStatus = queueStatus;
        }
    }
}
