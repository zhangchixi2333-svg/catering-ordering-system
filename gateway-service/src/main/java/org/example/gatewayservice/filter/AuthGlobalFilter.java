package org.example.gatewayservice.filter;

import lombok.extern.slf4j.Slf4j;
import org.example.gatewayservice.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
@Component
public class AuthGlobalFilter implements GlobalFilter, Ordered {

    @Autowired
    private JwtUtil jwtUtil;

    @Value("${security.whitelist:}")
    private String[] whitelistArray;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();
        
        String path = request.getURI().getPath();
        
        // 1. 检查是否在白名单中
        if (isWhitelist(path)) {
            log.debug("✅ 白名单路径，跳过认证: {}", path);
            return chain.filter(exchange);
        }

        // 2. 获取 Token
        String authorization = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            log.warn("❌ 未提供 Token: {}", path);
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return response.setComplete();
        }

        String token = authorization.substring(7);

        // 3. 验证 Token
        if (!jwtUtil.validateToken(token)) {
            log.warn("❌ Token 无效或已过期: {}", path);
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return response.setComplete();
        }

        // 4. Token 有效，提取用户信息并传递到下游服务
        Long userId = jwtUtil.getUserIdFromToken(token);
        String username = jwtUtil.getUsernameFromToken(token);
        String role = jwtUtil.getRoleFromToken(token);

        log.info("✅ Token 验证通过 - 用户ID: {}, 用户名: {}, 角色: {}, 路径: {}", 
                userId, username, role, path);

        // 5. 将用户信息添加到请求头，传递给下游服务
        ServerHttpRequest modifiedRequest = request.mutate()
                .header("X-User-Id", String.valueOf(userId))
                .header("X-Username", username)
                .header("X-User-Role", role)
                .build();

        return chain.filter(exchange.mutate().request(modifiedRequest).build());
    }

    /**
     * 检查路径是否在白名单中
     */
    private boolean isWhitelist(String path) {
        if (whitelistArray == null || whitelistArray.length == 0) {
            return false;
        }
        for (String pattern : whitelistArray) {
            if (pattern.endsWith("/**")) {
                String prefix = pattern.substring(0, pattern.length() - 3);
                if (path.startsWith(prefix)) {
                    return true;
                }
            } else if (pattern.endsWith("*")) {
                String prefix = pattern.substring(0, pattern.length() - 1);
                if (path.startsWith(prefix)) {
                    return true;
                }
            } else {
                if (path.equals(pattern)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 过滤器优先级，数值越小优先级越高
     */
    @Override
    public int getOrder() {
        return -1;
    }
}
