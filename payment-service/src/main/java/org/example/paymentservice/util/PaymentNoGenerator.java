package org.example.paymentservice.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * 支付单号生成工具类
 * 支付单号格式：PAY + yyyyMMdd + 6位随机数
 * 示例：PAY2026051700001
 */
public class PaymentNoGenerator {
    
    private static final String PREFIX = "PAY";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
    
    /**
     * 生成支付单号
     * @return 支付单号，格式：PAY + yyyyMMdd + 6位随机数
     */
    public static String generate() {
        String dateStr = LocalDate.now().format(DATE_FORMATTER);
        // 生成6位随机数（000001-999999）
        int randomNum = (int) (Math.random() * 900000) + 100000;
        return PREFIX + dateStr + randomNum;
    }
}
