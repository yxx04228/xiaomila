package org.xioamila.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.greatmap.modules.mybatis.base.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 音乐菜单关系实体类
 */
@Data
@TableName("music_menu_relation")
@Schema(description = "音乐菜单关系表")
public class MusicMenuRelation extends BaseEntity {

    /**
     * 音乐id
     */
    @Schema(description = "音乐id")
    private String musicId;

    /**
     * 菜单id
     */
    @Schema(description = "菜单id")
    private String menuId;

    /**
     * 排序
     */
    @Schema(description = "排序")
    private String position;

}
