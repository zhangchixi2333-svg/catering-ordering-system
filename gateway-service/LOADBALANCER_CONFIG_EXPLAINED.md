# 负载均衡配置机制详解

## 🎯 核心概念

### 1. 配置类的作用和角色

配置类在整个负载均衡系统中充当**工厂和策略选择器**的角色：

```
配置文件 → 配置类 → 具体负载均衡器 → 实际负载均衡
```

#### 配置类的职责：
- 🏭 **工厂模式**：根据配置创建具体的负载均衡器实例
- 🎛️ **策略选择**：读取配置，决定使用哪种负载均衡算法
- 🔧 **依赖注入**：将必要的依赖（如ServiceInstanceListSupplier）注入到负载均衡器中
- 📋 **Bean管理**：将负载均衡器注册为Spring Bean，供其他组件使用

### 2. 配置类与启动类的关系

#### 启动类
```java
@SpringBootApplication
@EnableDiscoveryClient
public class GatewayServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(GatewayServiceApplication.class, args);
    }
}
```

**职责：**
- 🚀 **应用启动**：启动Spring Boot应用
- 📦 **组件扫描**：扫描并加载所有Spring组件
- 🔗 **自动配置**：启用Spring Cloud的自动配置功能
- 📡 **服务发现**：注册到Eureka服务注册中心

#### 配置类
```java
@Configuration
public class CustomLoadBalancerConfiguration {
    @Bean
    public ReactorLoadBalancer<ServiceInstance> customLoadBalancer(...) {
        // 创建和配置负载均衡器
    }
}
```

**职责：**
- 🏗️ **Bean定义**：定义具体的Bean实例
- ⚙️ **业务配置**：配置业务逻辑相关的组件
- 🔌 **依赖装配**：装配组件之间的依赖关系

### 3. Bean的关系和区别

#### Bean的定义方式

**方式1：在启动类中定义Bean**
```java
@SpringBootApplication
public class GatewayServiceApplication {
    
    @Bean
    public ReactorLoadBalancer<ServiceInstance> loadBalancer() {
        return new CustomRoundRobinLoadBalancer();
    }
}
```

**方式2：在配置类中定义Bean**
```java
@Configuration
public class LoadBalancerConfig {
    
    @Bean
    public ReactorLoadBalancer<ServiceInstance> loadBalancer() {
        return new CustomRoundRobinLoadBalancer();
    }
}
```

#### 区别对比

| 特性 | 启动类中定义Bean | 配置类中定义Bean |
|------|----------------|----------------|
| **职责分离** | ❌ 混合启动逻辑和业务配置 | ✅ 职责清晰分离 |
| **可维护性** | ❌ 启动类变得臃肿 | ✅ 配置独立管理 |
| **可测试性** | ❌ 难以单独测试配置 | ✅ 配置可单独测试 |
| **复用性** | ❌ 配置无法复用 | ✅ 配置可跨项目复用 |
| **条件装配** | ❌ 难以实现条件装配 | ✅ 支持条件注解 |

## 🔧 配置类工作流程

### 完整的负载均衡流程

```
1. 应用启动
   ↓
2. 读取配置文件 (application.yml)
   spring.cloud.loadbalancer.strategy: random
   ↓
3. 配置类被扫描加载
   @Configuration public class CustomLoadBalancerConfiguration
   ↓
4. @Bean方法被执行
   创建具体的负载均衡器实例
   ↓
5. Bean注册到Spring容器
   ReactorLoadBalancer<ServiceInstance> bean
   ↓
6. Gateway使用负载均衡器
   lb://service-name → 负载均衡器选择实例
   ↓
7. 请求被路由到选中的实例
   http://localhost:8081/api/orders
```

### 配置类详细执行过程

