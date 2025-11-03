package org.xioamila.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {

    /**
     * JWT 密钥（必须至少32个字符）
     */
    private String secret;

    /**
     * JWT 过期时间（毫秒）
     */
    private Long expiration = 86400000L;

    /**
     * JWT 请求头名称
     */
    private String header = "X-Access-Token";
}