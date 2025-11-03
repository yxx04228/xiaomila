package org.xioamila.common.config;

import com.baomidou.mybatisplus.autoconfigure.ConfigurationCustomizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.xioamila.common.interceptor.AuditInterceptor;

/**
 * MyBatis配置类
 */
@Configuration
public class MybatisConfig {

    @Autowired
    private AuditInterceptor auditInterceptor;

    @Bean
    public ConfigurationCustomizer configurationCustomizer() {
        return configuration -> {
            configuration.addInterceptor(auditInterceptor);
        };
    }
}