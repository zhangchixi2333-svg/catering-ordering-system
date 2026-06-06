package org.example.notificationservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * Notification Service 启动类
 */
@SpringBootApplication
@EnableDiscoveryClient
public class NotificationServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(NotificationServiceApplication.class, args);
        System.out.println("========================================");
        System.out.println("   Notification Service 启动成功！");
        System.out.println("   API文档: http://localhost:8086/doc.html");
        System.out.println("   WebSocket端口: 8086");
        System.out.println("========================================");
    }
}
