package org.example.menuservice.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger配置类
 */
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("菜单服务 API 文档")
                        .version("1.0")
                        .description("餐饮点单排队系统 - 菜单服务接口文档")
                        .contact(new Contact()
                                .name("开发团队")
                                .email("dev@example.com")));
    }
}
