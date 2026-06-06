package org.example.queueservice.util;

/**
 * 排队号码生成工具类
 * 排队号码格式：前缀 + 年月日时分秒 + 3位序列号
 * 示例：A20260522143045001, B20260522143046002, V20260522143047001
 * 
 * 队列前缀说明：
 * - A: 普通桌（堂食）
 * - B: 卡座
 * - V: 包厢
 * - T: 外带
 */
public class QueueNoGenerator {
    
    /**
     * 生成排队号码（包含年月日时分秒）
     * @param prefix 队列前缀（A-普通桌，B-卡座，V-包厢，T-外带）
     * @param sequence 序列号
     * @return 排队号码，格式：前缀 + 年月日时分秒(yyyyMMddHHmmss) + 3位数字（如：A20260522143045001）
     */
    public static String generate(String prefix, int sequence) {
        if (prefix == null || prefix.isEmpty()) {
            prefix = "A"; // 默认A队列
        }
        
        // 获取当前时间（年月日时分秒）
        String dateTimeStr = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        
        // 格式：前缀 + 年月日时分秒 + 3位序列号
        return String.format("%s%s%03d", prefix.toUpperCase(), dateTimeStr, sequence);
    }
    
    /**
     * 根据排队类型生成前缀
     * @param queueType 排队类型：1-堂食，2-外带
     * @param tableType 桌台类型：1-普通桌，2-卡座，3-包厢，NULL-不限制
     * @return 队列前缀
     */
    public static String getPrefixByType(Integer queueType, Integer tableType) {
        if (tableType != null) {
            switch (tableType) {
                case 2: return "B"; // 卡座
                case 3: return "V"; // 包厢
                default: return "A"; // 普通桌
            }
        }
        // 如果没有指定桌台类型，根据排队类型决定
        return queueType != null && queueType == 2 ? "T" : "A"; // T-外带，A-堂食
    }
}