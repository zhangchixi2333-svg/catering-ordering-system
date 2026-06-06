package org.example.queueservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Redis排队管理服务
 * 使用Sorted Set实现实时排队和叫号功能
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RedisQueueService {

    private final StringRedisTemplate redisTemplate;

    // Redis Key前缀
    private static final String WAITING_QUEUE_KEY = "queue:waiting:";      // 等待中的排队
    private static final String CALLING_QUEUE_KEY = "queue:calling:";      // 已叫号的排队
    private static final String COMPLETED_QUEUE_KEY = "queue:completed:";  // 已完成的排队

    /**
     * 添加到等待队列
     * @param shopId 店铺ID
     * @param queueId 排队ID
     * @param timestamp 取号时间戳
     */
    public void addToWaitingQueue(Long shopId, Long queueId, long timestamp) {
        String key = WAITING_QUEUE_KEY + shopId;
        redisTemplate.opsForZSet().add(key, String.valueOf(queueId), (double) timestamp);
        
        // 设置过期时间（24小时）
        redisTemplate.expire(key, 24, TimeUnit.HOURS);
        
        log.info("添加到等待队列 - 店铺ID: {}, 排队ID: {}, 时间戳: {}", shopId, queueId, timestamp);
    }

    /**
     * 从等待队列移除
     * @param shopId 店铺ID
     * @param queueId 排队ID
     */
    public void removeFromWaitingQueue(Long shopId, Long queueId) {
        String key = WAITING_QUEUE_KEY + shopId;
        redisTemplate.opsForZSet().remove(key, String.valueOf(queueId));
        log.info("从等待队列移除 - 店铺ID: {}, 排队ID: {}", shopId, queueId);
    }

    /**
     * 移动到叫号队列
     * @param shopId 店铺ID
     * @param queueId 排队ID
     * @param callTimestamp 叫号时间戳
     */
    public void moveToCallingQueue(Long shopId, Long queueId, long callTimestamp) {
        // 从等待队列移除
        removeFromWaitingQueue(shopId, queueId);
        
        // 添加到叫号队列
        String key = CALLING_QUEUE_KEY + shopId;
        redisTemplate.opsForZSet().add(key, String.valueOf(queueId), (double) callTimestamp);
        redisTemplate.expire(key, 24, TimeUnit.HOURS);
        
        log.info("移动到叫号队列 - 店铺ID: {}, 排队ID: {}", shopId, queueId);
    }

    /**
     * 移动到完成队列
     * @param shopId 店铺ID
     * @param queueId 排队ID
     */
    public void moveToCompletedQueue(Long shopId, Long queueId) {
        // 从叫号队列移除
        String callingKey = CALLING_QUEUE_KEY + shopId;
        redisTemplate.opsForZSet().remove(callingKey, String.valueOf(queueId));
        
        // 添加到完成队列
        String completedKey = COMPLETED_QUEUE_KEY + shopId;
        redisTemplate.opsForZSet().add(completedKey, String.valueOf(queueId), 
                (double) System.currentTimeMillis());
        redisTemplate.expire(completedKey, 24, TimeUnit.HOURS);
        
        log.info("移动到完成队列 - 店铺ID: {}, 排队ID: {}", shopId, queueId);
    }

    /**
     * 获取等待队列中的所有排队ID（按时间排序）
     * @param shopId 店铺ID
     * @return 排队ID集合
     */
    public Set<String> getWaitingQueue(Long shopId) {
        String key = WAITING_QUEUE_KEY + shopId;
        return redisTemplate.opsForZSet().range(key, 0, -1);
    }

    /**
     * 获取等待队列中前N个排队记录
     * @param shopId 店铺ID
     * @param count 数量
     * @return 排队ID集合
     */
    public Set<String> getWaitingQueueTopN(Long shopId, int count) {
        String key = WAITING_QUEUE_KEY + shopId;
        return redisTemplate.opsForZSet().range(key, 0, count - 1);
    }

    /**
     * 获取等待队列长度
     * @param shopId 店铺ID
     * @return 等待人数
     */
    public Long getWaitingCount(Long shopId) {
        String key = WAITING_QUEUE_KEY + shopId;
        return redisTemplate.opsForZSet().size(key);
    }

    /**
     * 获取叫号队列中的所有排队ID
     * @param shopId 店铺ID
     * @return 排队ID集合
     */
    public Set<String> getCallingQueue(Long shopId) {
        String key = CALLING_QUEUE_KEY + shopId;
        return redisTemplate.opsForZSet().range(key, 0, -1);
    }

    /**
     * 获取用户在等待队列中的位置
     * @param shopId 店铺ID
     * @param queueId 排队ID
     * @return 排名（从0开始），-1表示不在队列中
     */
    public Long getQueuePosition(Long shopId, Long queueId) {
        String key = WAITING_QUEUE_KEY + shopId;
        return redisTemplate.opsForZSet().rank(key, String.valueOf(queueId));
    }

    /**
     * 检查排队是否在等待队列中
     * @param shopId 店铺ID
     * @param queueId 排队ID
     * @return true-在队列中，false-不在
     */
    public boolean isInWaitingQueue(Long shopId, Long queueId) {
        String key = WAITING_QUEUE_KEY + shopId;
        Double score = redisTemplate.opsForZSet().score(key, String.valueOf(queueId));
        return score != null;
    }

    /**
     * 清除已完成队列的旧数据（保留最近1小时）
     * @param shopId 店铺ID
     */
    public void cleanCompletedQueue(Long shopId) {
        String key = COMPLETED_QUEUE_KEY + shopId;
        long oneHourAgo = System.currentTimeMillis() - 3600000;
        redisTemplate.opsForZSet().removeRangeByScore(key, 0, (double) oneHourAgo);
    }

    /**
     * 清空店铺的所有队列数据（测试用）
     * @param shopId 店铺ID
     */
    public void clearAllQueues(Long shopId) {
        redisTemplate.delete(WAITING_QUEUE_KEY + shopId);
        redisTemplate.delete(CALLING_QUEUE_KEY + shopId);
        redisTemplate.delete(COMPLETED_QUEUE_KEY + shopId);
        log.info("清空店铺所有队列数据 - 店铺ID: {}", shopId);
    }
}
