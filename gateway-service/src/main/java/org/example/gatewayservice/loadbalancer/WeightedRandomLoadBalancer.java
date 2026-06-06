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

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
public class WeightedRandomLoadBalancer implements ReactorServiceInstanceLoadBalancer {
    private final ObjectProvider<ServiceInstanceListSupplier> serviceInstanceListSupplierProvider;
    private final String serviceId;
    private final Random random = ThreadLocalRandom.current();

    public WeightedRandomLoadBalancer(ObjectProvider<ServiceInstanceListSupplier> serviceInstanceListSupplierProvider, String serviceId) {
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

        List<ServiceInstance> weightedInstances = new ArrayList<>();
        for (ServiceInstance instance : instances) {
            int weight = getWeight(instance);
            for (int i = 0; i < weight; i++) {
                weightedInstances.add(instance);
            }
        }

        int index = random.nextInt(weightedInstances.size());
        ServiceInstance selectedInstance = weightedInstances.get(index);

        log.info("Weighted random load balancing for service: {}, selected instance: {}, weight: {}", 
                serviceId, selectedInstance.getUri(), getWeight(selectedInstance));

        return new DefaultResponse(selectedInstance);
    }

    private int getWeight(ServiceInstance instance) {
        String weightStr = instance.getMetadata().get("weight");
        return weightStr != null ? Integer.parseInt(weightStr) : 1;
    }
}
