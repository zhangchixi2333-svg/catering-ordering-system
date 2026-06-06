package org.example.shopservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.shopservice.common.Result;
import org.example.shopservice.dto.ShopStatsResponse;
import org.example.shopservice.feign.OrderServiceClient;
import org.example.shopservice.feign.QueueServiceClient;
import org.example.shopservice.service.ShopStatsService;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Tag(name = "店铺统计", description = "店铺统计数据查询")
@RestController
@RequestMapping("/api/shop")
@RequiredArgsConstructor
public class ShopStatsController {

    private final ShopStatsService shopStatsService;
    private final OrderServiceClient orderServiceClient;
    private final QueueServiceClient queueServiceClient;

    @Operation(summary = "获取店铺统计数据")
    @GetMapping("/{shopId}/stats")
    public Result<ShopStatsResponse> getShopStats(
            @Parameter(description = "店铺ID", example = "1", required = true)
            @PathVariable("shopId") Long shopId,
            @Parameter(description = "时间范围", example = "today", schema = @Schema(allowableValues = {"today", "week", "month"}))
            @RequestParam(defaultValue = "today") String timeRange) {
        
        log.info("查询店铺统计数据 - 店铺ID: {}, 时间范围: {}", shopId, timeRange);
        
        try {
            ShopStatsResponse stats = shopStatsService.getShopStats(shopId, timeRange);
            return Result.success(stats);
        } catch (Exception e) {
            log.error("查询店铺统计数据失败", e);
            return Result.error("查询统计数据失败: " + e.getMessage());
        }
    }

    @Operation(summary = "获取所有店铺统计数据")
    @GetMapping("/stats/all")
    public Result<List<Map<String, Object>>> getAllShopsStats() {
        log.info("查询所有店铺统计数据");
        
        try {
            List<Map<String, Object>> allStats = shopStatsService.getAllShopsStats();
            return Result.success(allStats);
        } catch (Exception e) {
            log.error("查询所有店铺统计数据失败", e);
            return Result.error("查询统计数据失败: " + e.getMessage());
        }
    }

    @Operation(summary = "获取店铺实时排队数据")
    @GetMapping("/{shopId}/queue/realtime")
    public Result<Map<String, Object>> getRealtimeQueueData(
            @Parameter(description = "店铺ID", example = "1", required = true)
            @PathVariable("shopId") Long shopId) {
        
        log.info("查询店铺实时排队数据 - 店铺ID: {}", shopId);
        
        try {
            Map<String, Object> data = new HashMap<>();
            
            var waitingCount = queueServiceClient.getWaitingCount(shopId);
            var avgWaitTime = queueServiceClient.getAvgWaitTime(shopId);
            var redisData = queueServiceClient.getRedisWaitingData(shopId);
            
            data.put("waitingCount", waitingCount.getData());
            data.put("avgWaitTime", avgWaitTime.getData());
            data.put("redisData", redisData.getData());
            
            return Result.success(data);
        } catch (Exception e) {
            log.error("查询实时排队数据失败", e);
            return Result.error("查询排队数据失败: " + e.getMessage());
        }
    }

    @Operation(summary = "获取店铺趋势数据")
    @GetMapping("/{shopId}/trends")
    public Result<Map<String, Object>> getShopTrends(
            @Parameter(description = "店铺ID", example = "1", required = true)
            @PathVariable("shopId") Long shopId,
            @Parameter(description = "天数", example = "7")
            @RequestParam(defaultValue = "7") Integer days) {
        
        log.info("查询店铺趋势数据 - 店铺ID: {}, 天数: {}", shopId, days);
        
        try {
            Map<String, Object> trends = shopStatsService.getShopTrends(shopId, days);
            return Result.success(trends);
        } catch (Exception e) {
            log.error("查询趋势数据失败", e);
            return Result.error("查询趋势数据失败: " + e.getMessage());
        }
    }
}