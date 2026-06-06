package org.example.shopservice.feign;

import org.example.shopservice.common.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

@FeignClient(name = "order-service", url = "http://localhost:8083")
public interface OrderServiceClient {
    
    @GetMapping("/api/order/shop/{shopId}/stats/today")
    Result<Map<String, Object>> getTodayStats(@PathVariable("shopId") Long shopId);
    
    @GetMapping("/api/order/shop/{shopId}/recent")
    Result<Object> getRecentOrders(@PathVariable("shopId") Long shopId);
}