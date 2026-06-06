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
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
        return getShopStats(shopId, "today");
    }

    @Override
    public ShopStatsResponse getShopStats(Long shopId, String timeRange) {
        ShopInfo shop = shopInfoService.getById(shopId);
        if (shop == null) {
            throw new RuntimeException("店铺不存在");
        }

        ShopStatsResponse stats = new ShopStatsResponse();
        
        try {
            Result<Map<String, Object>> orderStats = orderServiceClient.getTodayStats(shopId);
            if (orderStats != null && orderStats.getData() != null) {
                Map<String, Object> data = orderStats.getData();
                stats.setOrderCount(((Number) data.getOrDefault("todayOrders", 0)).longValue());
                stats.setRevenue(new BigDecimal(data.getOrDefault("todayRevenue", "0").toString()));
                stats.setCustomerCount(((Number) data.getOrDefault("todayCustomers", 0)).longValue());
                stats.setOrderTrend(((Number) data.getOrDefault("ordersTrend", 0.0)).doubleValue());
                stats.setRevenueTrend(((Number) data.getOrDefault("revenueTrend", 0.0)).doubleValue());
                stats.setCustomerTrend(((Number) data.getOrDefault("customersTrend", 0.0)).doubleValue());
                
                if (stats.getCustomerCount() != null && stats.getCustomerCount() > 0) {
                    stats.setAvgOrderValue(stats.getRevenue().divide(
                        new BigDecimal(stats.getCustomerCount()), 2, RoundingMode.HALF_UP));
                }
                
                stats.setAvgTrend(stats.getRevenueTrend());
            }
        } catch (Exception e) {
            log.error("获取订单统计失败", e);
            stats.setOrderCount(0L);
            stats.setRevenue(BigDecimal.ZERO);
            stats.setCustomerCount(0L);
            stats.setAvgOrderValue(BigDecimal.ZERO);
            stats.setOrderTrend(0.0);
            stats.setRevenueTrend(0.0);
            stats.setCustomerTrend(0.0);
            stats.setAvgTrend(0.0);
        }

        try {
            Result<Integer> waitingCount = queueServiceClient.getWaitingCount(shopId);
            Result<Integer> avgWaitTime = queueServiceClient.getAvgWaitTime(shopId);
            
            if (waitingCount != null && waitingCount.getData() != null) {
                stats.setTotalQueueCount(waitingCount.getData().longValue());
            }
            if (avgWaitTime != null && avgWaitTime.getData() != null) {
                stats.setAvgWaitTime(avgWaitTime.getData());
            }
        } catch (Exception e) {
            log.error("获取排队统计失败", e);
            stats.setTotalQueueCount(0L);
            stats.setAvgWaitTime(0);
        }

        List<TableInfo> tables = tableInfoService.listByShopId(shopId);
        int totalTables = tables != null ? tables.size() : 0;
        int availableTables = (int) tables.stream().filter(t -> t.getTableStatus() == 1).count();
        
        stats.setDailyStats(generateDailyStats(shopId, timeRange));
        stats.setQueueStats(generateQueueStats(shopId, timeRange));
        stats.setPaymentStats(generatePaymentStats(shopId, timeRange));

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
    public List<Map<String, Object>> getAllShopsStats() {
        List<ShopInfo> shops = shopInfoService.list();
        List<Map<String, Object>> result = new ArrayList<>();
        
        for (ShopInfo shop : shops) {
            try {
                ShopStatsResponse stats = getShopStats(shop.getId());
                Map<String, Object> shopData = new HashMap<>();
                shopData.put("shopId", shop.getId());
                shopData.put("shopName", shop.getShopName());
                shopData.put("shopCode", shop.getShopCode());
                shopData.put("shopStatus", shop.getShopStatus());
                shopData.put("orderCount", stats.getOrderCount());
                shopData.put("revenue", stats.getRevenue());
                shopData.put("customerCount", stats.getCustomerCount());
                shopData.put("avgOrderValue", stats.getAvgOrderValue());
                shopData.put("totalQueueCount", stats.getTotalQueueCount());
                shopData.put("avgWaitTime", stats.getAvgWaitTime());
                result.add(shopData);
            } catch (Exception e) {
                log.error("获取店铺 {} 统计数据失败", shop.getId(), e);
            }
        }
        
        return result;
    }

    @Override
    public Object getRealtimeQueueData(Long shopId) {
        Map<String, Object> data = new HashMap<>();
        
        try {
            Result<Integer> waitingCount = queueServiceClient.getWaitingCount(shopId);
            Result<Integer> avgWaitTime = queueServiceClient.getAvgWaitTime(shopId);
            Result<Object> redisData = queueServiceClient.getRedisWaitingData(shopId);
            
            data.put("waitingCount", waitingCount != null ? waitingCount.getData() : 0);
            data.put("avgWaitTime", avgWaitTime != null ? avgWaitTime.getData() : 0);
            data.put("redisData", redisData != null ? redisData.getData() : null);
        } catch (Exception e) {
            log.error("获取实时排队数据失败", e);
            data.put("waitingCount", 0);
            data.put("avgWaitTime", 0);
            data.put("redisData", null);
        }
        
        return data;
    }

    @Override
    public Map<String, Object> getShopTrends(Long shopId, Integer days) {
        Map<String, Object> trends = new HashMap<>();
        
        List<ShopStatsResponse.DailyStat> dailyStats = generateDailyStats(shopId, "custom", days);
        trends.put("dailyStats", dailyStats);
        
        List<String> dates = dailyStats.stream()
                .map(ShopStatsResponse.DailyStat::getDate)
                .collect(Collectors.toList());
        
        List<Long> orderCounts = dailyStats.stream()
                .map(ShopStatsResponse.DailyStat::getOrderCount)
                .collect(Collectors.toList());
        
        List<BigDecimal> revenues = dailyStats.stream()
                .map(ShopStatsResponse.DailyStat::getRevenue)
                .collect(Collectors.toList());
        
        trends.put("dates", dates);
        trends.put("orderCounts", orderCounts);
        trends.put("revenues", revenues);
        
        return trends;
    }

    private List<ShopStatsResponse.DailyStat> generateDailyStats(Long shopId, String timeRange) {
        return generateDailyStats(shopId, timeRange, 7);
    }

    private List<ShopStatsResponse.DailyStat> generateDailyStats(Long shopId, String timeRange, Integer days) {
        List<ShopStatsResponse.DailyStat> dailyStats = new ArrayList<>();
        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd");
        
        int actualDays = days;
        if ("today".equals(timeRange)) {
            actualDays = 1;
        } else if ("week".equals(timeRange)) {
            actualDays = 7;
        } else if ("month".equals(timeRange)) {
            actualDays = 30;
        }
        
        for (int i = actualDays - 1; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            ShopStatsResponse.DailyStat stat = new ShopStatsResponse.DailyStat();
            stat.setDate(date.format(formatter));
            
            Random random = new Random();
            stat.setOrderCount((long) (random.nextInt(50) + 10));
            stat.setRevenue(new BigDecimal(random.nextInt(5000) + 1000));
            stat.setCustomerCount((long) (random.nextInt(30) + 5));
            
            if (stat.getCustomerCount() > 0) {
                stat.setAvgOrderValue(stat.getRevenue().divide(
                    new BigDecimal(stat.getCustomerCount()), 2, RoundingMode.HALF_UP));
            } else {
                stat.setAvgOrderValue(BigDecimal.ZERO);
            }
            
            stat.setTrend(random.nextDouble() * 40 - 20);
            
            dailyStats.add(stat);
        }
        
        return dailyStats;
    }

    private List<ShopStatsResponse.QueueStat> generateQueueStats(Long shopId, String timeRange) {
        List<ShopStatsResponse.QueueStat> queueStats = new ArrayList<>();
        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd");
        
        int days = 7;
        if ("today".equals(timeRange)) {
            days = 1;
        } else if ("week".equals(timeRange)) {
            days = 7;
        } else if ("month".equals(timeRange)) {
            days = 30;
        }
        
        for (int i = days - 1; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            ShopStatsResponse.QueueStat stat = new ShopStatsResponse.QueueStat();
            stat.setDate(date.format(formatter));
            
            Random random = new Random();
            stat.setQueueCount((long) (random.nextInt(30) + 5));
            stat.setAvgWaitTime(random.nextInt(20) + 5);
            stat.setCompletedCount((long) (random.nextInt(25) + 3));
            stat.setCancelledCount((long) (random.nextInt(5)));
            
            if (stat.getQueueCount() > 0) {
                stat.setCompletionRate(stat.getCompletedCount() * 100.0 / stat.getQueueCount());
            } else {
                stat.setCompletionRate(0.0);
            }
            
            queueStats.add(stat);
        }
        
        return queueStats;
    }

    private List<ShopStatsResponse.PaymentStat> generatePaymentStats(Long shopId, String timeRange) {
        List<ShopStatsResponse.PaymentStat> paymentStats = new ArrayList<>();
        
        String[] paymentMethods = {"微信支付", "支付宝", "银行卡", "现金"};
        
        for (String method : paymentMethods) {
            ShopStatsResponse.PaymentStat stat = new ShopStatsResponse.PaymentStat();
            stat.setPaymentMethod(method);
            
            Random random = new Random();
            stat.setCount((long) (random.nextInt(40) + 10));
            stat.setAmount(new BigDecimal(random.nextInt(4000) + 1000));
            stat.setSuccessRate(95.0 + random.nextDouble() * 5);
            stat.setAvgProcessTime(random.nextInt(5) + 2);
            
            paymentStats.add(stat);
        }
        
        return paymentStats;
    }
}