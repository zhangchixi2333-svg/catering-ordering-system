package org.example.gatewayservice.loadbalancer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.loadbalancer.core.ReactorServiceInstanceLoadBalancer;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
import org.springframework.cloud.client.loadbalancer.DefaultResponse;
import org.springframework.cloud.client.loadbalancer.EmptyResponse;
import org.springframework.cloud.client.loadbalancer.Request;
import org.springframework.cloud.client.loadbalancer.Response;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class LeastConnectionsLoadBalancer implements ReactorServiceInstanceLoadBalancer {
    private final ObjectProvider<ServiceInstanceListSupplier> serviceInstanceListSupplierProvider;
    private final String serviceId;
    private final ConcurrentHashMap<String, AtomicInteger> connectionCounts = new ConcurrentHashMap<>();

    public LeastConnectionsLoadBalancer(ObjectProvider<ServiceInstanceListSupplier> serviceInstanceListSupplierProvider, String serviceId) {
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

        ServiceInstance selectedInstance = instances.stream()
                .min((i1, i2) -> {
                    int count1 = connectionCounts.computeIfAbsent(i1.getInstanceId(), k -> new AtomicInteger(0)).get();
                    int count2 = connectionCounts.computeIfAbsent(i2.getInstanceId(), k -> new AtomicInteger(0)).get();
                    return Integer.compare(count1, count2);
                })
                .orElse(instances.get(0));

        connectionCounts.computeIfAbsent(selectedInstance.getInstanceId(), k -> new AtomicInteger(0)).incrementAndGet();

        log.info("Least connections load balancing for service: {}, selected instance: {}, connections: {}", 
                serviceId, selectedInstance.getUri(), 
                connectionCounts.get(selectedInstance.getInstanceId()).get());

        return new DefaultResponse(selectedInstance);
    }

    public void releaseConnection(ServiceInstance instance) {
        if (instance != null && instance.getInstanceId() != null) {
            AtomicInteger count = connectionCounts.get(instance.getInstanceId());
            if (count != null && count.get() > 0) {
                count.decrementAndGet();
                log.debug("Released connection for instance: {}, current connections: {}", 
                        instance.getUri(), count.get());
            }
        }
    }
}
