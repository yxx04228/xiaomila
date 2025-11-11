package org.xioamila.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.greatmap.modules.mybatis.base.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 音乐菜单实体类
 */
@Data
@TableName("music_menu")
@Schema(description = "音乐菜单表")
public class MusicMenu extends BaseEntity {

    /**
     * 菜单标题
     */
    @Schema(description = "菜单标题")
    private String title;

}
