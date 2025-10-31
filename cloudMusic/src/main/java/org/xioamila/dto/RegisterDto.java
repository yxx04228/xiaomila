package org.xioamila.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
@Schema(description = "用户注册请求")
public class RegisterDto {

    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 20, message = "用户名长度必须在3-20个字符之间")
    @Schema(description = "登录用户名", required = true)
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, message = "密码长度至少6位")
    @Schema(description = "密码", required = true)
    private String password;

    @NotBlank(message = "昵称不能为空")
    @Size(min = 1, max = 20, message = "昵称长度必须在1-20个字符之间")
    @Schema(description = "用户昵称", required = true)
    private String nickname;
}