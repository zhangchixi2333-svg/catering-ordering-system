package org.example.shopservice.service;

import org.example.shopservice.dto.ShopStatsResponse;

import java.util.List;
import java.util.Map;

public interface ShopStatsService {
    
    ShopStatsResponse getShopStats(Long shopId);
    
    ShopStatsResponse getShopStats(Long shopId, String timeRange);
    
    List<ShopStatsResponse> getAllShopStats();
    
    List<Map<String, Object>> getAllShopsStats();
    
    Object getRealtimeQueueData(Long shopId);
    
    Map<String, Object> getShopTrends(Long shopId, Integer days);
}