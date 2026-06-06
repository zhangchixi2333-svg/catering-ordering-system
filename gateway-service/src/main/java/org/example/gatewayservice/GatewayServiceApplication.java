package org.example.gatewayservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.loadbalancer.annotation.LoadBalancerClients;
import org.example.gatewayservice.config.CustomLoadBalancerConfiguration;

@SpringBootApplication
@EnableDiscoveryClient
@LoadBalancerClients(defaultConfiguration = CustomLoadBalancerConfiguration.class)
public class GatewayServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(GatewayServiceApplication.class, args);
        System.out.println("\n========== Gateway Service Started ==========");
        System.out.println("网关端口: http://localhost:8080");
        System.out.println("Eureka 控制台: http://localhost:8761");
        System.out.println("==============================================\n");
    }
}
