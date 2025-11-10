package org.xioamila.common.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.xioamila.common.interceptor.JwtInterceptor;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private JwtInterceptor jwtInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        // 业务接口
                        "/user/login",
                        "/user/register",
                        "/newWork/health",

                        // Swagger UI 界面
                        "/swagger-ui/**",
                        "/swagger-ui.html",

                        // Swagger 资源
                        "/swagger-resources/**",
                        "/webjars/**",
                        "/v2/api-docs",

                        // SpringDoc OpenAPI 3
                        "/v3/api-docs/**",
                        "/swagger-config",
                        "/doc.html",

                        // 静态资源
                        "/error"
                );
    }
}