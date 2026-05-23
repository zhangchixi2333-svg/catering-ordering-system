package org.example.shopservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.shopservice.common.Result;
import org.example.shopservice.dto.ShopStatsResponse;
import org.example.shopservice.service.ShopStatsService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "店铺统计", description = "店铺统计数据查询")
@RestController
@RequestMapping("/api/shop")
@RequiredArgsConstructor
public class ShopStatsController {

    private final ShopStatsService shopStatsService;

    @Operation(summary = "获取店铺统计数据")
    @GetMapping("/{shopId}/stats")
    public Result<ShopStatsResponse> getShopStats(
            @Parameter(description = "店铺ID", example = "1", required = true)
            @PathVariable("shopId") Long shopId) {
        ShopStatsResponse stats = shopStatsService.getShopStats(shopId);
        return Result.success(stats);
    }

    @Operation(summary = "获取所有店铺统计数据")
    @GetMapping("/stats/all")
    public Result<List<ShopStatsResponse>> getAllShopStats() {
        List<ShopStatsResponse> statsList = shopStatsService.getAllShopStats();
        return Result.success(statsList);
    }

    @Operation(summary = "获取店铺实时排队数据")
    @GetMapping("/{shopId}/queue/realtime")
    public Result<Object> getRealtimeQueueData(
            @Parameter(description = "店铺ID", example = "1", required = true)
            @PathVariable("shopId") Long shopId) {
        Object queueData = shopStatsService.getRealtimeQueueData(shopId);
        return Result.success(queueData);
    }
}