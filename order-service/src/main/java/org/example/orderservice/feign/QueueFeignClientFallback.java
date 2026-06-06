package org.example.orderservice.feign;

import lombok.extern.slf4j.Slf4j;
import org.example.orderservice.common.Result;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * QueueFeignClient熔断降级处理类
 */
@Slf4j
@Component
public class QueueFeignClientFallback implements QueueFeignClient {

    @Override
    public Result<List<QueueInfoDTO>> getWaitingQueues(Long shopId) {
        log.error("调用queue-service失败，店铺ID: {}", shopId);
        return Result.success(Collections.emptyList());
    }

    @Override
    public Result<QueueInfoDTO> getQueueById(Long id) {
        log.error("调用queue-service失败，排队ID: {}", id);
        return Result.error("排队服务暂时不可用");
    }

    @Override
    public Result<QueueInfoDTO> getQueueByNo(String queueNo) {
        log.error("调用queue-service失败，排队号码: {}", queueNo);
        return Result.error("排队服务暂时不可用");
    }

    @Override
    public Result<Boolean> updateQueueStatus(Long id, Integer queueStatus) {
        log.error("调用queue-service失败，无法更新排队状态，排队ID: {}, 状态: {}", id, queueStatus);
        return Result.error("排队服务暂时不可用");
    }
}
