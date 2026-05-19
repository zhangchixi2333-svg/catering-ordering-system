package org.example.orderservice.feign;

import lombok.extern.slf4j.Slf4j;
import org.example.orderservice.common.Result;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * TableFeignClient熔断降级处理类
 * 当shop-service不可用时，提供友好的降级响应
 */
@Slf4j
@Component
public class TableFeignClientFallback implements TableFeignClient {

    @Override
    public Result<List<TableInfoDTO>> getAvailableTables(Long shopId) {
        log.error("调用shop-service失败，无法获取可用桌子，店铺ID: {}", shopId);
        return Result.success(Collections.emptyList());
    }
}
