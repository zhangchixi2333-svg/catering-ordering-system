package org.example.orderservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.orderservice.common.Result;
import org.example.orderservice.entity.Orders;
import org.example.orderservice.service.OrdersService;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Tag(name = "订单统计", description = "订单统计数据查询")
@RestController
@RequestMapping("/api/order")
@RequiredArgsConstructor
public class OrderStatsController {

    private final OrdersService ordersService;

    @Operation(summary = "获取店铺今日统计数据")
    @GetMapping("/shop/{shopId}/stats/today")
    public Result<Map<String, Object>> getTodayStats(
            @Parameter(description = "店铺ID", example = "1", required = true)
            @PathVariable("shopId") Long shopId) {
        
        log.info("查询店铺今日统计数据 - 店铺ID: {}", shopId);
        
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().atTime(LocalTime.MAX);
        
        List<Orders> todayOrders = ordersService.lambdaQuery()
                .eq(Orders::getShopId, shopId)
                .between(Orders::getCreatedAt, startOfDay, endOfDay)
                .list();
        
        int todayOrdersCount = todayOrders.size();
        BigDecimal todayRevenue = todayOrders.stream()
                .filter(order -> order.getPaymentStatus() != null && order.getPaymentStatus() == 1)
                .map(Orders::getActualAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        int todayCustomers = todayOrders.stream()
                .mapToInt(order -> {
                    if (order.getItemCount() != null) {
                        return order.getItemCount();
                    }
                    return 0;
                })
                .sum();
        
        LocalDateTime yesterdayStart = LocalDate.now().minusDays(1).atStartOfDay();
        LocalDateTime yesterdayEnd = LocalDate.now().minusDays(1).atTime(LocalTime.MAX);
        
        List<Orders> yesterdayOrders = ordersService.lambdaQuery()
                .eq(Orders::getShopId, shopId)
                .between(Orders::getCreatedAt, yesterdayStart, yesterdayEnd)
                .list();
        
        int yesterdayOrdersCount = yesterdayOrders.size();
        BigDecimal yesterdayRevenue = yesterdayOrders.stream()
                .filter(order -> order.getPaymentStatus() != null && order.getPaymentStatus() == 1)
                .map(Orders::getActualAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        double ordersTrend = 0.0;
        double revenueTrend = 0.0;
        
        if (yesterdayOrdersCount > 0) {
            ordersTrend = ((todayOrdersCount - yesterdayOrdersCount) * 100.0) / yesterdayOrdersCount;
        }
        
        if (yesterdayRevenue.compareTo(BigDecimal.ZERO) > 0) {
            revenueTrend = (todayRevenue.subtract(yesterdayRevenue))
                    .multiply(new BigDecimal("100"))
                    .divide(yesterdayRevenue, 2, BigDecimal.ROUND_HALF_UP)
                    .doubleValue();
        }
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("todayOrders", todayOrdersCount);
        stats.put("todayRevenue", todayRevenue);
        stats.put("todayCustomers", todayCustomers);
        stats.put("ordersTrend", ordersTrend);
        stats.put("revenueTrend", revenueTrend);
        
        log.info("今日统计数据 - 订单数: {}, 营收: {}, 客户数: {}, 订单趋势: {}%, 营收趋势: {}%", 
                todayOrdersCount, todayRevenue, todayCustomers, ordersTrend, revenueTrend);
        
        return Result.success(stats);
    }

    @Operation(summary = "获取店铺最近订单")
    @GetMapping("/shop/{shopId}/recent")
    public Result<List<Orders>> getRecentOrders(
            @Parameter(description = "店铺ID", example = "1", required = true)
            @PathVariable("shopId") Long shopId) {
        
        log.info("查询店铺最近订单 - 店铺ID: {}", shopId);
        
        List<Orders> recentOrders = ordersService.lambdaQuery()
                .eq(Orders::getShopId, shopId)
                .orderByDesc(Orders::getCreatedAt)
                .last("LIMIT 10")
                .list();
        
        log.info("查询到最近订单数量: {}", recentOrders.size());
        
        return Result.success(recentOrders);
    }
}