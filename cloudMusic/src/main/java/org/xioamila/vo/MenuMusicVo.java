package org.xioamila.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "菜单音乐Vo")
public class MenuMusicVo {

    @Schema(description = "id")
    private String id;

    @Schema(description = "排序")
    private String position;

    @Schema(description = "菜单id")
    private String menuId;

    @Schema(description = "菜单标题")
    private String menuTitle;

    @Schema(description = "音乐id")
    private String musicId;

    @Schema(description = "音乐标题")
    private String musicTitle;

    @Schema(description = "歌手")
    private String singer;

    @Schema(description = "专辑")
    private String album;

    @Schema(description = "时长")
    private String duration;

    @Schema(description = "文件大小")
    private String fileSize;

    @Schema(description = "下载次数")
    private String downloadCount;
}