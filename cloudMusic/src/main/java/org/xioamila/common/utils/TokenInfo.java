package org.xioamila.common.utils;

import lombok.Data;

@Data
public class TokenInfo {

    /**
     * token
     */
    private String token;

    /**
     * token失效时间
     */
    private Long expire;

}