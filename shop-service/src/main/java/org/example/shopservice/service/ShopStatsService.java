package org.example.shopservice.service;

import org.example.shopservice.dto.ShopStatsResponse;

import java.util.List;

public interface ShopStatsService {
    
    ShopStatsResponse getShopStats(Long shopId);
    
    List<ShopStatsResponse> getAllShopStats();
    
    Object getRealtimeQueueData(Long shopId);
}