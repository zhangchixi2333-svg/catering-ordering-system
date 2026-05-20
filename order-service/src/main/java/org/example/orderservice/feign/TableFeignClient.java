package org.example.orderservice.feign;

import lombok.Data;
import org.example.orderservice.common.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * 桌台服务Feign客户端
 * 用于order-service调用shop-service获取桌台信息
 */
@FeignClient(name = "shop-service", path = "/api/table", fallback = TableFeignClientFallback.class)
public interface TableFeignClient {

    /**
     * 获取店铺可用桌台列表
     * @param shopId 店铺ID
     * @return 可用桌台列表
     */
    @GetMapping("/shop/{shopId}/available")
    Result<List<TableInfoDTO>> getAvailableTables(@PathVariable("shopId") Long shopId);

    /**
     * 根据ID获取桌台详情
     * @param id 桌台ID
     * @return 桌台详情
     */
    @GetMapping("/{id}")
    Result<TableInfoDTO> getTableById(@PathVariable("id") Long id);

    /**
     * 更新桌台状态
     * @param id 桌台ID
     * @param request 桌台状态更新请求（包含tableStatus字段：0-空闲，1-已占用，2-预订，3-清洁中）
     * @return 更新结果
     */
    @PutMapping("/{id}/status")
    Result<Boolean> updateTableStatus(@PathVariable("id") Long id, @RequestBody TableStatusUpdateRequest request);

    /**
     * 桌台信息DTO
     */
    @Data
    class TableInfoDTO {
        private Long id;              // 桌台ID
        private String tableNumber;   // 桌台编号，如：A01、B02
        private Integer capacity;     // 容纳人数
        private Integer tableStatus;  // 0-空闲，1-占用，2-预订，3-清洁中
        private String tableType;     // 桌台类型：大厅、包间、露台等
    }

    /**
     * 桌台状态更新请求DTO
     */
    @Data
    class TableStatusUpdateRequest {
        private Integer tableStatus;  // 桌台状态：0-空闲，1-已占用，2-预订，3-清洁中
    }
}