```java
@Configuration
public class CustomLoadBalancerConfiguration {
    
    @Bean  // Spring容器会调用这个方法
    public ReactorLoadBalancer<ServiceInstance> customLoadBalancer(
            Environment environment,  // Spring自动注入Environment
            LoadBalancerClientFactory factory) {  // Spring自动注入Factory
        
        // 1. 读取配置
        String strategy = environment.getProperty(
            "spring.cloud.loadbalancer.strategy", 
            "round-robin"
        );
        
        // 2. 根据配置选择策略
        return switch (strategy.toLowerCase()) {
            case "random" -> new RandomLoadBalancer(...);
            case "least-connections" -> new LeastConnectionsLoadBalancer(...);
            default -> new CustomRoundRobinLoadBalancer(...);
        };
        
        // 3. 返回的实例会被注册为Spring Bean
        // 4. 其他组件可以通过@Autowired注入使用
    }
}
```

## 🎨 设计模式应用

### 1. 工厂模式
配置类充当工厂，根据配置创建不同的产品（负载均衡器）：

```java
@Configuration
public class LoadBalancerFactory {
    
    @Bean
    public ReactorLoadBalancer<ServiceInstance> createLoadBalancer(String strategy) {
        return switch (strategy) {
            case "random" -> new RandomLoadBalancer();
            case "round-robin" -> new RoundRobinLoadBalancer();
            default -> throw new IllegalArgumentException();
        };
    }
}
```

### 2. 策略模式
不同的负载均衡算法是不同的策略实现：

```java
// 策略接口
public interface ReactorLoadBalancer<T> {
    Mono<Response<T>> choose(Request request);
}

// 具体策略
public class RandomLoadBalancer implements ReactorLoadBalancer<ServiceInstance> {
    // 随机策略实现
}

public class RoundRobinLoadBalancer implements ReactorLoadBalancer<ServiceInstance> {
    // 轮询策略实现
}
```

### 3. 依赖注入模式
Spring容器负责管理依赖关系：

```java
public class RandomLoadBalancer {
    private final ObjectProvider<ServiceInstanceListSupplier> supplier;
    
    // 构造函数注入
    public RandomLoadBalancer(
            ObjectProvider<ServiceInstanceListSupplier> supplier) {
        this.supplier = supplier;  // Spring自动注入
    }
}
```

## 🔄 配置切换机制

### 动态切换负载均衡算法

#### 方法1：修改配置文件
```yaml
spring:
  cloud:
    loadbalancer:
      strategy: random  # 修改这里切换算法
```

#### 方法2：环境变量
```bash
java -jar gateway-service.jar --spring.cloud.loadbalancer.strategy=random
```

#### 方法3：启动参数
```java
public static void main(String[] args) {
    SpringApplication.run(GatewayServiceApplication.class, 
        "--spring.cloud.loadbalancer.strategy=random");
}
```

## 📊 配置类的高级用法

### 1. 条件装配
```java
@Configuration
public class ConditionalLoadBalancerConfig {
    
    @Bean
    @ConditionalOnProperty(name = "loadbalancer.strategy", havingValue = "random")
    public ReactorLoadBalancer<ServiceInstance> randomLoadBalancer() {
        return new RandomLoadBalancer();
    }
    
    @Bean
    @ConditionalOnProperty(name = "loadbalancer.strategy", havingValue = "round-robin")
    public ReactorLoadBalancer<ServiceInstance> roundRobinLoadBalancer() {
        return new RoundRobinLoadBalancer();
    }
}
```

### 2. 多服务不同策略
```java
@Configuration
public class ServiceSpecificLoadBalancerConfig {
    
    @Bean
    @LoadBalancerClient(name = "order-service", configuration = OrderLoadBalancerConfig.class)
    public ReactorLoadBalancer<ServiceInstance> orderServiceLoadBalancer() {
        return new LeastConnectionsLoadBalancer();
    }
    
    @Bean
    @LoadBalancerClient(name = "payment-service", configuration = PaymentLoadBalancerConfig.class)
    public ReactorLoadBalancer<ServiceInstance> paymentServiceLoadBalancer() {
        return new WeightedRandomLoadBalancer();
    }
}
```

### 3. 自定义配置属性
```java
@Configuration
@ConfigurationProperties(prefix = "loadbalancer.custom")
public class LoadBalancerProperties {
    private String strategy = "round-robin";
    private int retryCount = 3;
    private long timeout = 5000;
    
    // getters and setters
}

@Configuration
@EnableConfigurationProperties(LoadBalancerProperties.class)
public class LoadBalancerAutoConfig {
    
    @Bean
    public ReactorLoadBalancer<ServiceInstance> loadBalancer(
            LoadBalancerProperties properties) {
        // 使用自定义配置属性
        return createLoadBalancer(properties.getStrategy());
    }
}
```

