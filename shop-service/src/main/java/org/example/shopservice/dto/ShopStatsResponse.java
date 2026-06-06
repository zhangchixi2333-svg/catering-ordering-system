package org.example.shopservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShopStatsResponse {
    
    private Long orderCount;
    private BigDecimal revenue;
    private Long customerCount;
    private BigDecimal avgOrderValue;
    
    private Double orderTrend;
    private Double revenueTrend;
    private Double customerTrend;
    private Double avgTrend;
    
    private Long totalQueueCount;
    private Integer avgWaitTime;
    
    private Long paymentSuccessCount;
    private Long paymentFailedCount;
    
    private List<DailyStat> dailyStats;
    private List<QueueStat> queueStats;
    private List<PaymentStat> paymentStats;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DailyStat {
        private String date;
        private Long orderCount;
        private BigDecimal revenue;
        private Long customerCount;
        private BigDecimal avgOrderValue;
        private Double trend;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QueueStat {
        private String date;
        private Long queueCount;
        private Integer avgWaitTime;
        private Long completedCount;
        private Long cancelledCount;
        private Double completionRate;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PaymentStat {
        private String paymentMethod;
        private Long count;
        private BigDecimal amount;
        private Double successRate;
        private Integer avgProcessTime;
    }
}