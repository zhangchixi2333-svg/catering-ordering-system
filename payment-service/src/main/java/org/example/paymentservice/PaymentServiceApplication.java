package org.example.paymentservice;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * Payment Service 启动类
 */
@SpringBootApplication
@MapperScan("org.example.paymentservice.mapper")
@EnableDiscoveryClient
@EnableFeignClients  // 启用Feign客户端，自动扫描@FeignClient注解的接口
public class PaymentServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(PaymentServiceApplication.class, args);
        System.out.println("========================================");
        System.out.println("   Payment Service 启动成功！");
        System.out.println("   API文档: http://localhost:8084/doc.html");
        System.out.println("========================================");
    }
}
