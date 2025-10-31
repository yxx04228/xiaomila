package org.xioamila.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import com.greatmap.modules.mybatis.base.BaseEntity;
import lombok.Data;


/**
 * 用户实体类
 */
@Data
@TableName("user")
@Schema(description = "用户表")
public class User extends BaseEntity {

    /**
     * 登录用户名（唯一）
     */
    @Schema(description = "登录用户名（唯一）")
    private String username;

    /**
     * 加密后的密码
     */
    @Schema(description = "加密后的密码")
    private String password;

    /**
     * 用户昵称
     */
    @Schema(description = "用户昵称")
    private String nickname;

}