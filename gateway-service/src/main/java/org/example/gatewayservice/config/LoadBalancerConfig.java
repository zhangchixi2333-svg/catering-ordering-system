package org.example.gatewayservice.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.loadbalancer.core.ReactorServiceInstanceLoadBalancer;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
import org.springframework.cloud.client.loadbalancer.EmptyResponse;
import org.springframework.cloud.client.loadbalancer.Response;
import org.springframework.cloud.client.loadbalancer.DefaultResponse;
import org.springframework.cloud.client.loadbalancer.Request;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class LoadBalancerConfig {

    static class CustomRoundRobinLoadBalancer implements ReactorServiceInstanceLoadBalancer {
        private final ObjectProvider<ServiceInstanceListSupplier> serviceInstanceListSupplierProvider;
        private final String serviceId;
        private final AtomicInteger position = new AtomicInteger(0);

        CustomRoundRobinLoadBalancer(ObjectProvider<ServiceInstanceListSupplier> serviceInstanceListSupplierProvider, String serviceId) {
            this.serviceInstanceListSupplierProvider = serviceInstanceListSupplierProvider;
            this.serviceId = serviceId;
        }

        @Override
        public Mono<Response<ServiceInstance>> choose(Request request) {
            ServiceInstanceListSupplier supplier = serviceInstanceListSupplierProvider.getIfAvailable();
            if (supplier == null) {
                log.warn("No ServiceInstanceListSupplier available for service: {}", serviceId);
                return Mono.just(new EmptyResponse());
            }
            return supplier.get(request).next()
                    .map(this::processInstanceResponse);
        }

        private Response<ServiceInstance> processInstanceResponse(List<ServiceInstance> instances) {
            if (instances.isEmpty()) {
                log.warn("No servers available for service: {}", serviceId);
                return new EmptyResponse();
            }
            
            int pos = Math.abs(this.position.incrementAndGet());
            ServiceInstance instance = instances.get(pos % instances.size());
            
            log.info("Load balancing for service: {}, selected instance: {}", 
                    serviceId, instance.getUri());
            
            return new DefaultResponse(instance);
        }
    }
}
