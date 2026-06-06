package org.example.gatewayservice.config;

import org.example.gatewayservice.loadbalancer.LeastConnectionsLoadBalancer;
import org.example.gatewayservice.loadbalancer.RandomLoadBalancer;
import org.example.gatewayservice.loadbalancer.WeightedRandomLoadBalancer;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.loadbalancer.core.ReactorServiceInstanceLoadBalancer;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
import org.springframework.cloud.loadbalancer.support.LoadBalancerClientFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

public class CustomLoadBalancerConfiguration {

    @Bean
    public ReactorServiceInstanceLoadBalancer customLoadBalancer(Environment environment,
                                                                 LoadBalancerClientFactory loadBalancerClientFactory) {
        String name = environment.getProperty(LoadBalancerClientFactory.PROPERTY_NAME);
        String strategy = environment.getProperty("spring.cloud.loadbalancer.strategy", "round-robin");

        return switch (strategy.toLowerCase()) {
            case "random" -> new RandomLoadBalancer(
                    loadBalancerClientFactory.getLazyProvider(name, ServiceInstanceListSupplier.class), name);
            case "least-connections" -> new LeastConnectionsLoadBalancer(
                    loadBalancerClientFactory.getLazyProvider(name, ServiceInstanceListSupplier.class), name);
            case "weighted-random" -> new WeightedRandomLoadBalancer(
                    loadBalancerClientFactory.getLazyProvider(name, ServiceInstanceListSupplier.class), name);
            default -> new LoadBalancerConfig.CustomRoundRobinLoadBalancer(
                    loadBalancerClientFactory.getLazyProvider(name, ServiceInstanceListSupplier.class), name);
        };
    }
}
