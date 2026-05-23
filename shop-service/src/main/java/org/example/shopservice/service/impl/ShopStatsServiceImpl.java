package org.example.shopservice.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.shopservice.common.Result;
import org.example.shopservice.dto.ShopStatsResponse;
import org.example.shopservice.entity.ShopInfo;
import org.example.shopservice.entity.TableInfo;
import org.example.shopservice.feign.OrderServiceClient;
import org.example.shopservice.feign.QueueServiceClient;
import org.example.shopservice.service.ShopInfoService;
import org.example.shopservice.service.ShopStatsService;
import org.example.shopservice.service.TableInfoService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShopStatsServiceImpl implements ShopStatsService {

    private final ShopInfoService shopInfoService;
    private final TableInfoService tableInfoService;
    private final OrderServiceClient orderServiceClient;
    private final QueueServiceClient queueServiceClient;

    @Override
    public ShopStatsResponse getShopStats(Long shopId) {
        ShopInfo shop = shopInfoService.getById(shopId);
        if (shop == null) {
            throw new RuntimeException("店铺不存在");
        }

        ShopStatsResponse stats = new ShopStatsResponse();
        stats.setShopId(shopId);
        stats.setShopName(shop.getShopName());
        stats.setShopStatus(shop.getShopStatus());

        try {
            Result<Map<String, Object>> orderStats = orderServiceClient.getTodayStats(shopId);
            if (orderStats != null && orderStats.getData() != null) {
                Map<String, Object> data = orderStats.getData();
                stats.setTodayOrders(((Number) data.getOrDefault("todayOrders", 0)).intValue());
                stats.setTodayRevenue(new BigDecimal(data.getOrDefault("todayRevenue", "0").toString()));
                stats.setTodayCustomers(((Number) data.getOrDefault("todayCustomers", 0)).intValue());
                stats.setOrdersTrend(((Number) data.getOrDefault("ordersTrend", 0.0)).doubleValue());
                stats.setRevenueTrend(((Number) data.getOrDefault("revenueTrend", 0.0)).doubleValue());
            }
        } catch (Exception e) {
            log.error("获取订单统计失败", e);
            stats.setTodayOrders(0);
            stats.setTodayRevenue(BigDecimal.ZERO);
            stats.setTodayCustomers(0);
            stats.setOrdersTrend(0.0);
            stats.setRevenueTrend(0.0);
        }

        try {
            Result<Integer> waitingResult = queueServiceClient.getWaitingCount(shopId);
            if (waitingResult != null && waitingResult.getData() != null) {
                stats.setWaitingQueue(waitingResult.getData());
            }
        } catch (Exception e) {
            log.error("获取等待人数失败", e);
            stats.setWaitingQueue(0);
        }

        try {
            Result<Integer> avgWaitResult = queueServiceClient.getAvgWaitTime(shopId);
            if (avgWaitResult != null && avgWaitResult.getData() != null) {
                stats.setAvgWaitTime(avgWaitResult.getData());
            }
        } catch (Exception e) {
            log.error("获取平均等待时间失败", e);
            stats.setAvgWaitTime(15);
        }

        List<TableInfo> tables = tableInfoService.listByShopId(shopId);
        int totalTables = tables.size();
        int availableTables = (int) tables.stream()
                .filter(t -> t.getTableStatus() == 0)
                .count();

        stats.setTotalTables(totalTables);
        stats.setAvailableTables(availableTables);

        if (totalTables > 0) {
            int occupancyRate = ((totalTables - availableTables) * 100) / totalTables;
            stats.setTableOccupancyRate(occupancyRate);
        } else {
            stats.setTableOccupancyRate(0);
        }

        return stats;
    }

    @Override
    public List<ShopStatsResponse> getAllShopStats() {
        List<ShopInfo> shops = shopInfoService.list();
        return shops.stream()
                .map(shop -> getShopStats(shop.getId()))
                .collect(Collectors.toList());
    }

    @Override
    public Object getRealtimeQueueData(Long shopId) {
        try {
            Result<Object> result = queueServiceClient.getRedisWaitingData(shopId);
            if (result != null && result.getData() != null) {
                return result.getData();
            }
        } catch (Exception e) {
            log.error("获取Redis排队数据失败", e);
        }
        return Collections.emptyMap();
    }
}