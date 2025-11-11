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
import org.springframework.web.bind.annotation.*;
import org.xioamila.entity.Music;
import org.xioamila.entity.MusicMenu;
import org.xioamila.service.MusicMenuService;
import org.xioamila.vo.Result;

@Slf4j
@RestController
@RequestMapping("/music/menu")
@AllArgsConstructor
@Tag(name = "音乐菜单管理", description = "音乐菜单管理")
public class MusicMenuController {

    private MusicMenuService musicMenuService;

    // 用户-我的歌单
    // 添加音乐菜单：/menu/add、修改音乐菜单信息：/menu/update、删除音乐菜单：/menu/delete、音乐菜单列表查询：/menu/getPageList
    // 添加音乐：/menu/addMusic、 删除音乐：/menu/deleteMusic

    @Operation(summary = "菜单列表查询")
    @GetMapping("/getPageList")
    @Parameters({
            @Parameter(name = "title", description = "歌曲标题", in = ParameterIn.QUERY, schema = @Schema(type = "string"))
    })
    public Result<Page<MusicMenu>> getPageList(@Parameter(hidden = true) MusicMenu musicMenu,
                                           @Parameter(description = "当前页数") @RequestParam(defaultValue = "1") Integer nCurrent,
                                           @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer nSize) {

        Page<MusicMenu> menuList = musicMenuService.getPageList(new Page<>(nCurrent, nSize), musicMenu);
        return Result.data(menuList);
    }

    @PostMapping("/save")
    @Operation(summary = "新增或修改菜单")
    public Result<Boolean> save(@RequestBody MusicMenu musicMenu) {
        return Result.data(musicMenuService.saveOrUpdate(musicMenu));
    }

    @Operation(summary = "删除菜单")
    @DeleteMapping("/delete")
    public Result<Boolean> deleteUser(@Parameter(description = "菜单ID") @RequestParam("ids") String ids) {
        return Result.data(musicMenuService.deleteMenu(Func.toStringList(ids)));
    }

}
