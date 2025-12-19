package org.xioamila.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.greatmap.modules.core.util.Func;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.xioamila.entity.Music;
import org.xioamila.entity.MusicMenu;
import org.xioamila.entity.MusicMenuRelation;
import org.xioamila.service.MusicMenuRelationService;
import org.xioamila.service.MusicMenuService;
import org.xioamila.vo.MenuMusicVo;
import org.xioamila.vo.MenuVo;
import org.xioamila.vo.Result;

import javax.validation.Valid;

@Slf4j
@RestController
@RequestMapping("/music/menu")
@AllArgsConstructor
@Tag(name = "音乐菜单管理", description = "音乐菜单管理")
public class MusicMenuController {

    private MusicMenuService musicMenuService;

    private MusicMenuRelationService musicMenuRelationService;

    @Operation(summary = "菜单列表查询")
    @GetMapping("/getPageList")
    @Parameters({
            @Parameter(name = "title", description = "菜单标题", in = ParameterIn.QUERY, schema = @Schema(type = "string")),
            @Parameter(name = "createBy", description = "菜单创建人", required = true, in = ParameterIn.QUERY, schema = @Schema(type = "string"))
    })
    public Result<Page<MenuVo>> getPageList(@Parameter(hidden = true) MusicMenu musicMenu,
                                            @Parameter(description = "当前页数") @RequestParam(defaultValue = "1") Integer nCurrent,
                                            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer nSize) {
        if (StringUtils.isBlank(musicMenu.getCreateBy())) {
            return Result.error("用户id为空");
        }
        Page<MenuVo> menuList = musicMenuService.getPageList(new Page<>(nCurrent, nSize), musicMenu);
        return Result.data(menuList);
    }

    @PostMapping("/save")
    @Operation(summary = "新增或修改菜单")
    public Result<Boolean> save(@Valid @RequestBody MusicMenu musicMenu) {
        return Result.data(musicMenuService.saveOrUpdate(musicMenu));
    }

    @Operation(summary = "删除菜单")
    @DeleteMapping("/delete")
    public Result<Boolean> delete(@Parameter(description = "菜单ID", required = true) @RequestParam("ids") String ids) {
        return Result.data(musicMenuService.deleteMenu(Func.toStringList(ids)));
    }

    @PostMapping("/addMusic")
    @Operation(summary = "添加音乐")
    public Result<Boolean> addMusic(@Valid @RequestBody MusicMenuRelation musicMenuRelation) {
        return Result.data(musicMenuRelationService.addMusic(musicMenuRelation));
    }

    @Operation(summary = "删除音乐")
    @DeleteMapping("/deleteMusic")
    public Result<Boolean> deleteMusic(@Parameter(description = "菜单ID", required = true) @RequestParam("menuId") String menuId,
                                       @Parameter(description = "关系ID", required = true) @RequestParam("ids") String ids) {
        return Result.data(musicMenuRelationService.deleteMusic(menuId, Func.toStringList(ids)));
    }

    @Operation(summary = "调整音乐顺序")
    @PostMapping("/moveMusic")
    public Result<Boolean> moveMusic(@Parameter(description = "菜单ID") @RequestParam("menuId") String menuId,
                                     @Parameter(description = "调整前位置") @RequestParam("start") String start,
                                     @Parameter(description = "调整后位置") @RequestParam("end") String end) {
        return Result.data(musicMenuRelationService.moveMusic(menuId, start, end));
    }


    @Operation(summary = "音乐列表查询")
    @GetMapping("/listMusic")
    @Parameters({
            @Parameter(name = "title", description = "歌曲标题", in = ParameterIn.QUERY, schema = @Schema(type = "string")),
            @Parameter(name = "singer", description = "歌手", in = ParameterIn.QUERY, schema = @Schema(type = "string"))
    })
    public Result<Page<MenuMusicVo>> listMusic(@Parameter(hidden = true) Music music,
                                               @Parameter(description = "菜单ID", required = true) @RequestParam("menuId") String menuId,
                                               @Parameter(description = "当前页数") @RequestParam(defaultValue = "1") Integer nCurrent,
                                               @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer nSize) {
        if (StringUtils.isBlank(menuId)) {
            return Result.error("歌单id为空");
        }
        Page<MenuMusicVo> menuMusicList = musicMenuRelationService.listMusic(new Page<>(nCurrent, nSize), menuId, music);
        return Result.data(menuMusicList);
    }
}