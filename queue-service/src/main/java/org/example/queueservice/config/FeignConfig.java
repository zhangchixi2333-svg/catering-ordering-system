package org.example.queueservice.config;

import feign.Logger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Feign 客户端配置
 */
@Configuration
public class FeignConfig {

    /**
     * 配置 Feign 日志级别
     * NONE: 不记录任何日志（默认）
     * BASIC: 仅记录请求方法、URL、响应状态码和执行时间
     * HEADERS: 记录 BASIC 级别的基础上，还记录请求和响应的头信息
     * FULL: 记录请求和响应的头信息、正文和元数据
     */
    @Bean
    public Logger.Level feignLoggerLevel() {
        return Logger.Level.FULL;
    }
}
