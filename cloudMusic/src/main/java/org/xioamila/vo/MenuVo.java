package org.xioamila.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.xioamila.entity.MusicMenu;

@Data
@Schema(description = "菜单Vo")
public class MenuVo extends MusicMenu {

    @Schema(description = "musicCount")
    private String musicCount;
}