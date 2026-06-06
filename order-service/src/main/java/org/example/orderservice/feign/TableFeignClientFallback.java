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

    @Override
    public Result<TableInfoDTO> getTableById(Long id) {
        log.error("调用shop-service失败，无法获取桌台详情，桌台ID: {}", id);
        return Result.error("桌台服务暂时不可用");
    }

    @Override
    public Result<Boolean> updateTableStatus(Long id, TableStatusUpdateRequest request) {
        log.error("调用shop-service失败，无法更新桌台状态，桌台ID: {}, 状态: {}", id, request != null ? request.getTableStatus() : "null");
        return Result.error("桌台服务暂时不可用");
    }
}
