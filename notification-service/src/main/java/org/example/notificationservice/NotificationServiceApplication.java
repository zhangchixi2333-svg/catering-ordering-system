package org.example.notificationservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Notification Service 启动类
 */
@SpringBootApplication
public class NotificationServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(NotificationServiceApplication.class, args);
        System.out.println("========================================");
        System.out.println("   Notification Service 启动成功！");
        System.out.println("   WebSocket端口: 8086");
        System.out.println("========================================");
    }
}
