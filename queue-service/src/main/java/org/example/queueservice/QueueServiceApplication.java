package org.example.queueservice;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Queue Service 启动类
 */
@SpringBootApplication
@MapperScan("org.example.queueservice.mapper")
public class QueueServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(QueueServiceApplication.class, args);
        System.out.println("========================================");
        System.out.println("   Queue Service 启动成功！");
        System.out.println("   API文档: http://localhost:8085/doc.html");
        System.out.println("========================================");
    }
}
