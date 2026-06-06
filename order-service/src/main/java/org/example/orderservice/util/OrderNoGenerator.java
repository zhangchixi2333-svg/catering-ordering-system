package org.example.orderservice.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * 订单号生成工具类
 * 订单号格式：ORD + yyyyMMdd + 6位随机数
 * 示例：ORD2026051700001
 */
public class OrderNoGenerator {
    
    private static final String PREFIX = "ORD";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
    
    /**
     * 生成订单号
     * @return 订单号，格式：ORD + yyyyMMdd + 6位随机数
     */
    public static String generate() {
        String dateStr = LocalDate.now().format(DATE_FORMATTER);
        // 生成6位随机数（000001-999999）
        int randomNum = (int) (Math.random() * 900000) + 100000;
        return PREFIX + dateStr + randomNum;
    }
}
