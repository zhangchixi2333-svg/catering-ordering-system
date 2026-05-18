package org.example.shopservice;

import org.example.shopservice.feign.ShopFeignClient;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * Shop Service 启动类
 */
@SpringBootApplication
@EnableDiscoveryClient
@MapperScan("org.example.shopservice.mapper")
@EnableFeignClients(clients = ShopFeignClient.class)
public class ShopServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ShopServiceApplication.class, args);
        System.out.println("========================================");
        System.out.println("   Shop Service 启动成功！");
        System.out.println("   API文档: http://localhost:8081/doc.html");
        System.out.println("========================================");
    }
}
