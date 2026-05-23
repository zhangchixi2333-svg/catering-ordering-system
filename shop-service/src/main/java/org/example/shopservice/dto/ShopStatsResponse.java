package org.example.shopservice.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ShopStatsResponse {
    private Long shopId;
    private String shopName;
    
    private Integer todayOrders;
    private BigDecimal todayRevenue;
    private Integer waitingQueue;
    private Integer availableTables;
    private Integer totalTables;
    private Integer todayCustomers;
    
    private Double ordersTrend;
    private Double revenueTrend;
    
    private Integer tableOccupancyRate;
    private Integer avgWaitTime;
    
    private Integer shopStatus;
}