package org.example.menuservice;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * 菜单服务启动类
 */
@SpringBootApplication
@MapperScan("org.example.menuservice.mapper")
@EnableDiscoveryClient
public class MenuServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(MenuServiceApplication.class, args);
        System.out.println("========================================");
        System.out.println("   Menu Service 启动成功！");
        System.out.println("   API文档: http://localhost:8181/doc.html");
        System.out.println("========================================");
    }
}