## 🎯 最佳实践

### 1. 配置类设计原则
- ✅ **单一职责**：每个配置类只负责一类组件的配置
- ✅ **清晰命名**：配置类名称要体现其配置的内容
- ✅ **条件装配**：使用条件注解避免不必要的Bean创建
- ✅ **外部化配置**：将可变配置放在配置文件中

### 2. Bean定义最佳实践
```java
// ✅ 好的做法
@Configuration
public class LoadBalancerConfig {
    
    @Bean
    @ConditionalOnMissingBean  // 如果没有其他Bean，才创建这个
    public ReactorLoadBalancer<ServiceInstance> defaultLoadBalancer() {
        return new RoundRobinLoadBalancer();
    }
}

// ❌ 不好的做法
@SpringBootApplication
public class Application {
    
    @Bean
    public ReactorLoadBalancer<ServiceInstance> loadBalancer() {
        // 启动类中定义业务Bean
        return new RoundRobinLoadBalancer();
    }
    
    @Bean
    public DataSource dataSource() {
        // 又定义了数据源Bean
        return createDataSource();
    }
    
    // 启动类变得臃肿，职责不清
}
```

### 3. 依赖注入最佳实践
```java
// ✅ 构造函数注入（推荐）
public class LoadBalancerService {
    private final ReactorLoadBalancer<ServiceInstance> loadBalancer;
    
    public LoadBalancerService(
            ReactorLoadBalancer<ServiceInstance> loadBalancer) {
        this.loadBalancer = loadBalancer;
    }
}

// ✅ Setter注入（可选依赖）
public class LoadBalancerService {
    private ReactorLoadBalancer<ServiceInstance> loadBalancer;
    
    @Autowired
    public void setLoadBalancer(
            ReactorLoadBalancer<ServiceInstance> loadBalancer) {
        this.loadBalancer = loadBalancer;
    }
}

// ❌ 字段注入（不推荐）
public class LoadBalancerService {
    @Autowired
    private ReactorLoadBalancer<ServiceInstance> loadBalancer;
}
```

## 🔍 调试和监控

### 查看注册的Bean
```java
@Component
public class BeanDebugger implements ApplicationListener<ApplicationReadyEvent> {
    
    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        ConfigurableApplicationContext context = event.getApplicationContext();
        
        // 查看所有负载均衡相关的Bean
        String[] beanNames = context.getBeanNamesForType(ReactorLoadBalancer.class);
        System.out.println("Registered LoadBalancer Beans:");
        for (String beanName : beanNames) {
            System.out.println("  - " + beanName);
        }
    }
}
```

### 监控负载均衡选择
```java
@Slf4j
public class MonitoredLoadBalancer implements ReactorLoadBalancer<ServiceInstance> {
    
    private final ReactorLoadBalancer<ServiceInstance> delegate;
    
    @Override
    public Mono<Response<ServiceInstance>> choose(Request request) {
        long startTime = System.currentTimeMillis();
        
        return delegate.choose(request)
                .doOnNext(response -> {
                    long duration = System.currentTimeMillis() - startTime;
                    log.info("Load balancing took {}ms, selected: {}", 
                            duration, response.getServer());
                });
    }
}
```

## 📝 总结

### 配置类的核心作用
1. **工厂角色**：根据配置创建具体的实现类
2. **策略选择**：实现运行时策略切换
3. **依赖管理**：处理组件间的依赖关系
4. **生命周期管理**：管理Bean的创建和销毁

### 启动类与配置类的关系
- **启动类**：应用的入口，负责启动和基础配置
- **配置类**：业务组件的配置工厂，负责具体功能的配置
- **协作关系**：启动类扫描配置类，配置类定义业务Bean

### Bean管理的最佳实践
- 使用配置类分离业务配置
- 优先使用构造函数注入
- 合理使用条件装配
- 保持配置类的单一职责