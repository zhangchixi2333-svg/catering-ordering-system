package org.example.queueservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.queueservice.common.Result;
import org.example.queueservice.service.RedisQueueService;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Slf4j
@Tag(name = "排队统计", description = "排队统计数据查询")
@RestController
@RequestMapping("/api/queue")
@RequiredArgsConstructor
public class QueueStatsController {

    private final RedisQueueService redisQueueService;

    @Operation(summary = "获取店铺等待中排队数量")
    @GetMapping("/shop/{shopId}/waiting/count")
    public Result<Integer> getWaitingCount(
            @Parameter(description = "店铺ID", example = "1", required = true)
            @PathVariable("shopId") Long shopId) {
        
        log.info("查询店铺等待中排队数量 - 店铺ID: {}", shopId);
        
        Set<String> waitingQueue = redisQueueService.getWaitingQueue(shopId);
        int count = waitingQueue != null ? waitingQueue.size() : 0;
        
        log.info("等待中排队数量: {}", count);
        
        return Result.success(count);
    }

    @Operation(summary = "获取店铺平均等待时间")
    @GetMapping("/shop/{shopId}/avg/wait/time")
    public Result<Integer> getAvgWaitTime(
            @Parameter(description = "店铺ID", example = "1", required = true)
            @PathVariable("shopId") Long shopId) {
        
        log.info("查询店铺平均等待时间 - 店铺ID: {}", shopId);
        
        Set<String> waitingQueue = redisQueueService.getWaitingQueue(shopId);
        int avgWaitTime = 15; // 默认15分钟
        
        if (waitingQueue != null && !waitingQueue.isEmpty()) {
            int queueSize = waitingQueue.size();
            if (queueSize <= 5) {
                avgWaitTime = 10;
            } else if (queueSize <= 10) {
                avgWaitTime = 15;
            } else if (queueSize <= 20) {
                avgWaitTime = 20;
            } else {
                avgWaitTime = 30;
            }
        }
        
        log.info("平均等待时间: {}分钟", avgWaitTime);
        
        return Result.success(avgWaitTime);
    }

    @Operation(summary = "获取Redis实时排队数据（包含等待和叫号队列）")
    @GetMapping("/stats/redis/{shopId}")
    public Result<Map<String, Object>> getRedisWaitingData(
            @Parameter(description = "店铺ID", example = "1", required = true)
            @PathVariable("shopId") Long shopId) {
        
        log.info("查询Redis实时排队数据 - 店铺ID: {}", shopId);
        
        Map<String, Object> data = new HashMap<>();
        
        Set<String> waitingQueue = redisQueueService.getWaitingQueue(shopId);
        Set<String> callingQueue = redisQueueService.getCallingQueue(shopId);
        
        data.put("waitingQueue", waitingQueue);
        data.put("callingQueue", callingQueue);
        data.put("waitingCount", waitingQueue != null ? waitingQueue.size() : 0);
        data.put("callingCount", callingQueue != null ? callingQueue.size() : 0);
        
        log.info("Redis排队数据 - 等待中: {}, 已叫号: {}", data.get("waitingCount"), data.get("callingCount"));
        
        return Result.success(data);
    }
}