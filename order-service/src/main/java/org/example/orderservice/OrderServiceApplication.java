package org.example.orderservice;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Order Service 启动类
 */
@SpringBootApplication
@MapperScan("org.example.orderservice.mapper")
public class OrderServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrderServiceApplication.class, args);
        System.out.println("========================================");
        System.out.println("   Order Service 启动成功！");
        System.out.println("   API文档: http://localhost:8083/doc.html");
        System.out.println("========================================");
    }
}
