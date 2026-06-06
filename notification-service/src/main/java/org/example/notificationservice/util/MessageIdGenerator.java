package org.example.notificationservice.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * 消息ID生成工具类
 * 消息ID格式：MSG + yyyyMMdd + 6位随机数
 * 示例：MSG2026051700001
 */
public class MessageIdGenerator {
    
    private static final String PREFIX = "MSG";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
    
    /**
     * 生成消息ID
     * @return 消息ID，格式：MSG + yyyyMMdd + 6位随机数
     */
    public static String generate() {
        String dateStr = LocalDate.now().format(DATE_FORMATTER);
        // 生成6位随机数（000001-999999）
        int randomNum = (int) (Math.random() * 900000) + 100000;
        return PREFIX + dateStr + randomNum;
    }
}
