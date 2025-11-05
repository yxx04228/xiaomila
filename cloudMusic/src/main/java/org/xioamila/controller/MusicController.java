package org.xioamila.controller;

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
import org.xioamila.vo.Result;

import javax.servlet.http.HttpServletRequest;

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
            @Parameter(name = "title", description = "歌曲标题", in = ParameterIn.QUERY, schema = @Schema(type = "string")),
            @Parameter(name = "singer", description = "歌手", in = ParameterIn.QUERY, schema = @Schema(type = "string"))
    })
    public Result<Page<Music>> getPageList(@Parameter(hidden = true) Music music,
                                            @Parameter(description = "当前页数") @RequestParam(defaultValue = "1") Integer nCurrent,
                                            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer nSize) {

        Page<Music> musicList = musicService.getPageList(new Page<>(nCurrent, nSize), music);
        return Result.data(musicList);
    }

    @Operation(summary = "音乐文件上传")
    @PostMapping(value = "/upload")
    public Result<String> uploadMusic(
            @Parameter(description = "音乐文件") @RequestParam("file") MultipartFile file) {
        return Result.data(musicService.uploadMusic(file));
    }

    @Operation(summary = "音乐文件下载")
    @GetMapping(value = "/download")
    public ResponseEntity<Resource> download(@Parameter(description = "文件ID") @RequestParam("id") String id) {
        return musicService.downloadMusic(id);
    }

    @Operation(summary = "音乐播放")
    @GetMapping(value = "/play")
    public ResponseEntity<Resource> play(@Parameter(description = "文件ID") @RequestParam("id") String id, HttpServletRequest request) {
        return musicService.playMusic(id, request);
    }

    @PutMapping("/update")
    @Operation(summary = "更新音乐")
    public Result<Boolean> updateUser(@RequestBody Music music) {
        return Result.data(musicService.updateMusic(music));
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除音乐")
    public Result<Boolean> deleteUser(@Parameter(description = "音乐ID") @RequestParam("id") String id) {
        return Result.data(musicService.deleteMusic(id));
    }
}
