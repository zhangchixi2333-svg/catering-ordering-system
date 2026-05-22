package org.example.paymentservice.config;

import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Feign错误解码器配置
 * 用于捕获Feign调用时的详细HTTP错误信息
 */
@Slf4j
@Configuration
public class FeignErrorDecoderConfig {

    @Bean
    public ErrorDecoder errorDecoder() {
        return new ErrorDecoder() {
            @Override
            public Exception decode(String methodKey, Response response) {
                log.error("========== Feign HTTP错误详情 ==========");
                log.error("【调用方法】{}", methodKey);
                log.error("【HTTP状态码】{}", response.status());
                log.error("【HTTP原因短语】{}", response.reason());
                log.error("【请求URL】{}", response.request().url());
                log.error("【请求方法】{}", response.request().httpMethod());
                
                // 记录请求头
                log.error("【请求头】");
                response.request().headers().forEach((key, value) -> 
                    log.error("  {}: {}", key, value)
                );
                
                // 记录响应头
                log.error("【响应头】");
                response.headers().forEach((key, value) -> 
                    log.error("  {}: {}", key, value)
                );
                
                log.error("======================================");
                
                // 返回默认的错误解码器处理结果
                return new Default().decode(methodKey, response);
            }
        };
    }
}
