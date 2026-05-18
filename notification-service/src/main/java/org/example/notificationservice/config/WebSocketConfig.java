package org.example.notificationservice.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

/**
 * WebSocket配置类
 * 启用WebSocket支持，配置服务端点
 */
@Slf4j
@Configuration
@EnableWebSocket
public class WebSocketConfig {

    /**
     * 注册ServerEndpointExporter
     * 用于扫描@ServerEndpoint注解的WebSocket端点
     */
    @Bean
    public ServerEndpointExporter serverEndpointExporter() {
        return new ServerEndpointExporter();
    }
}
