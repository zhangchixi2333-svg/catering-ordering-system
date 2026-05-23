package org.example.shopservice.feign;

import org.example.shopservice.common.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "queue-service", url = "http://localhost:8085")
public interface QueueServiceClient {
    
    @GetMapping("/api/queue/shop/{shopId}/waiting/count")
    Result<Integer> getWaitingCount(@PathVariable("shopId") Long shopId);
    
    @GetMapping("/api/queue/shop/{shopId}/avg/wait/time")
    Result<Integer> getAvgWaitTime(@PathVariable("shopId") Long shopId);
    
    @GetMapping("/api/queue/stats/redis/{shopId}")
    Result<Object> getRedisWaitingData(@PathVariable("shopId") Long shopId);
}