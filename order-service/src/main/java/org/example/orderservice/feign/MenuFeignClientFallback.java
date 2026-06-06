package org.example.orderservice.feign;

import lombok.extern.slf4j.Slf4j;
import org.example.orderservice.common.Result;
import org.springframework.stereotype.Component;

/**
 * MenuFeignClient熔断降级处理类
 */
@Slf4j
@Component
public class MenuFeignClientFallback implements MenuFeignClient {

    @Override
    public Result<MenuItemInfoDTO> getMenuItemById(Long id) {
        log.error("调用menu-service失败，菜品ID: {}", id);
        return Result.error("菜单服务暂时不可用，请稍后重试");
    }

    @Override
    public Result<Boolean> updateStock(Long id, Integer stock) {
        log.error("调用menu-service失败，菜品ID: {}, 库存: {}", id, stock);
        return Result.error("菜单服务暂时不可用");
    }
}
