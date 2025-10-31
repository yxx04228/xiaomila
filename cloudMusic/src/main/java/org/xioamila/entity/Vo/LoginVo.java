package org.xioamila.entity.Vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "登录响应数据")
public class LoginVo {

    @Schema(description = "用户ID")
    private String id;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "用户昵称")
    private String nickname;

    @Schema(description = "登录状态")
    private Boolean success;

    @Schema(description = "消息")
    private String message;
}