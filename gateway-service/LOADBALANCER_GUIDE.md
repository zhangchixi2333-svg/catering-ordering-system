# 负载均衡算法自定义指南

## 概述

本项目支持多种负载均衡算法，可以通过配置文件灵活切换。

## 支持的负载均衡算法

### 1. 轮询算法 (Round-Robin)
- **配置值**: `round-robin`
- **描述**: 按顺序依次分配请求到各个服务实例
- **特点**: 简单公平，适合服务器性能相近的场景
- **实现类**: `LoadBalancerConfig.CustomRoundRobinLoadBalancer`

### 2. 随机算法 (Random)
- **配置值**: `random`
- **描述**: 随机选择一个服务实例
- **特点**: 实现简单，适合请求量大的场景
- **实现类**: `RandomLoadBalancer`

### 3. 最少连接数算法 (Least Connections)
- **配置值**: `least-connections`
- **描述**: 选择当前连接数最少的服务实例
- **特点**: 适合处理时间差异大的请求
- **实现类**: `LeastConnectionsLoadBalancer`

### 4. 加权随机算法 (Weighted Random)
- **配置值**: `weighted-random`
- **描述**: 根据权重随机选择服务实例
- **特点**: 可以根据服务器性能分配不同权重
- **实现类**: `WeightedRandomLoadBalancer`
- **权重配置**: 在服务实例的 metadata 中配置 `weight` 属性

## 配置方法

### 全局配置
在 `application.yml` 中配置全局负载均衡策略：

```yaml
spring:
  cloud:
    loadbalancer:
      strategy: round-robin  # 可选值: round-robin, random, least-connections, weighted-random
```

### 服务级别配置
可以为特定服务配置不同的负载均衡策略：

```yaml
spring:
  cloud:
    loadbalancer:
      clients:
        order-service:
          strategy: least-connections
        payment-service:
          strategy: weighted-random
```

## 权重配置示例

如果使用加权随机算法，需要在服务启动时配置权重：

```yaml
# order-service 的 application.yml
eureka:
  instance:
    metadata-map:
      weight: 3  # 该实例的权重为3
```

## 自定义负载均衡算法

### 步骤1: 实现接口
创建一个类实现 `ReactorLoadBalancer<ServiceInstance>` 接口：

```java
public class CustomLoadBalancer implements ReactorLoadBalancer<ServiceInstance> {
    private final ServiceInstanceListSupplier serviceInstanceListSupplier;
    private final String serviceId;

    public CustomLoadBalancer(ServiceInstanceListSupplier serviceInstanceListSupplier, String serviceId) {
        this.serviceInstanceListSupplier = serviceInstanceListSupplier;
        this.serviceId = serviceId;
    }

    @Override
    public Mono<Response<ServiceInstance>> choose(Request request) {
        return serviceInstanceListSupplier.get(request).next()
                .map(this::processInstanceResponse);
    }

    private Response<ServiceInstance> processInstanceResponse(List<ServiceInstance> instances) {
        // 自定义选择逻辑
        return new DefaultResponse(selectedInstance);
    }
}
```

### 步骤2: 注册Bean
在配置类中注册你的负载均衡器：

```java
@Bean
public ReactorLoadBalancer<ServiceInstance> customLoadBalancer(Environment environment,
                                                                LoadBalancerClientFactory loadBalancerClientFactory) {
    String name = environment.getProperty(LoadBalancerClientFactory.PROPERTY_NAME);
    return new CustomLoadBalancer(
            loadBalancerClientFactory.getLazyProvider(name, ServiceInstanceListSupplier.class), name);
}
```

### 步骤3: 添加到策略选择
在 `CustomLoadBalancerConfiguration` 中添加你的策略：

```java
return switch (strategy.toLowerCase()) {
    case "custom" -> new CustomLoadBalancer(
            loadBalancerClientFactory.getLazyProvider(name, ServiceInstanceListSupplier.class), name);
    // 其他策略...
};
```

## 监控和日志

所有负载均衡器都会记录选择日志，可以通过日志查看负载均衡情况：

```
Load balancing for service: order-service, selected instance: http://localhost:8081
```

## 性能建议

1. **轮询算法**: 适合大多数场景，性能稳定
2. **随机算法**: 适合高并发场景，避免热点问题
3. **最少连接数**: 适合请求处理时间差异大的场景
4. **加权随机**: 适合服务器性能不均匀的场景

## 故障处理

当没有可用的服务实例时，负载均衡器会返回 `EmptyResponse`，Gateway 会根据重试配置进行重试。

可以通过配置重试策略：

```yaml
spring:
  cloud:
    gateway:
      httpclient:
        connect-timeout: 1000
        response-timeout: 5s
        pool:
          max-connections: 500
```