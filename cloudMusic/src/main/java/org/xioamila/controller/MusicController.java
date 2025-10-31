package org.xioamila.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.xioamila.entity.Music;
import org.xioamila.service.MusicService;

@Slf4j
@RestController
@RequestMapping("/music")
@AllArgsConstructor
@Tag(name = "音乐管理", description = "音乐管理")
public class MusicController {

    private MusicService musicService;

    @Operation(summary = "音乐列表查询")
    @GetMapping("/getPageList")
    @Parameters({
            @Parameter(name = "title", description = "歌曲标题", in = ParameterIn.QUERY, schema = @Schema(type = "string"))
    })
    public IPage<Music> getPageList(@RequestParam(required = false) String title,
                                    @Parameter(description = "当前页数") @RequestParam(defaultValue = "1") Integer nCurrent,
                                    @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer nSize) {

        QueryWrapper<Music> queryWrapper = new QueryWrapper<>();

        if (StringUtils.isNotBlank(title)) {
            queryWrapper.like("title", title);
        }
        queryWrapper.orderByDesc("create_time");
        IPage<Music> pageList = musicService.page(new Page<>(nCurrent, nSize), queryWrapper);
        return pageList;
    }

    @Operation(summary = "音乐文件上传")
    @PostMapping(value = "/upload")
    public String uploadMusic(
            @Parameter(description = "音乐文件") @RequestParam("file") MultipartFile file,
            @Parameter(description = "标题") @RequestParam String title,
            @Parameter(description = "歌手") @RequestParam String singer,
            @Parameter(description = "专辑") @RequestParam(required = false) String album) {
        return musicService.uploadMusic(file, title, singer, album);
    }

    @Operation(summary = "音乐文件下载")
    @GetMapping(value = "/download")
    public ResponseEntity<Resource> download(@Parameter(description = "文件ID") @RequestParam("id") String id) {
        return musicService.downloadMusic(id);
    }
}
