package org.example.paymentservice;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Payment Service 启动类
 */
@SpringBootApplication
@MapperScan("org.example.paymentservice.mapper")
public class PaymentServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(PaymentServiceApplication.class, args);
        System.out.println("========================================");
        System.out.println("   Payment Service 启动成功！");
        System.out.println("   API文档: http://localhost:8084/doc.html");
        System.out.println("========================================");
    }
}
