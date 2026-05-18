package org.example.gatewayservice.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Component
public class LoggingGlobalFilter implements GlobalFilter, Ordered {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        
        String requestId = generateRequestId();
        LocalDateTime startTime = LocalDateTime.now();
        
        // 记录请求信息
        log.info("\n========== 请求开始 [{}] ==========", requestId);
        log.info("时间: {}", startTime.format(FORMATTER));
        log.info("方法: {}", request.getMethod());
        log.info("路径: {}", request.getURI().getPath());
        log.info("客户端IP: {}", request.getRemoteAddress() != null ? 
                request.getRemoteAddress().getAddress().getHostAddress() : "unknown");
        
        // 记录查询参数
        if (!request.getQueryParams().isEmpty()) {
            log.info("参数: {}", request.getQueryParams());
        }
        
        // 执行后续过滤器
        return chain.filter(exchange).then(Mono.fromRunnable(() -> {
            LocalDateTime endTime = LocalDateTime.now();
            long duration = java.time.Duration.between(startTime, endTime).toMillis();
            
            // 记录响应信息
            log.info("========== 请求结束 [{}] ==========", requestId);
            log.info("耗时: {} ms", duration);
            log.info("状态码: {}\n", exchange.getResponse().getStatusCode());
        }));
    }

    /**
     * 生成请求ID
     */
    private String generateRequestId() {
        return "REQ-" + System.currentTimeMillis() % 1000000;
    }

    @Override
    public int getOrder() {
        return -2; // 比认证过滤器优先级更高
    }
}
