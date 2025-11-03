package org.xioamila.common.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                // 定义安全方案（JWT 令牌）
                .components(new Components()
                        .addSecuritySchemes("X-Access-Token",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.APIKEY)
                                        .in(SecurityScheme.In.HEADER) // 令牌放在请求头
                                        .name("X-Access-Token") // 与拦截器使用的头名称一致
                        )
                )
                // 全局添加安全要求（所有接口都需要携带令牌，登录接口除外）
                .addSecurityItem(new SecurityRequirement().addList("X-Access-Token"));
    }
}