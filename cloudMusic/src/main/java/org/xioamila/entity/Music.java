package org.xioamila.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.greatmap.modules.mybatis.base.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 音乐实体类
 */
@Data
@TableName("music")
@Schema(description = "音乐信息表")
public class Music extends BaseEntity {

    /**
     * 歌曲标题
     */
    @Schema(description = "歌曲标题")
    private String title;

    /**
     * 歌手名
     */
    @Schema(description = "歌手名")
    private String singer;

    /**
     * 专辑名
     */
    @Schema(description = "专辑名")
    private String album;

    /**
     * 歌曲时长（秒）
     */
    @Schema(description = "歌曲时长（秒）")
    private String duration;

    /**
     * 音乐文件存储路径
     */
    @Schema(description = "音乐文件存储路径")
    private String filePath;

    /**
     * 音乐文件名称
     */
    @Schema(description = "音乐文件名称")
    private String fileName;

    /**
     * 文件大小
     */
    @Schema(description = "文件大小")
    private String fileSize;

    /**
     * 文件格式
     */
    @Schema(description = "文件类型")
    private String fileType;

    /**
     * 播放次数
     */
    @Schema(description = "播放次数")
    private Integer playCount;

}
