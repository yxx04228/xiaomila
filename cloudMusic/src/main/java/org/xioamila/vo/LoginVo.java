package org.xioamila.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "登录返回数据")
public class LoginVo {

    @Schema(description = "用户ID")
    private String id;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "用户昵称")
    private String nickname;

    @Schema(description = "JWT令牌")
    private String token;

    @Schema(description = "令牌类型")
    private String tokenType = "Bearer";

    @Schema(description = "过期时间")
    private Long expiresIn;
}